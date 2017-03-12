/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests chain functionality with null, 0, 1, 2, 100 and 101 links.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class AAAFilterChainTest {

    private final ServletRequest servletRequest = mock(ServletRequest.class);
    private final ServletResponse servletResponse = mock(ServletResponse.class);
    private static final FilterChain FILTER_CHAIN = mock(FilterChain.class);

    @BeforeClass
    public static void setup() throws IOException, ServletException {
        // existingChain.doFilter() should always just return (so the egress
        // path can be traversed)
        doAnswer(invocationOnMock -> null).when(FILTER_CHAIN).doFilter(any(), any());
    }

    @Test
    public void testCreateAAAFilterChain() throws Exception {
        final AAAFilterChain aaaFilterChain = AAAFilterChain.createAAAFilterChain();
        assertNotNull(aaaFilterChain);
        assertTrue(aaaFilterChain instanceof AAAFilterChain);
    }

    @Test
    public void testDoFilterNoFilters() throws IOException, ServletException {
        final ServletRequest servletRequest = mock(ServletRequest.class);
        final ServletResponse servletResponse = mock(ServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        doAnswer(invocationOnMock -> null).when(filterChain).doFilter(any(), any());
        final List<Filter> injectedFilterChain = new Vector<>();
        AAAFilterChain.createAAAFilterChain().doFilter(servletRequest, servletResponse, filterChain,
                injectedFilterChain);
    }

    @Test
    public void testDoFilter() throws IOException, ServletException {
        testChain(1);
        testChain(2);
        testChain(100);
        testChain(101);
    }

    /**
     * Test chain traversal of a certain size.
     *
     * @param numLinks
     *            The number of links to traverse in the chain.
     */
    public void testChain(final int numLinks) throws IOException, ServletException {
        final FilterChainMockUtils.TestFilterDTO testFilterDTO = FilterChainMockUtils.createFilterChain(numLinks);
        final List<String> callStack = testFilterDTO.getCallStack();
        final List<Filter> injectedFilterChain = testFilterDTO.getFilters();
        AAAFilterChain.createAAAFilterChain().doFilter(servletRequest, servletResponse, FILTER_CHAIN,
                injectedFilterChain);
        final List<String> expectedCallStack = FilterChainMockUtils.formExpectedCallStack(numLinks);
        final Iterator<String> expectedIterator = expectedCallStack.iterator();
        for (String actual : callStack) {
            assertEquals(expectedIterator.next(), actual);
        }
        callStack.clear();
        injectedFilterChain.clear();
        expectedCallStack.clear();
    }

}
