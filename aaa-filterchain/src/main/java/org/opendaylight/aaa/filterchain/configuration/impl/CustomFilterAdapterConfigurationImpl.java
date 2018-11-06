/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filterchain.configuration.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.eclipse.jdt.annotation.Nullable;
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
    private volatile List<FilterDTO> namedFilterDTOs = Collections.emptyList();

    private volatile List<FilterDTO> instanceFilterDTOs = Collections.emptyList();

    public CustomFilterAdapterConfigurationImpl(final Map<String, String> properties) {
        update(properties);
    }

    public void close() {
    }

    // Invoked in response to configuration admin changes
    public void update(final Map<String, String> properties) {
        if (properties != null) {
            LOG.info("Custom filter properties updated: {}", properties);

            this.namedFilterDTOs = getCustomFilterList(properties);
            updateListeners();
        }
    }

    // Invoked when a Filter OSGi service is added
    public void onFilterAdded(final Filter filter, final Map<String, String> properties) {
        if (filter == null) {
            return;
        }

        LOG.info("Custom Filter {} added", filter);

        this.instanceFilterDTOs = ImmutableList.<FilterDTO>builder().addAll(instanceFilterDTOs)
                .add(FilterDTO.createFilterDTO(filter)).build();
        updateListeners();
    }

    // Invoked when a Filter OSGi service is removed
    public void onFilterRemoved(final Filter filter, final Map<String, String> properties) {
        if (filter == null) {
            return;
        }

        LOG.info("Custom Filter {} removed", filter);

        FilterDTO toRemove = FilterDTO.createFilterDTO(filter);
        this.instanceFilterDTOs = ImmutableList.copyOf(instanceFilterDTOs.stream().filter(dto -> !dto.equals(toRemove))
                .collect(Collectors.toList()));
        updateListeners();
    }

    /**
     * Notify all listeners of a change event.
     */
    private void updateListeners() {
        for (CustomFilterAdapterListener listener : listeners) {
            updateListener(listener);
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
    private void updateListener(final CustomFilterAdapterListener listener) {
        final Optional<ServletContext> listenerServletContext = extractServletContext(listener);
        final List<Filter> filterList = convertCustomFilterList(listenerServletContext);

        LOG.debug("Notifying listener {} of filters {}", listener, filterList);

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
    private static Optional<ServletContext> extractServletContext(final CustomFilterAdapterListener listener) {
        final FilterConfig listenerFilterConfig = listener.getFilterConfig();
        return listenerFilterConfig != null ? Optional.ofNullable(listenerFilterConfig.getServletContext())
                : Optional.empty();
    }

    /**
     * Converts a List of class names (possibly Filters) and attempts to spawn
     * corresponding <code>javax.servlet.Filter</code> instances.
     *
     * @param customFilterList
     *            a list of class names, ideally Filters
     * @return a list of derived Filter(s)
     */
    private List<Filter> convertCustomFilterList(final Optional<ServletContext> listenerServletContext) {
        final List<Filter> filterList = ImmutableList.<FilterDTO>builder().addAll(namedFilterDTOs)
            .addAll(instanceFilterDTOs).build().stream().flatMap(
                filter -> getFilterInstance(filter, listenerServletContext)).collect(Collectors.toList());
        return Collections.unmodifiableList(filterList);
    }

    /**
     * Utility method used to create and initialize a Filter from a FilterDTO.
     *
     * @param customFilter
     *            DTO containing Filter and properties path, if one exists.
     * @param servletContext
     *            Scoped to the listener
     * @return A Stream containing the Filter, or empty if one cannot be instantiated.
     */
    private static Stream<Filter> getFilterInstance(final FilterDTO customFilter,
            final Optional<ServletContext> servletContext) {
        final Filter filter = customFilter.getInstance(servletContext);
        if (filter != null) {
            LOG.info("Successfully loaded custom Filter {} for context {}", filter, servletContext);
            return Stream.of(filter);
        }

        return Stream.empty();
    }

    /**
     * Allows creation of <code>FilterConfig</code> from a key/value properties file.
     */
    private static final class InjectedFilterConfig implements FilterConfig {

        private final String filterName;
        private final ServletContext servletContext;
        private final Map<String, String> filterConfig;

        // private for Factory Method pattern
        private InjectedFilterConfig(final Filter filter, final Optional<ServletContext> servletContext,
                final Map<String, String> filterConfig) {

            this.filterName = filter.getClass().getSimpleName();
            this.servletContext = servletContext.orElse(null);
            this.filterConfig = filterConfig;
        }

        static InjectedFilterConfig createInjectedFilterConfig(final Filter filter,
                final Optional<ServletContext> servletContext, final Map<String, String> filterConfig) {
            return new InjectedFilterConfig(filter, servletContext, filterConfig);
        }

        @Override
        public String getFilterName() {
            return filterName;
        }

        @Override
        public String getInitParameter(final String paramName) {
            return filterConfig != null ? filterConfig.get(paramName) : null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return filterConfig != null ? new Enumeration<String>() {
                final Iterator<String> keySet = filterConfig.keySet().iterator();

                @Override
                public boolean hasMoreElements() {
                    return keySet.hasNext();
                }

                @Override
                public String nextElement() {
                    return keySet.next();
                }
            } : Collections.emptyEnumeration();
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
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
        LOG.debug("registerCustomFilterAdapterConfigurationListener: {}", listener);
        if (this.listeners.add(listener)) {
            LOG.debug("Updated listener set: {}", listeners);
            this.updateListener(listener);
        }
    }

    private abstract static class FilterDTO {

        private final Map<String, String> initParams;

        protected FilterDTO(final Map<String, String> initParams) {
            this.initParams = requireNonNull(initParams);
        }

        abstract @Nullable Filter getInstance(Optional<ServletContext> servletContext);

        static FilterDTO createFilterDTO(final String clazzName, final Map<String, String> initParams) {
            return new NamedFilterDTO(clazzName, initParams);
        }

        static FilterDTO createFilterDTO(final Filter instance) {
            return new InstanceFilterDTO(instance);
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
     * Essentially a tuple of (filterClassName, propertiesFileName). Allows
     * quicker passing and return of Filter information.
     */
    private static class NamedFilterDTO extends FilterDTO {
        private final String clazzName;

        NamedFilterDTO(final String clazzName, final Map<String, String> initParams) {
            super(initParams);
            this.clazzName = requireNonNull(clazzName);
        }

        @SuppressWarnings("unchecked")
        @Override
        Filter getInstance(final Optional<ServletContext> servletContext) {
            try {
                final Class<Filter> filterClazz = (Class<Filter>) Class.forName(clazzName);
                return init(filterClazz.newInstance(), servletContext);
            } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
                LOG.error("Error loading  {}", this, e);
            }

            return null;
        }

        private Filter init(final Filter filter, final Optional<ServletContext> servletContext) {
            try {
                FilterConfig filterConfig = InjectedFilterConfig.createInjectedFilterConfig(filter, servletContext,
                        getInitParams());
                filter.init(filterConfig);
            } catch (ServletException e) {
                LOG.error("Error injecting custom filter {} - continuing anyway", filter, e);
            }

            return filter;
        }

        @Override
        public int hashCode() {
            return clazzName.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            NamedFilterDTO other = (NamedFilterDTO) obj;
            return clazzName.equals(other.clazzName);
        }

        @Override
        public String toString() {
            return "NamedFilterDTO [clazzName=" + clazzName + ", initParams=" + getInitParams() + "]";
        }
    }

    private static class InstanceFilterDTO extends FilterDTO {
        private final Filter instance;

        InstanceFilterDTO(final Filter instance) {
            super(Collections.emptyMap());
            this.instance = requireNonNull(instance);
        }

        @Override
        Filter getInstance(final Optional<ServletContext> servletContext) {
            return instance;
        }

        @Override
        public int hashCode() {
            return instance.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            InstanceFilterDTO other = (InstanceFilterDTO) obj;
            return instance.equals(other.instance);
        }

        @Override
        public String toString() {
            return "InstanceFilterDTO [instance=" + instance + "]";
        }
    }
}
