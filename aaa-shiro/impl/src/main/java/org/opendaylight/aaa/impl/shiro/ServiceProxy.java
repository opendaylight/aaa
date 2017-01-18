/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro;

import org.opendaylight.aaa.shiro.filters.AAAFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for enabling and disabling the AAA service. By default, the
 * service is disabled; the AAAFilter will not require AuthN or AuthZ. The
 * service is enabled through calling
 * <code>ServiceProxy.getInstance().setEnabled(true)</code>. AuthN and AuthZ are
 * disabled by default in order to support workflows such as the feature
 * <code>odl-restconf-noauth</code>.
 *
 * The AAA service is enabled through installing the <code>odl-aaa-shiro</code>
 * feature. The <code>org.opendaylight.aaa.shiroact.Activator()</code>
 * constructor calls enables AAA through the ServiceProxy, which in turn enables
 * the AAAFilter.
 *
 * ServiceProxy is a singleton; access to the ServiceProxy is granted through
 * the <code>getInstance()</code> function.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <a
 *      href="https://github.com/opendaylight/netconf/blob/master/opendaylight/restconf/sal-rest-connector/src/main/resources/WEB-INF/web.xml">resconf
 *      web,xml</a>
 * @see <code>org.opendaylight.aaa.shiro.Activator</code>
 * @see <code>org.opendaylight.aaa.shiro.filters.AAAFilter</code>
 */
public class ServiceProxy {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProxy.class);

    private static ServiceProxy instance = new ServiceProxy();
    private volatile boolean enabled = false;
    private AAAFilter filter;

    /**
     * private for singleton pattern
     */
    private ServiceProxy() {
        final String INFO_MESSAGE = "Creating the ServiceProxy";
        LOG.info(INFO_MESSAGE);
    }

    /**
     * @return ServiceProxy, a feature level singleton
     */
    public static ServiceProxy getInstance() {
        return instance;
    }

    /**
     * Enables/disables the feature, cascading the state information to the
     * AAAFilter.
     *
     * @param enabled A flag indicating whether to enable the Service.
     */
    public synchronized void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        LOG.info("Setting ServiceProxy enabled to {}", enabled);
        // check for null because of non-determinism in bundle load
        if (filter != null) {
            filter.setEnabled(enabled);
        }
    }

    /**
     * Extract whether the service is enabled.
     *
     * @param filter
     *            register an optional Filter for callback if enable state
     *            changes
     * @return Whether the service is enabled
     */
    public synchronized boolean getEnabled(final AAAFilter filter) {
        this.filter = filter;
        return enabled;
    }
}
