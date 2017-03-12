/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.filters;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recreates the Chain of Responsibility pattern for
 * <code>javax.servlet.Filter</code>(s). Jersey 1.17 does not include the
 * ability to programmatically add Filter(s), as Filter chains are defined at
 * compile time within the <code>web.xml</code> file. This Adapter dynamically
 * adds the capability to dynamically insert links into the filter chain.
 *
 * <p>
 * This Adapter is enabled by placing the <code>CustomFilterAdapter</code> in
 * the Servlet's <code>web.xml</code> definition (ideally directly after the
 * <code>AAAFilter</code> Filter, as ordering is honored directly).
 *
 * <code>CustomFilterAdapter.doFilter(...)</code> calls
 * <code>AAAFilterChain.doFilter(...)</code>, which honors the injected filter
 * chain links, and then continues the original filter chain.
 *
 * <p>
 * This code was designed specifically to work with the common, generic
 * <code>javax.servlet.Filter</code> interface; thus, certain choices, such as
 * creating a new <code>AAAFilterChain</code> per request, were necessary to
 * preserve the existing API contracts (i.e., the injected chain is stored as a
 * local variable in <code>AAAFilterChain</code> so it may be used in existing
 * methods (could not be passed as a parameter), and if a new chain was not
 * spawned each time, there is a risk that the existingChain changes in the
 * middle of requests, causing inconsistent behavior.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class CustomFilterAdapter implements Filter, CustomFilterAdapterListener {

    private static final Logger LOG = LoggerFactory.getLogger(CustomFilterAdapter.class);

    private FilterConfig filterConfig;

    /**
     * Stores the injected filter chain. TODO can this be an ArrayList?
     */
    private volatile List<Filter> injectedFilterChain = Collections.emptyList();

    @Override
    public void destroy() {
        LOG.info("Destroying CustomFilterAdapter");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        // chain is the existing chain of responsibility, and filterChain
        // contains the new links to inject into the existing chain. Since
        // Jersey spawns <code>chain</code> for each request, a new chain
        List<Filter> localFilterChain = injectedFilterChain;
        if (!localFilterChain.isEmpty()) {
            AAAFilterChain.createAAAFilterChain().doFilter(request, response, chain, localFilterChain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        LOG.info("Initializing CustomFilterAdapter");
        // register as a listener for config admin changes
        CustomFilterAdapterConfiguration.getInstance().registerCustomFilterAdapterConfigurationListener(this);
        this.filterConfig = filterConfig;
    }

    /**
     * Updates the injected filter chain.
     *
     * @param filterChain
     *            The injected chain
     */
    private void setInjectedFilterChain(final List<Filter> filterChain) {
        this.injectedFilterChain = ImmutableList.copyOf(filterChain);
        final String commaSeperatedFilterChain = this.injectedFilterChain.stream()
                .map(i -> i.getClass().getSimpleName()).collect(Collectors.joining(","));
        LOG.info("Injecting a new filter chain with {} Filters: {}", filterChain.size(), commaSeperatedFilterChain);
    }

    @Override
    public void updateInjectedFilters(final List<Filter> injectedFilters) {
        this.setInjectedFilterChain(injectedFilters);
    }

    @Override
    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }
}
