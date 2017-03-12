/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.filters;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Some Mock utilities used in many different JUnit Test classes.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class FilterChainMockUtils {
    /**
     * Creates a mock <code>Filter</code> which adds ingress output to
     * callStack, calls doFilter, then adds egress output to the callStack.
     *
     * @param name
     *            The name of the filter
     * @param callStack
     *            Just a list of String events
     * @return The mocked up filter
     */
    private static Filter createMockFilter(final String name, final List<String> callStack)
            throws IOException, ServletException {
        final Filter testFilter = mock(Filter.class);
        doAnswer(invocationOnMock -> {
            callStack.add(name + " ingress");
            final Object[] args = invocationOnMock.getArguments();
            final ServletRequest servletRequest = (ServletRequest) args[0];
            final ServletResponse servletResponse = (ServletResponse) args[1];
            final FilterChain filterChain = (FilterChain) args[2];
            filterChain.doFilter(servletRequest, servletResponse);
            callStack.add(name + " egress");
            return null;
        }).when(testFilter).doFilter(any(), any(), any());
        return testFilter;
    }

    /**
     * Forms the expected call stack, which is really just an event listing.
     *
     * @param size
     *            the number of filters used to create the call stack
     * @return A list of String events
     */
    static List<String> formExpectedCallStack(final int size) {
        final List<String> expected = new Vector<>();
        for (int i = 0; i < size; i++) {
            expected.add(String.format("filter%d ingress", i));
        }
        for (int i = size - 1; i >= 0; i--) {
            expected.add(String.format("filter%d egress", i));
        }
        return expected;
    }

    /**
     * Creates a filter chain test case.
     *
     * @param size
     *            The number of links for the test case
     * @return the test case
     */
    static TestFilterDTO createFilterChain(final int size) throws IOException, ServletException {
        final List<Filter> filters = new Vector<>();
        final List<String> callBack = new Vector<>();
        for (int i = 0; i < size; i++) {
            final Filter filter = createMockFilter(String.format("filter%d", i), callBack);
            filters.add(filter);
        }

        return new TestFilterDTO(filters, callBack);
    }

    /**
     * A holder class for the tuple <code>(filters,callStack)</code>.
     */
    static final class TestFilterDTO {
        private List<Filter> filters;
        private List<String> callStack;

        TestFilterDTO(final List<Filter> filters, final List<String> callStack) {
            this.filters = filters;
            this.callStack = callStack;
        }

        public List<Filter> getFilters() {
            return filters;
        }

        public List<String> getCallStack() {
            return callStack;
        }
    }
}
