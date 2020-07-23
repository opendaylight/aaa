/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.configuration.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterListener;
import org.opendaylight.aaa.filterchain.filters.ExtractFilterConfigFilter;

/**
 * Custom Filter Adapter Configuration Test Suite.
 */
public class CustomFilterAdapterConfigurationImplTest {

    private static class TestCustomFilterAdapterListener implements CustomFilterAdapterListener {
        volatile List<Filter> updatedInjectedFilters;

        @Override
        public void updateInjectedFilters(final List<Filter> injectedFilters) {
            updatedInjectedFilters = injectedFilters;
        }

        @Override
        public FilterConfig getFilterConfig() {
            return null;
        }
    }

    private final CustomFilterAdapterConfigurationImpl config = new CustomFilterAdapterConfigurationImpl();
    private final TestCustomFilterAdapterListener listener = new TestCustomFilterAdapterListener();

    @Before
    public void setup() {
        config.registerCustomFilterAdapterConfigurationListener(listener);
    }

    // also tests "getCustomFilterList()" and
    // "registerCustomFilterAdapaterConfigurationListener".
    @Test
    public void testUpdatedUnresolvableClass() {
        // test a class that won't resolve
        final Map<String, String> oneUnresolvableFilterMap = new HashMap<>();
        final String oneUnresolvableFilter = "org.opendaylight.aaa.filterchain.filters.TestFilter1,"
                + "org.opendaylight.aaa.filterchain.filters.FilterDoesntExist";
        oneUnresolvableFilterMap.put(CustomFilterAdapterConfigurationImpl.CUSTOM_FILTER_LIST_KEY,
                oneUnresolvableFilter);
        config.update(oneUnresolvableFilterMap);
        assertEquals(1, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testUpdated() {
        // test valid input
        final Map<String, String> updatedDictionary = new HashMap<>();
        final String customFilterListValue = "org.opendaylight.aaa.filterchain.filters.TestFilter1,"
                + "org.opendaylight.aaa.filterchain.filters.TestFilter2";
        updatedDictionary.put(CustomFilterAdapterConfigurationImpl.CUSTOM_FILTER_LIST_KEY, customFilterListValue);
        config.update(updatedDictionary);
        assertEquals(2, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testUpdatedWithNull() {
        // test null for updated
        config.update(null);
        assertEquals(0, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testUpdatedWithNonInstantiableFilter() {
        // test with a class that cannot be instantiated
        final String cannotInstantiateClassName = "org.opendaylight.aaa.filterchain.filters.CannotInstantiate";
        final Map<String, String> cannotInstantiateDictionary = new HashMap<>();
        cannotInstantiateDictionary.put(CustomFilterAdapterConfigurationImpl.CUSTOM_FILTER_LIST_KEY,
                cannotInstantiateClassName);
        config.update(cannotInstantiateDictionary);
        assertEquals(0, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testUpdatedWithNonFilterClass() throws Exception {
        // test with a class that cannot be cast to a javax.servlet.Filter
        final String notAFilterClassName = "org.opendaylight.aaa.filterchain.filters.NotAFilter";
        final Map<String, String> notAFilterDictionary = new HashMap<>();
        notAFilterDictionary.put(CustomFilterAdapterConfigurationImpl.CUSTOM_FILTER_LIST_KEY, notAFilterClassName);
        config.update(notAFilterDictionary);
        assertEquals(0, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testUpdatedWithCfgFile() throws Exception {
        final String filterList = "org.opendaylight.aaa.filterchain.filters.ExtractFilterConfigFilter,"
                + "org.opendaylight.aaa.filterchain.filters.TestFilter2";
        final Map<String, String> filterWithCfgFileDictionary = new HashMap<>();
        filterWithCfgFileDictionary.put(CustomFilterAdapterConfigurationImpl.CUSTOM_FILTER_LIST_KEY, filterList);
        filterWithCfgFileDictionary.put("org.opendaylight.aaa.filterchain.filters.ExtractFilterConfigFilter.key",
                "value");
        filterWithCfgFileDictionary.put("badkey", "badkeyvalue");
        config.update(filterWithCfgFileDictionary);
        assertEquals(2, listener.updatedInjectedFilters.size());

        final ExtractFilterConfigFilter extractableFilter = (ExtractFilterConfigFilter) listener.updatedInjectedFilters
                .get(0);
        final FilterConfig filterConfig = extractableFilter.getFilterConfig();
        final String value = filterConfig.getInitParameter("key");
        assertNotNull(value);
        assertEquals("value", value);
    }

    @Test
    public void testListenerWithNonNullServletConfig() throws Exception {
        // just ensures a non-null ServletConfig is accepted.
        config.registerCustomFilterAdapterConfigurationListener(new CustomFilterAdapterListener() {
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

        config.update(null);
        assertEquals(0, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testUpdatedFilterInitThrowsException() throws Exception {
        final Map<String, String> initThrowsException = new HashMap<>();
        initThrowsException.put(CustomFilterAdapterConfigurationImpl.CUSTOM_FILTER_LIST_KEY,
                "org.opendaylight.aaa.filterchain.filters.FilterInitThrowsException");
        config.update(initThrowsException);
        assertEquals(1, listener.updatedInjectedFilters.size());
    }

    @Test
    public void testFilterAddedAndRemoved() {
        Filter mockFilter = mock(Filter.class);
        config.addFilter(mockFilter);

        assertEquals(1, listener.updatedInjectedFilters.size());
        assertSame(mockFilter, listener.updatedInjectedFilters.get(0));

        config.removeFilter(mockFilter);

        assertEquals(0, listener.updatedInjectedFilters.size());
    }
}
