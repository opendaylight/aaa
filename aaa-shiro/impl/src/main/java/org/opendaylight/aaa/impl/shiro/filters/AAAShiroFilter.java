/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import org.apache.shiro.web.servlet.ShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default AAA JAX-RS 1.X Web Filter.  Unlike AAAFilter, which is aimed towards
 * supporting RESTCONF and its existing API mechanisms, AAAShiroFilter is a generic
 * <code>ShiroFilter</code> for use with any other ODL Servlets.  The main difference
 * is that <code>AAAFilter</code> was designed to support the existing noauth
 * mechanism, while this filter cannot be disabled.
 *
 * This class is also responsible for delivering debug information; to enable these
 * debug statements, please issue the following in the karaf shell:
 *
 * <code>log:set DEBUG org.opendaylight.aaa.shiro.filters.AAAShiroFilter</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <code>javax.servlet.Filter</code>
 * @see <code>org.apache.shiro.web.servlet.ShiroFilter</code>
 */
public class AAAShiroFilter extends ShiroFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroFilter.class);

    public AAAShiroFilter() {
        LOG.debug("Creating the AAAShiroFilter");
    }

    /*
     * (non-Javadoc)
     *
     * Adds context clues that aid in debugging.
     *
     * @see org.apache.shiro.web.servlet.ShiroFilter#init()
     */
    @Override
    public void init() throws Exception {
        super.init();
        LOG.debug("Initializing the AAAShiroFilter");
    }
}
