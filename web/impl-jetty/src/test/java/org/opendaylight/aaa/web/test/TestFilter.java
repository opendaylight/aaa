/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class TestFilter implements Filter {

    public boolean isInitialized = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        assertThat(filterConfig.getServletContext().getAttribute("testParam1")).isEqualTo("avalue");
        isInitialized = true;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {
        filterChain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }

}
