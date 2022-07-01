/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filterchain.filters;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
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
 * Recreates the Chain of Responsibility pattern for <code>javax.servlet.Filter</code>(s). This Adapter adds the
 * capability to dynamically insert links into the filter chain.
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
 */
public class CustomFilterAdapter implements Filter, CustomFilterAdapterListener {
    private static final Logger LOG = LoggerFactory.getLogger(CustomFilterAdapter.class);

    private final CustomFilterAdapterConfiguration customFilterAdapterConfig;

    private FilterConfig filterConfig;

    /**
     * Stores the injected filter chain.
     */
    private volatile ImmutableList<Filter> injectedFilterChain = ImmutableList.of();

    public CustomFilterAdapter(final CustomFilterAdapterConfiguration customFilterAdapterConfig) {
        this.customFilterAdapterConfig = customFilterAdapterConfig;
    }

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
        final var localFilterChain = injectedFilterChain;
        if (localFilterChain.isEmpty()) {
            chain.doFilter(request, response);
        } else {
            AAAFilterChain.createAAAFilterChain().doFilter(request, response, chain, localFilterChain);
        }
    }

    @Override
    public void init(final FilterConfig newFilterConfig) throws ServletException {
        LOG.info("Initializing CustomFilterAdapter");

        filterConfig = newFilterConfig;

        // register as a listener for config admin changes
        customFilterAdapterConfig.registerCustomFilterAdapterConfigurationListener(this);
    }

    @Override
    public void updateInjectedFilters(final List<Filter> injectedFilters) {
        injectedFilterChain = ImmutableList.copyOf(injectedFilters);
        if (LOG.isInfoEnabled()) {
            LOG.info("Injecting a new filter chain with {} Filters: {}", injectedFilters.size(),
                injectedFilterChain.stream().map(i -> i.getClass().getSimpleName()).collect(Collectors.joining(",")));
        }
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }
}
