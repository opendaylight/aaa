/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.configuration;

import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.opendaylight.aaa.filterchain.filters.CustomFilterAdapterListener;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for parsing configuration from the <code>CustomFilterAdapterConfiguration</code>
 * configuration admin file (or service).  This interface may be configured through the Karaf
 * webconsole, or though adding a configuration file as follows.  The configuration can be specified
 * through performing the following:
 *
 * Edit <code>etc/org.opendaylight.aaa.filterchain.cfg</code>:
 *
 * Add Filters in the following form:
 * <code>customFilterList = c.b.a.Filter1,f.d.e.Filter2,j.h.i.FilterN</code>
 *
 * If you wish to specify key/value init-params (normally done in web.xml), you can do so by adding
 * a <b>$</b>sign followed by the path to the file.  For example:
 * <code>customFilterList = c.b.a.Filter1$etc/Filter1PropertyFile.cfg,f.d.e.Filter2</code>
 *
 * Properties within the init-param configuration file are specified using the following convention:
 * <code>name = value</code>
 *
 * Updates to init-param property files are only honored following updates of the config admin file.
 * Thus, if you change a property, you must force an update to the configuration file to reinitialize
 * the <code>Filter</code>.
 *
 * Note:  If you wish to use a Filter from an outside package, you must first enable dynamic import
 * for that bundle:
 * <code>bundle:dynamic-import ID</code>
 *
 * This class follows the Singleton design pattern, and the instance is extracted through:
 * <code>CustomFilterAdapterConfiguration.getInstance()</code>
 *
 * This class is a <code>CustomFilterAdapterConfiguration</code> Event Producer, and objects
 * can subscribe for changes through:
 * <code>CustomFilterAdapterConfiguration.registerCustomFilterAdapterConfigurationListener(...)</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class CustomFilterAdapterConfiguration implements ManagedService {

    /**
     * Separates different filter definitions.  For example:
     * <code>customFilterList = c.b.a.Filter1,f.d.e.Filter2,j.h.i.FilterN</code>
     */
    static final String FILTER_DTO_SEPARATOR = ",";

    private static final String FILTER_PROPERTIES_FILE_SEPARATOR_REGEX = "\\$";

    private static final String FILTER_PROPERTIES_FILE_SEPARATOR = "$";

    private static final Logger LOG = LoggerFactory.getLogger(CustomFilterAdapterConfiguration.class);

    /**
     * PID key used by Apache Configuration Admin to store custom filter adapter configuration
     * related information.  Also represents to the config admin filename, which is
     * <code>etc/org.opendaylight.aaa.filterchain.cfg</code>.
     */
    static final String CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID = "org.opendaylight.aaa.filterchain";

    /**
     * <code>customFilterList</code> is the property advertised in the Karaf configuration admin
     */
    static final String CUSTOM_FILTER_LIST_KEY = "customFilterList";

    /**
     * Default the customFilterList to an empty string.
     */
    static final String DEFAULT_CUSTOM_FILTER_LIST_VALUE = "";

    /**
     * Stores the default configuration, which is a combination of the PID and key/value pairs.
     */
    private static final Hashtable<String, String> DEFAULT_CONFIGURATION = new Hashtable<>();

    // Initialize the defaults.
    static {
        DEFAULT_CONFIGURATION.put(Constants.SERVICE_PID, CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID);
        DEFAULT_CONFIGURATION.put(CUSTOM_FILTER_LIST_KEY, DEFAULT_CUSTOM_FILTER_LIST_VALUE);
    }

    // For singleton
    private static final CustomFilterAdapterConfiguration INSTANCE = new CustomFilterAdapterConfiguration();

    // the active configuration;  this variable is mutated in response to config admin changes
    private volatile Map<String, String> configuration = new ConcurrentHashMap<>();

    /**
     * List of listeners to notify upon config admin events.
     */
    private volatile List<CustomFilterAdapterListener> listeners = new Vector<CustomFilterAdapterListener>();

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
    public void updated(final Dictionary<String, ?> properties)
            throws ConfigurationException {

        if (properties == null) {
            this.resetPropertiesToDefault();
            updateListeners();
        } else {
            configuration = extractPropertiesMap(properties);
            addPID(configuration);
            updateListeners();
        }
    }

    /**
     * Utility method to convert a properties <code>Dictionary</code> to a properties <code>Map</code>.
     *
     * @param propertiesDictionary Supplied by config admin.
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
                // should never happen;  Just put here to be explicit.
                LOG.error("skipping CustomFilterAdapterConfiguration config for key \"{}\"; can't parse value",
                        key, e);
            }
        }
        return propertiesMap;
    }

    /**
     * Mutator method to add PID to a provided map.
     *
     * @param configuration a properties map
     */
    private void addPID(final Map<String, String> configuration) {
        configuration.put(Constants.SERVICE_PID, CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID);
    }

    /**
     * Resets the configuration to the default.
     */
    private void resetPropertiesToDefault() {
        configuration.clear();
        configuration.putAll(DEFAULT_CONFIGURATION);
    }

    /**
     * Notify all listeners of a change event.
     */
    private void updateListeners() {
        final List<FilterDTO> customFilterList = getCustomFilterList();
        for (CustomFilterAdapterListener listener : listeners) {
            updateListener(listener, customFilterList);
        }
    }

    /**
     * Update a particular listener with the new injected <code>FilterDTO</code> list.
     *
     * @param listener The <code>CustomFilterAdapter</code> instance
     * @param customFilterList The newly injected <code>FilterDTO</code> list
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
     * @param listener An object which listens for filter chain configuration changes.
     * @return An extracted <code>ServletContext</code>, or null if either the
     * <code>FilterConfig</code> of <code>ServletContext</code> is null
     */
    private static ServletContext extractServletContext(final CustomFilterAdapterListener listener) {
        final FilterConfig listenerFilterConfig = listener.getFilterConfig();
        final ServletContext listenerServletContext =
                (listenerFilterConfig != null ? listenerFilterConfig.getServletContext() : null);
        return listenerServletContext;
    }

    /**
     * Converts a List of class names (possibly Filters) and attempts to spawn corresponding
     * <code>javax.servlet.Filter</code> instances.
     *
     * @param customFilterList a list of class names, ideally Filters
     * @return a list of derived Filter(s)
     */
    private List<Filter> convertCustomFilterList(final List<FilterDTO> customFilterList,
            final ServletContext servletContext) {
        final List<Filter> injectedFilters = new ArrayList<>();
        for (FilterDTO customFilter : customFilterList) {
            final Filter injectedFilter = injectAndInitializeCustomFilter(customFilter, servletContext);
            if (injectedFilter != null) {
                injectedFilters.add(injectedFilter);
            }
        }
        return injectedFilters;
    }

    /**
     * Utility method used to inject and initialize a Filter.  If initialization fails, it is
     * logged but the Filter is still added to the chain.
     *
     * @param customFilter DTO containing Filter and properties path, if one exists.
     * @param servletContext Scoped to the listener
     * @return A filter, or null if one cannot be instantiated.
     */
    private static Filter injectAndInitializeCustomFilter(final FilterDTO customFilter, final ServletContext servletContext) {
        LOG.info("Attempting to load Class.forName({})", customFilter);
        try {
            final Filter filter = injectCustomFilter(customFilter);
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
     * @param customFilter DTO containing the desired filters fully qualified name
     * @return The Filter instance
     * @throws ClassNotFoundException The class couldn't be found, possibly since
     * dynamic imports weren't enabled on the target bundle.
     * @throws InstantiationException The class couldn't be created
     * @throws IllegalAccessException Security manager ruled the class wasn't allowed
     * to be created.
     */
    private static Filter injectCustomFilter(final FilterDTO customFilter)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        final Class<?> clazz = Class.forName(customFilter.getClassName());
        @SuppressWarnings("unchecked")
        final Class<Filter> filterClazz = (Class<Filter>) clazz;
        final Filter filter = filterClazz.newInstance();
        return filter;
    }

    /**
     * Attempt to initialize with a generated <code>FilterConfig</code>.  Gracefully continue if
     * initialization fails, but log the encountered Exception.
     *
     * @param filterDTO The filter config file location is contained in the filterDTO object
     * @param filter The already created filter, which we need to initialize.
     * @param servletContext Scoped to the listener.
     */
    private static void initializeInjectedFilter(final FilterDTO filterDTO, final Filter filter, final ServletContext servletContext) {
        try {
            final Map<String, String> initParams = filterDTO.getInitParams();
            final FilterConfig filterConfig = InjectedFilterConfig.createInjectedFilterConfig(filter, servletContext, initParams);
            filter.init(filterConfig);
        } catch (final ServletException e) {
            LOG.error("Although {} was injected into the filter chain, {}.init() failed; continuing anyway",
                    filterDTO.getClassName(), filterDTO.getClassName(), e);
        }
    }

    /**
     * Allows creation of <code>FilterConfig</code> from a key/value properties file.
     */
    private static class InjectedFilterConfig implements FilterConfig {

        private String filterName;
        private ServletContext servletContext;
        private Map<String, String> filterConfig;

        // private for Factory Method pattern
        private InjectedFilterConfig(final Filter filter, final ServletContext servletContext,
                final Map<String, String> filterConfig) {

            this.filterName = filter.getClass().getSimpleName();
            this.servletContext = servletContext;
            this.filterConfig = filterConfig;
        }

        public static InjectedFilterConfig createInjectedFilterConfig(final Filter filter, final ServletContext servletContext,
                final Map<String, String> filterConfig) {
            return new InjectedFilterConfig(filter, servletContext, filterConfig);
        }

        // The following is implemented for conformance with the FilterConfig
        // interface.  It is never called.
        @Override
        public String getFilterName() {
            return filterName;
        }

        // The following method is implemented for conformance with the FilterConfig
        // interface.  It is never called.
        @Override
        public String getInitParameter(final String paramName) {
            return (filterConfig != null ? filterConfig.get(paramName) : null);
        }

        // The following method is implemented for conformance with the FilterConfig
        // interface.  It is never called.
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

        // The following method is implemented for conformance with the FilterConfig
        // interface.  It is never called.
        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }
    }

    /**
     * Essentially a tuple of (filterClassName, propertiesFileName).  Allows quicker passing
     * and return of Filter information.
     */
    static class FilterDTO {

        private String clazzName;
        private String propertiesFileName = null;

        // private for factory method pattern
        private FilterDTO(final String clazzName, final String propertiesFileName) {
            this.clazzName = clazzName;
            this.propertiesFileName = propertiesFileName;
        }

        public static FilterDTO createFilterDTO(final String clazzName, final String propertiesFileName) {
            return new FilterDTO(clazzName, propertiesFileName);
        }

        String getClassName() {
            return this.clazzName;
        }

        String getPropertiesFileName() {
            return this.propertiesFileName;
        }

        /**
         * Attempts to extract a map of key/value pairs from a given file.
         * @return
         */
        Map<String, String> getInitParams() {
            if (this.propertiesFileName == null) {
                return null;
            }
            final Map<String, String> propertyMap = new HashMap<>();
            final File f = new File(getPropertiesFileName());
            // read line by line
            Scanner s = null;
            try {
                s = new Scanner(f);
                while (s.hasNextLine()) {
                    final String prop = s.nextLine();
                    final String [] assignment = prop.replaceAll("\\s","").split("=");
                    try {
                        if (!assignment[0].startsWith("#")) {
                            propertyMap.put(assignment[0], assignment[1]);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        LOG.error("encountered an unparsable line in {} for {}, skipping!",
                                propertiesFileName, clazzName);
                    }
                }
            } catch (final IOException e) {
                LOG.error("Couldn't parse {} for {}", propertiesFileName, clazzName);
                return null;
            } finally {
                if (s != null) {
                    s.close();
                }
            }
            return propertyMap;
        }
    }

    /**
     * Extracts the custom filter list as provided by Karaf Configuration Admin.
     *
     * @return A <code>non-null</code> <code>List</code> of the custom filter fully qualified class names.
     */
    public List<FilterDTO> getCustomFilterList() {
        final String customFilterListValue = configuration.get(CUSTOM_FILTER_LIST_KEY);
        final List<FilterDTO> customFilterList = new ArrayList<>();
        if (customFilterListValue != null) {
            // Creates the list from comma separate values;  whitespace is removed first
            final List<String> filterDefinitions = Arrays.asList(customFilterListValue.replaceAll("\\s","").split(FILTER_DTO_SEPARATOR));
            for (String filterDefinition : filterDefinitions) {
                if (!Strings.isNullOrEmpty(filterDefinition)) {
                    if (filterDefinition.contains(FILTER_PROPERTIES_FILE_SEPARATOR)) {
                        final String[] filterConfig = filterDefinition.split(FILTER_PROPERTIES_FILE_SEPARATOR_REGEX);
                        try {
                            final String filterName = filterConfig[0];
                            final String filterPropertiesFileName = filterConfig[1];
                            final FilterDTO filterDTO = FilterDTO.createFilterDTO(filterName, filterPropertiesFileName);
                            customFilterList.add(filterDTO);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            // This should never happen, but we catch it just in case.
                            LOG.error("Ignoring {} as it could not be parsed correctly.", filterDefinition);
                        }
                    } else {
                        customFilterList.add(new FilterDTO(filterDefinition, null));
                    }
                }
            }
        }
        return customFilterList;
    }

    /**
     * Register for config changes.
     *
     * @param listener A listener implementing <code>CustomFilterAdapterListener</code>
     */
    public void registerCustomFilterAdapterConfigurationListener(final CustomFilterAdapterListener listener) {
        this.listeners.add(listener);
    }
}
