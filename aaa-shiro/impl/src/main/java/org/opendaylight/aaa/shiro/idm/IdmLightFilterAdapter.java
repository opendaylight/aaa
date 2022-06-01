/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.opendaylight.aaa.filterchain.filters.CustomFilterAdapter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterPattern;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;

// Allows user to add javax.servlet.Filter(s) in front of REST services
@WebFilter(urlPatterns = "/*", filterName = "IdmLightFilterAdapter")
@HttpWhiteboardFilterAsyncSupported
@HttpWhiteboardFilterPattern("/*")
@HttpWhiteboardFilterName("IdmLightFilterAdapter")
@JaxrsApplicationSelect("/auth")
@Component(immediate = true, service = Filter.class)
public class IdmLightFilterAdapter extends CustomFilterAdapter {
    @Activate
    public IdmLightFilterAdapter(@Reference final CustomFilterAdapterConfiguration customFilterAdapterConfig) {
        super(customFilterAdapterConfig);
    }
}
