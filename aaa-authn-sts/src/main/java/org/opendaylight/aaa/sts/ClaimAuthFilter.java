/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import static org.opendaylight.aaa.AuthConstants.AUTH_CLAIM;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;

/**
 * A generic {@link Filter} for {@link ClaimAuth} implementations.
 *
 * @author liemmn
 *
 */
public class ClaimAuthFilter implements Filter {

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        for (ClaimAuth ca : ServiceLocator.INSTANCE.ca) {
            Claim claim = ca.transform(claims((HttpServletRequest) req));
            if (claim != null) {
                req.setAttribute(AUTH_CLAIM, claim);
                // No need to do further transformation since it has been done
                break;
            }
        }
        chain.doFilter(req, resp);
    }

    // Extract attributes and headers out of the request
    private Map<String, Object> claims(HttpServletRequest req) {
        Map<String, Object> claims = new HashMap<>();

        @SuppressWarnings("unchecked")
        Enumeration<String> attrs = req.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
            claims.put(attr, req.getAttribute(attr));
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            claims.put(header, req.getHeader(header));
        }
        return claims;
    }

}
