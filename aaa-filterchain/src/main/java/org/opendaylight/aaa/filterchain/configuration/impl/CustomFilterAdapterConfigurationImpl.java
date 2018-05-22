/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.configuration.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of CustomFilterAdapterConfiguration.
 */
public final class CustomFilterAdapterConfigurationImpl implements CustomFilterAdapterConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(CustomFilterAdapterConfigurationImpl.class);

    /**
     * Separates different filter definitions. For example:
     * <code>customFilterList = c.b.a.TestFilter1,f.d.e.TestFilter2,j.h.i.FilterN</code>
     */
    private static final String FILTER_DTO_SEPARATOR = ",";

    private static final AtomicReference<SettableFuture<CustomFilterAdapterConfiguration>> INSTANCE_FUTURE =
            new AtomicReference<>();

    /**
     * <code>customFilterList</code> is the property advertised in the Karaf
     * configuration admin.
     */
    static final String CUSTOM_FILTER_LIST_KEY = "customFilterList";

    /**
     * List of listeners to notify upon config admin events.
     */
    private final Collection<CustomFilterAdapterListener> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Saves a local copy of the most recent configuration so when a listener is
     * added, it can receive and initial update.
     */
    private volatile List<FilterDTO> filterDTOs = Collections.emptyList();

    public CustomFilterAdapterConfigurationImpl(Map<String, String> properties) {
        update(properties);

        INSTANCE_FUTURE.compareAndSet(null, SettableFuture.create());
        INSTANCE_FUTURE.get().set(this);
    }

    public static ListenableFuture<CustomFilterAdapterConfiguration> instanceFuture() {
        INSTANCE_FUTURE.compareAndSet(null, SettableFuture.create());
        return INSTANCE_FUTURE.get();
    }

    public void close() {
        SettableFuture<CustomFilterAdapterConfiguration> future = INSTANCE_FUTURE.getAndSet(null);
        if (future != null) {
            future.setException(new RuntimeException("CustomFilterAdapterConfiguration has been closed"));
        }
    }

    // Invoked in response to configuration admin changes
    public void update(Map<String, String> properties) {
        if (properties != null) {
            LOG.info("Custom filter properties updated: {}", properties);
            updateListeners(properties);
        }
    }

    /**
     * Notify all listeners of a change event.
     */
    private void updateListeners(final Map<String, String> configuration) {
        final List<FilterDTO> customFilterList = getCustomFilterList(configuration);
        this.filterDTOs = customFilterList;
        for (CustomFilterAdapterListener listener : listeners) {
            updateListener(listener, customFilterList);
        }
    }

    /**
     * Update a particular listener with the new injected <code>FilterDTO</code>
     * list.
     *
     * @param listener
     *            The <code>CustomFilterAdapter</code> instance
     * @param customFilterList
     *            The newly injected <code>FilterDTO</code> list
     */
    private void updateListener(final CustomFilterAdapterListener listener, final List<FilterDTO> customFilterList) {
        final ServletContext listenerServletContext = extractServletContext(listener);
        final List<Filter> filterList = convertCustomFilterList(customFilterList, listenerServletContext);
        listener.updateInjectedFilters(filterList);
    }

    /**
     * Utility method to extract a <code>ServletContext</code> from a listener's
     * <code>FilterConfig</code>.
     *
     * @param listener
     *            An object which listens for filter chain configuration
     *            changes.
     * @return An extracted <code>ServletContext</code>, or null if either the
     *         <code>FilterConfig</code> of <code>ServletContext</code> is null
     */
    private static ServletContext extractServletContext(final CustomFilterAdapterListener listener) {
        final FilterConfig listenerFilterConfig = listener.getFilterConfig();
        final ServletContext listenerServletContext = listenerFilterConfig != null
                ? listenerFilterConfig.getServletContext()
                : null;
        return listenerServletContext;
    }

    /**
     * Converts a List of class names (possibly Filters) and attempts to spawn
     * corresponding <code>javax.servlet.Filter</code> instances.
     *
     * @param customFilterList
     *            a list of class names, ideally Filters
     * @return a list of derived Filter(s)
     */
    private List<Filter> convertCustomFilterList(final List<FilterDTO> customFilterList,
                                                 final ServletContext servletContext) {

        final ImmutableList.Builder<Filter> injectedFiltersBuilder = ImmutableList.builder();
        for (FilterDTO customFilter : customFilterList) {
            final Filter injectedFilter = injectAndInitializeCustomFilter(customFilter, servletContext);
            if (injectedFilter != null) {
                injectedFiltersBuilder.add(injectedFilter);
            }
        }
        return injectedFiltersBuilder.build();
    }

    /**
     * Utility method used to inject and initialize a Filter. If initialization
     * fails, it is logged but the Filter is still added to the chain.
     *
     * @param customFilter
     *            DTO containing Filter and properties path, if one exists.
     * @param servletContext
     *            Scoped to the listener
     * @return A filter, or null if one cannot be instantiated.
     */
    private static Filter injectAndInitializeCustomFilter(final FilterDTO customFilter,
                                                          final ServletContext servletContext) {
        LOG.info("Attempting to load Class.forName({})", customFilter);
        try {
            final Filter filter = newInstanceOf(customFilter.getClassName());
            initializeInjectedFilter(customFilter, filter, servletContext);
            return filter;
        } catch (final ClassNotFoundException e) {
            LOG.error("skipping {} as it couldn't be found", customFilter, e);
        } catch (final ClassCastException e) {
            LOG.error("skipping {} as it could not be cast as javax.servlet.Filter", customFilter, e);
        } catch (final InstantiationException | IllegalAccessException e) {
            LOG.error("skipping {} as it cannot be instantiated", customFilter, e);
        }
        return null;
    }

    /**
     * Utility to inject a custom filter into the classpath.
     *
     * @param customFilterClassName
     *            fully qualified name of desired Filter
     * @return The Filter instance
     * @throws ClassNotFoundException
     *             The class couldn't be found, possibly since dynamic imports
     *             weren't enabled on the target bundle.
     * @throws InstantiationException
     *             The class couldn't be created
     * @throws IllegalAccessException
     *             Security manager ruled the class wasn't allowed to be
     *             created.
     */
    private static Filter newInstanceOf(final String customFilterClassName)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        final Class<?> clazz = Class.forName(customFilterClassName);
        @SuppressWarnings("unchecked")
        final Class<Filter> filterClazz = (Class<Filter>) clazz;
        final Filter filter = filterClazz.newInstance();
        return filter;
    }

    /**
     * Attempt to initialize with a generated <code>FilterConfig</code>.
     * Gracefully continue if initialization fails, but log the encountered
     * Exception.
     *
     * @param filterDTO
     *            The filter config file location is contained in the filterDTO
     *            object
     * @param filter
     *            The already created filter, which we need to initialize.
     * @param servletContext
     *            Scoped to the listener.
     */
    private static void initializeInjectedFilter(final FilterDTO filterDTO, final Filter filter,
                                                 final ServletContext servletContext) {
        try {
            final Map<String, String> initParams = filterDTO.getInitParams();
            final FilterConfig filterConfig = InjectedFilterConfig.createInjectedFilterConfig(filter, servletContext,
                    initParams);
            filter.init(filterConfig);
        } catch (final ServletException e) {
            LOG.error("Although {} was injected into the filter chain, {}.init() failed; continuing anyway",
                    filterDTO.getClassName(), filterDTO.getClassName(), e);
        }
    }

    /**
     * Allows creation of <code>FilterConfig</code> from a key/value properties
     * file.
     */
    private static final class InjectedFilterConfig implements FilterConfig {

        private final String filterName;
        private final ServletContext servletContext;
        private final Map<String, String> filterConfig;

        // private for Factory Method pattern
        private InjectedFilterConfig(final Filter filter, final ServletContext servletContext,
                                     final Map<String, String> filterConfig) {

            this.filterName = filter.getClass().getSimpleName();
            this.servletContext = servletContext;
            this.filterConfig = filterConfig;
        }

        public static InjectedFilterConfig createInjectedFilterConfig(final Filter filter,
                                                                      final ServletContext servletContext,
                                                                      final Map<String, String> filterConfig) {
            return new InjectedFilterConfig(filter, servletContext, filterConfig);
        }

        // The following is implemented for conformance with the FilterConfig
        // interface. It is never called.
        @Override
        public String getFilterName() {
            return filterName;
        }

        // The following method is implemented for conformance with the
        // FilterConfig
        // interface. It is never called.
        @Override
        public String getInitParameter(final String paramName) {
            return filterConfig != null ? filterConfig.get(paramName) : null;
        }

        // The following method is implemented for conformance with the
        // FilterConfig
        // interface. It is never called.
        @Override
        public Enumeration<String> getInitParameterNames() {
            return new Enumeration<String>() {
                final Iterator<String> keySet = filterConfig.keySet().iterator();

                @Override
                public boolean hasMoreElements() {
                    return keySet != null ? keySet.hasNext() : false;
                }

                @Override
                public String nextElement() {
                    return keySet != null ? keySet.next() : null;
                }
            };
        }

        // The following method is implemented for conformance with the
        // FilterConfig
        // interface. It is never called.
        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }
    }

    /**
     * Essentially a tuple of (filterClassName, propertiesFileName). Allows
     * quicker passing and return of Filter information.
     */
    private static final class FilterDTO {

        private final String clazzName;
        private final Map<String, String> initParams;

        // private for factory method pattern
        private FilterDTO(final String clazzName, final Map<String, String> initParams) {
            this.clazzName = clazzName;
            this.initParams = initParams;
        }

        public static FilterDTO createFilterDTO(final String clazzName, final Map<String, String> initParams) {
            return new FilterDTO(clazzName, initParams);
        }

        String getClassName() {
            return this.clazzName;
        }

        /**
         * Attempts to extract a map of key/value pairs from a given file.
         *
         * @return map with the initialization parameters.
         */
        Map<String, String> getInitParams() {
            return initParams;
        }
    }

    /**
     * Extracts the custom filter list as provided by Karaf Configuration Admin.
     *
     * @return A <code>non-null</code> <code>List</code> of the custom filter
     *         fully qualified class names.
     */
    private List<FilterDTO> getCustomFilterList(final Map<String, String> configuration) {
        final String customFilterListValue = configuration.get(CUSTOM_FILTER_LIST_KEY);
        final ImmutableList.Builder<FilterDTO> customFilterListBuilder = ImmutableList.builder();
        if (customFilterListValue != null) {
            // Creates the list from comma separate values; whitespace is
            // removed first
            final List<String> filterClazzNames = Arrays
                    .asList(customFilterListValue.replaceAll("\\s", "").split(FILTER_DTO_SEPARATOR));
            for (String filterClazzName : filterClazzNames) {
                if (!Strings.isNullOrEmpty(filterClazzName)) {
                    final Map<String, String> applicableConfigs = extractPropertiesForFilter(filterClazzName,
                            configuration);
                    final FilterDTO filterDTO = FilterDTO.createFilterDTO(filterClazzName, applicableConfigs);
                    customFilterListBuilder.add(filterDTO);
                }
            }
        }
        return customFilterListBuilder.build();
    }

    /**
     * Extract a subset of properties that apply to a particular Filter.
     *
     * @param clazzName
     *            prefix used to specify key value pair (i.e.,
     *            a.b.c.Filter.property)
     * @param fullConfiguration
     *            The entire configuration dictionary, which is traversed for
     *            applicable properties.
     * @return A Map of applicable properties for a filter.
     */
    private static Map<String, String> extractPropertiesForFilter(final String clazzName,
                                                                  final Map<String, String> fullConfiguration) {

        final Map<String, String> extractedConfig = new HashMap<>();
        for (Entry<String, String> entry : fullConfiguration.entrySet()) {
            String key = entry.getKey();
            final int lastDotSeparator = key.lastIndexOf(".");
            if (lastDotSeparator >= 0) {
                final String comparisonClazzNameSubstring = key.substring(0, lastDotSeparator);
                if (comparisonClazzNameSubstring.equals(clazzName)) {
                    final String filterInitParamKey = key.substring(lastDotSeparator + 1);
                    extractedConfig.put(filterInitParamKey, entry.getValue());
                }
            } else {
                if (!key.equals(CUSTOM_FILTER_LIST_KEY)) {
                    LOG.error("couldn't parse property \"{}\"; skipping", key);
                }
            }
        }
        return extractedConfig;
    }

    /**
     * Register for config changes.
     *
     * @param listener
     *            A listener implementing
     *            <code>CustomFilterAdapterListener</code>
     */
    @Override
    public void registerCustomFilterAdapterConfigurationListener(final CustomFilterAdapterListener listener) {
        if (this.listeners.add(listener)) {
            LOG.debug("registerCustomFilterAdapterConfigurationListener: {}, updated set: {}", listener, listeners);
            this.updateListener(listener, this.filterDTOs);
        }
    }
}