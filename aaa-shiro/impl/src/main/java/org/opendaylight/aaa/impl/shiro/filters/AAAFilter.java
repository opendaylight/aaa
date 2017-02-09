/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import org.apache.shiro.web.servlet.ShiroFilter;
import org.opendaylight.aaa.shiro.ServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RESTCONF AAA JAX-RS 1.X Web Filter. This class is also responsible for
 * delivering debug information; to enable these debug statements, please issue
 * the following in the karaf shell:
 *
 * <code>log:set DEBUG org.opendaylight.aaa.shiro.filters.AAAFilter</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <code>javax.servlet.Filter</code>
 * @see <code>org.apache.shiro.web.servlet.ShiroFilter</code>
 */
public class AAAFilter extends ShiroFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AAAFilter.class);

    public AAAFilter() {
        LOG.debug("Creating the AAAFilter");
    }

    /*
     * (non-Javadoc)
     *
     * Adds context clues that aid in debugging. Also initializes the enable
     * status to correspond with
     * <code>ServiceProxy.getInstance.getEnabled()</code>.
     *
     * @see org.apache.shiro.web.servlet.ShiroFilter#init()
     */
    @Override
    public void init() throws Exception {
        super.init();
        LOG.debug("Initializing the AAAFilter");
        // sets the filter to the startup value. Because of non-determinism in
        // bundle loading, this passes an instance of itself along so that if
        // the
        // enable status changes, then AAAFilter enable status is changed.
        setEnabled(ServiceProxy.getInstance().getEnabled(this));
    }

    /*
     * (non-Javadoc)
     *
     * Adds context clues to aid in debugging whether the filter is enabled.
     *
     * @see
     * org.apache.shiro.web.servlet.OncePerRequestFilter#setEnabled(boolean)
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        LOG.debug("Setting AAAFilter enabled to {}", enabled);
    }
}
