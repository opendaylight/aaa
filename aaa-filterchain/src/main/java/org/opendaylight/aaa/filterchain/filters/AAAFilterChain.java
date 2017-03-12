/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.filters;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Recreates the <code>javax.servlet.Filter</code> chain of responsibility
 * Pattern to allow for programmatic injection of Filters. Essentially, the
 * links of the injected chain are traversed, and if the Requests makes it
 * through all of the injected Filters, then the original, existing chain is
 * maintained.
 *
 * <p>
 * This revision of code assumes that the url-pattern for injected filters is
 * exactly the same as the one specified for <code>CustomFilterAdapter</code>.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public final class AAAFilterChain implements FilterChain {

    // Must be stored locally to be used within
    // javax.servlet.FilterChain.doFilter(
    // ServletRequest, ServletResponse) due to rigid API contract.
    private volatile Iterator<Filter> injectedFilterChainIterator;
    private volatile FilterChain existingFilterChain;

    private AAAFilterChain() {
    }

    public static AAAFilterChain createAAAFilterChain() {
        return new AAAFilterChain();
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        // Recursive call using the next available link in the chain iterator If
        // a "next" link does
        // not exist and we have traversed the chain thus far, that means the
        // Request has successfully
        // traversed the injected filter chain links and we can invoke filtering
        // on the existing chain.
        if (injectedFilterChainIterator.hasNext()) {
            injectedFilterChainIterator.next().doFilter(request, response, this);
        } else {
            existingFilterChain.doFilter(request, response);
        }
    }

    /**
     * A wrapper method used to inject a new Filter chain. Essentially, this
     * just adds links to the existing chain.
     *
     * @param request
     *            Wrapped parameter passed directly to
     *            <code>doFilter(ServletRequest, ServletResponse)</code>
     * @param response
     *            Wrapped parameter passed directly to
     *            <code>doFilter(ServletRequest, ServletResponse)</code>
     * @param existingFilterChain
     *            The chain provided from Jersey as defined in the Servlet's
     *            <code>web.xml</code>
     * @param injectedFilterChain
     *            The programmatically injected chain, which may be empty
     * @throws IOException
     *             Wrapped exception handling from
     *             <code>doFilter(ServletRequest, ServletResponse)</code>
     * @throws ServletException
     *             Wrapped exception handling from
     *             <code>doFilter(ServletRequest, ServletResponse)</code>
     */
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain existingFilterChain, final List<Filter> injectedFilterChain)
            throws IOException, ServletException {

        this.existingFilterChain = existingFilterChain;
        this.injectedFilterChainIterator = injectedFilterChain.iterator();
        doFilter(request, response);
    }
}
