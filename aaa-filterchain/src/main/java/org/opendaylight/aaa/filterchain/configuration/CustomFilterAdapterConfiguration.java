/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.configuration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for parsing configuration from the
 * <code>CustomFilterAdapterConfiguration</code> configuration admin file (or
 * service). This interface may be configured through the Karaf webconsole, or
 * though adding a configuration file as follows. The configuration can be
 * specified through performing the following:
 *
 * <p>
 * Edit <code>etc/org.opendaylight.aaa.filterchain.cfg</code>:
 *
 * <p>
 * Add Filters in the following form:
 * <code>customFilterList = c.b.a.TestFilter1,f.d.e.TestFilter2,j.h.i.FilterN</code>
 *
 * <p>
 * If you wish to specify key/value init-params (normally done in web.xml), you
 * can do so by adding them scoped to the filter on a new line. For example:
 * <code>c.b.a.TestFilter1.propertyKey=propertyValue</code>
 *
 * <p>
 * This class follows the Singleton design pattern, and the instance is
 * extracted through:
 * <code>CustomFilterAdapterConfiguration.getInstance()</code>
 *
 * <p>
 * This class is a <code>CustomFilterAdapterConfiguration</code> Event Producer,
 * and objects can subscribe for changes through:
 * <code>CustomFilterAdapterConfiguration.registerCustomFilterAdapterConfigurationListener(...)</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class CustomFilterAdapterConfiguration implements ManagedService {

    /**
     * Separates different filter definitions. For example:
     * <code>customFilterList = c.b.a.TestFilter1,f.d.e.TestFilter2,j.h.i.FilterN</code>
     */
    static final String FILTER_DTO_SEPARATOR = ",";

    private static final Logger LOG = LoggerFactory.getLogger(CustomFilterAdapterConfiguration.class);

    /**
     * PID key used by Apache Configuration Admin to store custom filter adapter
     * configuration related information. Also represents to the config admin
     * filename, which is <code>etc/org.opendaylight.aaa.filterchain.cfg</code>.
     */
    static final String CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID = "org.opendaylight.aaa.filterchain";

    /**
     * <code>customFilterList</code> is the property advertised in the Karaf
     * configuration admin.
     */
    static final String CUSTOM_FILTER_LIST_KEY = "customFilterList";

    /**
     * Default the customFilterList to an empty string.
     */
    static final String DEFAULT_CUSTOM_FILTER_LIST_VALUE = "";

    /**
     * Stores the default configuration, which is a combination of the PID and
     * key/value pairs.
     */
    private static final Hashtable<String, String> DEFAULT_CONFIGURATION = new Hashtable<>();

    // For singleton
    private static final CustomFilterAdapterConfiguration INSTANCE = new CustomFilterAdapterConfiguration();

    // Initialize the defaults.
    static {
        DEFAULT_CONFIGURATION.put(Constants.SERVICE_PID, CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID);
        DEFAULT_CONFIGURATION.put(CUSTOM_FILTER_LIST_KEY, DEFAULT_CUSTOM_FILTER_LIST_VALUE);
    }

    /**
     * List of listeners to notify upon config admin events.
     */
    private final List<CustomFilterAdapterListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Saves a local copy of the most recent configuration so when a listener is
     * added, it can receive and initial update.
     */
    private volatile List<FilterDTO> filterDTOs = Collections.emptyList();

    private CustomFilterAdapterConfiguration() {
        // private for Singleton
    }

    /**
     * Extract the Singleton <code>CustomFilterAdapterConfiguration</code>.
     *
     * @return The <code>CustomFilterAdapterConfiguration</code> instance
     */
    public static CustomFilterAdapterConfiguration getInstance() {
        return INSTANCE;
    }

    public Dictionary<String, ?> getDefaultProperties() {
        return DEFAULT_CONFIGURATION;
    }

    // Invoked in response to configuration admin changes
    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {

        if (properties == null) {
            updateListeners(DEFAULT_CONFIGURATION);
        } else {
            final Map<String, String> configuration = extractPropertiesMap(properties);
            updateListeners(configuration);
        }
    }

    /**
     * Utility method to convert a properties <code>Dictionary</code> to a
     * properties <code>Map</code>.
     *
     * @param propertiesDictionary
     *            Supplied by config admin.
     * @return A properties map, which is a somewhat friendlier interface
     */
    private static Map<String, String> extractPropertiesMap(final Dictionary<String, ?> propertiesDictionary) {
        final Map<String, String> propertiesMap = new HashMap<>();
        final Enumeration<String> keys = propertiesDictionary.keys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            try {
                final String value = (String) propertiesDictionary.get(key);
                propertiesMap.put(key, value);
            } catch (final ClassCastException e) {
                // should never happen; Just put here to be explicit.
                LOG.error("skipping CustomFilterAdapterConfiguration config for key \"{}\"; can't parse value", key, e);
            }
        }
        // add in the PID
        propertiesMap.put(Constants.SERVICE_PID, CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID);
        return propertiesMap;
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
        final ServletContext listenerServletContext = (listenerFilterConfig != null
                ? listenerFilterConfig.getServletContext()
                : null);
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
    private static class InjectedFilterConfig implements FilterConfig {

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
                final ServletContext servletContext, final Map<String, String> filterConfig) {
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
            return (filterConfig != null ? filterConfig.get(paramName) : null);
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
                    return (keySet != null ? keySet.hasNext() : false);
                }

                @Override
                public String nextElement() {
                    return (keySet != null ? keySet.next() : null);
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
    private static class FilterDTO {

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
        final Set<String> fullConfigurationKeySet = fullConfiguration.keySet();
        for (String key : fullConfigurationKeySet) {
            final int lastDotSeparator = key.lastIndexOf(".");
            if (lastDotSeparator >= 0) {
                final String comparisonClazzNameSubstring = key.substring(0, lastDotSeparator);
                if (comparisonClazzNameSubstring.equals(clazzName)) {
                    final String value = fullConfiguration.get(key);
                    final String filterInitParamKey = key.substring(lastDotSeparator + 1);
                    extractedConfig.put(filterInitParamKey, value);
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
    public void registerCustomFilterAdapterConfigurationListener(final CustomFilterAdapterListener listener) {
        this.listeners.add(listener);
        this.updateListener(listener, this.filterDTOs);
    }
}
