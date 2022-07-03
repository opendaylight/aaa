/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.web.servlet.ForwardingServlet;
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

/**
 * Bridge between {@link IdmLightApplication} and HTTP Whiteboard.
 */
@WebServlet(value = "/auth", name = "IdmLightServlet")
@HttpWhiteboardServletPattern("/auth")
@HttpWhiteboardServletName("IdmLightServlet")
@Component(immediate = true, service = Servlet.class)
public final class IdmLightServlet extends ForwardingServlet {
    @Activate
    public IdmLightServlet(@Reference final ServletSupport servletSupport, @Reference final ClaimCache claimCache,
            @Reference final IIDMStore iidMStore) {
        super(servletSupport.createHttpServletBuilder(new IdmLightApplication(iidMStore, claimCache)).build());
    }
}
