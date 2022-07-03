/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Abstract base class for setting up Servlets which forward requests to a backing Servlet. This is useful for bridging
 * servlets created via {@link ServletSupport} to dynamic environments, such as OSGi HTTP Whiteboard.
 */
@Beta
public abstract class ForwardingServlet implements Servlet {
    private final Servlet delegate;

    protected ForwardingServlet(final Servlet delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final void init(final ServletConfig config) throws ServletException {
        delegate.init(config);
    }

    @Override
    public final ServletConfig getServletConfig() {
        return delegate.getServletConfig();
    }

    @Override
    public final void service(final ServletRequest req, final ServletResponse res)
            throws ServletException, IOException {
        delegate.service(req, res);
    }

    @Override
    public final String getServletInfo() {
        return delegate.getServletInfo();
    }

    @Override
    public final void destroy() {
        delegate.destroy();
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
    }
}
