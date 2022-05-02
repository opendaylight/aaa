/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.moon;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(value = "/moon", name = "MoonTokenEndpoint")
@HttpWhiteboardServletPattern("/moon")
@HttpWhiteboardServletName("MoonTokenEndpoint")
@RequireHttpWhiteboard
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE)
public final class MoonTokenEndpoint extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(MoonTokenEndpoint.class);
    private static final long serialVersionUID = 4980356362831585417L;

    public MoonTokenEndpoint() {
        // required public constructor
    }

    @Override
    public String getServletName() {
        return MoonTokenEndpoint.class.getSimpleName();
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        LOG.info("MoonTokenEndpoint Servlet doPost: {} {}", req.getServletPath(), req.getRequestURI());
    }
}
