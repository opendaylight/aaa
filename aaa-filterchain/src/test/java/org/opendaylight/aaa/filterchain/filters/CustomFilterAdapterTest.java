/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.Test;

/**
 * Custom Filter Adapter Test Suite.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class CustomFilterAdapterTest {

    final ServletRequest servletRequest = mock(ServletRequest.class);
    final ServletResponse servletResponse = mock(ServletResponse.class);
    final FilterChain filterChain = mock(FilterChain.class);

    @Test
    public void testDoFilter() throws Exception {
        final CustomFilterAdapter customFilterAdapter = new CustomFilterAdapter();
        final FilterChainMockUtils.TestFilterDTO testFilterDTO = FilterChainMockUtils.createFilterChain(3);
        customFilterAdapter.updateInjectedFilters(testFilterDTO.getFilters());
        final boolean[] existingFilterChainEncountered = { false };
        doAnswer(invocationOnMock -> {
            existingFilterChainEncountered[0] = true;
            return null;
        }).when(filterChain).doFilter(any(), any());
        customFilterAdapter.doFilter(servletRequest, servletResponse, filterChain);
        assertTrue(existingFilterChainEncountered[0]);
        customFilterAdapter.destroy();
    }

    // also tests CustomFilterAdapter.getFilterConfig()
    @Test
    public void testInit() throws Exception {
        final CustomFilterAdapter customFilterAdapter = new CustomFilterAdapter();
        // test tolerance of null filter config
        customFilterAdapter.init(null);
        assertNull(customFilterAdapter.getFilterConfig());

        // Test that added filter config essentially has no effect, since
        // CustomFilterAdapter doesn't accept any configuration init-params.
        final FilterConfig filterConfig = new FilterConfig() {

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
        customFilterAdapter.init(filterConfig);
        assertEquals(filterConfig, customFilterAdapter.getFilterConfig());
        customFilterAdapter.destroy();
    }

    @Test
    public void testUpdateInjectedFilters() throws IOException, ServletException {
        testUpdateInjectedFilters(0);
        testUpdateInjectedFilters(1);
        testUpdateInjectedFilters(2);
        testUpdateInjectedFilters(100);
    }

    private void testUpdateInjectedFilters(final int size) throws IOException, ServletException {
        final CustomFilterAdapter customFilterAdapter = new CustomFilterAdapter();
        final FilterChainMockUtils.TestFilterDTO testFilterDTO = FilterChainMockUtils.createFilterChain(size);
        customFilterAdapter.updateInjectedFilters(testFilterDTO.getFilters());
        customFilterAdapter.doFilter(servletRequest, servletResponse, filterChain);
        final List<String> callStack = testFilterDTO.getCallStack();
        final List<String> expectedCallStack = FilterChainMockUtils.formExpectedCallStack(size);
        final Iterator<String> expectedIterator = expectedCallStack.iterator();
        for (String actual : callStack) {
            assertEquals(expectedIterator.next(), actual);
        }
        customFilterAdapter.destroy();
    }

    @Test
    public void testRealFilters() throws Exception {
        final CustomFilterAdapter customFilterAdapter = new CustomFilterAdapter();
        final List<Filter> injectedFilters = new Vector<>();
        injectedFilters.add(new TestFilter1());
        injectedFilters.add(new TestFilter2());
        for (Filter filter : injectedFilters) {
            filter.init(null);
        }
        customFilterAdapter.updateInjectedFilters(injectedFilters);
        customFilterAdapter.doFilter(servletRequest, servletResponse, filterChain);
        final boolean[] existingFilterChainEncountered = { false };
        doAnswer(invocationOnMock -> {
            existingFilterChainEncountered[0] = true;
            return null;
        }).when(filterChain).doFilter(any(), any());
        customFilterAdapter.doFilter(servletRequest, servletResponse, filterChain);
        assertTrue(existingFilterChainEncountered[0]);
        for (Filter filter : injectedFilters) {
            filter.destroy();
        }
    }
}
