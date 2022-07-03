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

/**
 * Extensible filters for {@link IdmLightApplication}.
 */
@WebFilter(urlPatterns = "/auth/*", filterName = "IdmLightCustomFilterAdapter")
@HttpWhiteboardFilterAsyncSupported
@HttpWhiteboardFilterPattern("/auth/*")
@HttpWhiteboardFilterName("IdmLightCustomFilterAdapter")
@Component(immediate = true, service = Filter.class)
public final class IdmLightCustomFilterAdapter extends CustomFilterAdapter {
    @Activate
    public IdmLightCustomFilterAdapter(@Reference final CustomFilterAdapterConfiguration configuration) {
        super(configuration);
    }
}
