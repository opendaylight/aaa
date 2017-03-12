/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.filterchain.filters.ExtractFilterConfigFilter;
import org.osgi.framework.Constants;

/**
 * Custom Filter Adapter Configuration Test Suite.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class CustomFilterAdapterConfigurationTest {

    private static final CustomFilterAdapterConfiguration CUSTOM_FILTER_ADAPTER_CONFIGURATION =
            CustomFilterAdapterConfiguration.getInstance();
    private static final List<Filter>[] UPDATED_INJECTED_FILTERS = (List<Filter>[]) new List[1];

    private static final CustomFilterAdapterListener CUSTOM_FILTER_ADAPTER_LISTENER =
            new CustomFilterAdapterListener() {
        @Override
        public void updateInjectedFilters(final List<Filter> injectedFilters) {
            UPDATED_INJECTED_FILTERS[0] = injectedFilters;
        }

        @Override
        public FilterConfig getFilterConfig() {
            return null;
        }
    };

    @BeforeClass
    public static void setup() {
        CUSTOM_FILTER_ADAPTER_CONFIGURATION
                .registerCustomFilterAdapterConfigurationListener(CUSTOM_FILTER_ADAPTER_LISTENER);
    }

    @Test
    public void testGetInstance() throws Exception {
        final CustomFilterAdapterConfiguration customFilterAdapterConfiguration = CustomFilterAdapterConfiguration
                .getInstance();
        assertNotNull(customFilterAdapterConfiguration);
        assertTrue(customFilterAdapterConfiguration instanceof CustomFilterAdapterConfiguration);
    }

    @Test
    public void testGetDefaultProperties() throws Exception {
        // just pull out the defaults and make sure they are in line with what
        // is expected
        final CustomFilterAdapterConfiguration customFilterAdapterConfiguration = CustomFilterAdapterConfiguration
                .getInstance();
        final Dictionary<String, ?> defaultProperties = customFilterAdapterConfiguration.getDefaultProperties();
        assertEquals(2, defaultProperties.size());
        assertEquals(CustomFilterAdapterConfiguration.CUSTOM_FILTER_ADAPTER_CONFIGURATION_PID,
                defaultProperties.get(Constants.SERVICE_PID));
        assertEquals(CustomFilterAdapterConfiguration.DEFAULT_CUSTOM_FILTER_LIST_VALUE,
                defaultProperties.get(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY));
    }

    // also tests "getCustomFilterList()" and
    // "registerCustomFilterAdapaterConfigurationListener".
    @Test
    public void testUpdatedUnresolvableClass() throws Exception {
        // test a class that won't resolve
        final Dictionary<String, String> oneUnresolvableFilterDictionary = new Hashtable<>();
        final String oneUnresolvableFilter = "org.opendaylight.aaa.filterchain.filters.TestFilter1,"
                + "org.opendaylight.aaa.filterchain.filters.FilterDoesntExist";
        oneUnresolvableFilterDictionary.put(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY,
                oneUnresolvableFilter);
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(oneUnresolvableFilterDictionary);
        assertEquals(1, UPDATED_INJECTED_FILTERS[0].size());
    }

    @Test
    public void testUpdated() throws Exception {
        // test valid input
        final Dictionary<String, String> updatedDictionary = new Hashtable<>();
        final String customFilterListValue = "org.opendaylight.aaa.filterchain.filters.TestFilter1,"
                + "org.opendaylight.aaa.filterchain.filters.TestFilter2";
        updatedDictionary.put(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY, customFilterListValue);
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(updatedDictionary);
        assertEquals(2, UPDATED_INJECTED_FILTERS[0].size());
    }

    @Test
    public void testUpdatedWithNull() throws Exception {
        // test null for updated
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(null);
        assertEquals(0, UPDATED_INJECTED_FILTERS[0].size());
    }

    @Test
    public void testUpdatedWithNonInstantiableFilter() throws Exception {
        // test with a class that cannot be instantiated
        final String cannotInstantiateClassName = "org.opendaylight.aaa.filterchain.filters.CannotInstantiate";
        final Dictionary<String, String> cannotInstantiateDictionary = new Hashtable<>();
        cannotInstantiateDictionary.put(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY,
                cannotInstantiateClassName);
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(cannotInstantiateDictionary);
        assertEquals(0, UPDATED_INJECTED_FILTERS[0].size());
    }

    @Test
    public void testUpdatedWithNonFilterClass() throws Exception {
        // test with a class that cannot be cast to a javax.servlet.Filter
        final String notAFilterClassName = "org.opendaylight.aaa.filterchain.filters.NotAFilter";
        final Dictionary<String, String> notAFilterDictionary = new Hashtable<>();
        notAFilterDictionary.put(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY, notAFilterClassName);
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(notAFilterDictionary);
        assertEquals(0, UPDATED_INJECTED_FILTERS[0].size());
    }

    @Test
    public void testUpdatedWithCfgFile() throws Exception {
        final String filterList = "org.opendaylight.aaa.filterchain.filters.ExtractFilterConfigFilter,"
                + "org.opendaylight.aaa.filterchain.filters.TestFilter2";
        final Dictionary<String, String> filterWithCfgFileDictionary = new Hashtable<>();
        filterWithCfgFileDictionary.put(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY, filterList);
        filterWithCfgFileDictionary.put("org.opendaylight.aaa.filterchain.filters.ExtractFilterConfigFilter.key",
                "value");
        filterWithCfgFileDictionary.put("badkey", "badkeyvalue");
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(filterWithCfgFileDictionary);
        assertEquals(2, UPDATED_INJECTED_FILTERS[0].size());

        final ExtractFilterConfigFilter extractableFilter = (ExtractFilterConfigFilter) UPDATED_INJECTED_FILTERS[0]
                .get(0);
        final FilterConfig filterConfig = extractableFilter.getFilterConfig();
        final String value = filterConfig.getInitParameter("key");
        assertNotNull(value);
        assertEquals("value", value);
    }

    @Test
    public void testListenerWithNonNullServletConfig() throws Exception {
        // just ensures a non-null ServletConfig is accepted.
        CUSTOM_FILTER_ADAPTER_CONFIGURATION
                .registerCustomFilterAdapterConfigurationListener(new CustomFilterAdapterListener() {

                    @Override
                    public void updateInjectedFilters(List<Filter> injectedFilters) {
                    }

                    @Override
                    public FilterConfig getFilterConfig() {
                        return new FilterConfig() {

                            @Override
                            public String getFilterName() {
                                return null;
                            }

                            @Override
                            public ServletContext getServletContext() {
                                return null;
                            }

                            @Override
                            public String getInitParameter(String string) {
                                return null;
                            }

                            @Override
                            public Enumeration<String> getInitParameterNames() {
                                return null;
                            }
                        };
                    }
                });
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(null);
        assertEquals(0, UPDATED_INJECTED_FILTERS[0].size());
    }

    @Test
    public void testUpdatedFilterInitThrowsException() throws Exception {
        final Dictionary<String, String> initThrowsException = new Hashtable<>();
        initThrowsException.put(CustomFilterAdapterConfiguration.CUSTOM_FILTER_LIST_KEY,
                "org.opendaylight.aaa.filterchain.filters.FilterInitThrowsException");
        CUSTOM_FILTER_ADAPTER_CONFIGURATION.updated(initThrowsException);
        assertEquals(1, UPDATED_INJECTED_FILTERS[0].size());
    }
}
