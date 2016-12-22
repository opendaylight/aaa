/*
 * Copyright (c) 2014, 2015 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Deprecated
class SssdHeadersRequest extends HttpServletRequestWrapper {
    private static final String headerPrefix = "X-SSSD-";

    public SssdHeadersRequest(HttpServletRequest request) {
        super(request);
    }

    public Object getAttribute(String name) {
        HttpServletRequest request = (HttpServletRequest) getRequest();
        String headerValue;

        headerValue = request.getHeader(headerPrefix + name);
        if (headerValue != null) {
            return headerValue;
        } else {
            return request.getAttribute(name);
        }
    }

    @Override
    public String getRemoteUser() {
        HttpServletRequest request = (HttpServletRequest) getRequest();
        String headerValue;

        headerValue = request.getHeader(headerPrefix + "REMOTE_USER");
        if (headerValue != null) {
            return headerValue;
        } else {
            return request.getRemoteUser();
        }
    }

    @Override
    public String getAuthType() {
        HttpServletRequest request = (HttpServletRequest) getRequest();
        String headerValue;

        headerValue = request.getHeader(headerPrefix + "AUTH_TYPE");
        if (headerValue != null) {
            return headerValue;
        } else {
            return request.getAuthType();
        }
    }

    @Override
    public String getRemoteAddr() {
        HttpServletRequest request = (HttpServletRequest) getRequest();
        String headerValue;

        headerValue = request.getHeader(headerPrefix + "REMOTE_ADDR");
        if (headerValue != null) {
            return headerValue;
        } else {
            return request.getRemoteAddr();
        }
    }

    @Override
    public String getRemoteHost() {
        HttpServletRequest request = (HttpServletRequest) getRequest();
        String headerValue;

        headerValue = request.getHeader(headerPrefix + "REMOTE_HOST");
        if (headerValue != null) {
            return headerValue;
        } else {
            return request.getRemoteHost();
        }
    }

    @Override
    public int getRemotePort() {
        HttpServletRequest request = (HttpServletRequest) getRequest();
        String headerValue;

        headerValue = request.getHeader(headerPrefix + "REMOTE_PORT");
        if (headerValue != null) {
            return Integer.parseInt(headerValue);
        } else {
            return request.getRemotePort();
        }
    }

}

/**
 * Populate HttpRequestServlet API data from HTTP extension headers.
 *
 * When SSSD is used for authentication and identity lookup those actions occur
 * in an Apache HTTP server which is fronting the servlet container. After
 * successful authentication Apache will proxy the request to the container
 * along with additional authentication and identity metadata.
 *
 * The preferred way to transport the metadata and have it appear seamlessly in
 * the servlet API is via the AJP protocol. However AJP may not be available or
 * desirable. An alternative method is to transport the metadata in extension
 * HTTP headers. However we still want the standard servlet request API methods
 * to work. Another way to say this is we do not want upper layers to be aware
 * of the transport mechanism. To achieve this we wrap the HttpServletRequest
 * class and override specific methods which need to extract the data from the
 * extension HTTP headers. (This is roughly equivalent to what happens when AJP
 * is implemented natively in the container).
 *
 * The extension HTTP headers are identified by the prefix "X-SSSD-". The
 * overridden methods check for the existence of the appropriate extension
 * header and if present returns the value found in the extension header,
 * otherwise it returns the value from the method it's wrapping.
 *
 */
public class SssdFilter implements Filter {
    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            SssdHeadersRequest request = new SssdHeadersRequest(httpServletRequest);
            filterChain.doFilter(request, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
