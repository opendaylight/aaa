/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default AAA JAX-RS 1.X Web Filter.  Unlike AAAFilter, which is aimed towards
 * supporting RESTCONF and its existing API mechanisms, AAAShiroFilter is a generic
 * <code>ShiroFilter</code> for use with any other ODL Servlets.  The main difference
 * is that <code>AAAFilter</code> was designed to support the existing noauth
 * mechanism, while this filter cannot be disabled.
 *
 * <p>
 * This class is also responsible for delivering debug information; to enable these
 * debug statements, please issue the following in the karaf shell:
 *
 * <code>log:set DEBUG AAAShiroFilter</code>
 *
 * @see javax.servlet.Filter
 * @see ShiroFilter
 */
@WebFilter(urlPatterns = "/*", filterName = "AAAShiroFilter")
@HttpWhiteboardFilterAsyncSupported
@HttpWhiteboardFilterPattern("/*")
@HttpWhiteboardFilterName("AAAShiroFilter")
@RequireHttpWhiteboard
@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE)
public final class AAAShiroFilter extends ShiroFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroFilter.class);

    public AAAShiroFilter() {
        LOG.debug("Creating the AAAShiroFilter");
    }

    /*
     * (non-Javadoc)
     *
     * Adds context clues that aid in debugging.
     *
     * @see ShiroFilter#init()
     */
    @Override
    public void init() throws Exception {
        super.init();
        LOG.debug("AAAShiroFilter initialized");
    }
}
