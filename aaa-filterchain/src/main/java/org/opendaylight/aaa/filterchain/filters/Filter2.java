/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to help guide configuration of a deployment.  If an operator is confused
 * whether a Filter is reached/traversed, he or she can use this class to produce
 * some basic karaf log output.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class Filter2 implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(Filter2.class);
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain filterChain) throws IOException, ServletException {
        // TODO Auto-generated method stub
        LOG.error("INGRESS FILTER2");
        filterChain.doFilter(request, response);
        LOG.error("EGRESS FILTER2");
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

}
