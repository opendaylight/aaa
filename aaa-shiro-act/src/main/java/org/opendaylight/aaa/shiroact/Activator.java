/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiroact;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.opendaylight.aaa.shiro.ServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for activating the aaa-shiro-act bundle. This bundle is primarily
 * responsible for enabling AuthN and AuthZ. If this bundle is not installed,
 * then AuthN and AuthZ will not take effect.
 *
 * To ensure that the AAA is enabled for your feature, make sure to include the
 * <code>odl-aaa-shiro</code> feature in your feature definition.
 *
 * Offers contextual <code>DEBUG</code> level clues concerning the activation of
 * the <code>aaa-shiro-act</code> bundle. To enable the enhanced debugging issue
 * the following line in the karaf shell:
 * <code>log:set debug org.opendaylight.aaa.shiroact.Activator</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void stop(BundleContext context) throws Exception {
        final String DEBUG_MESSAGE = "Destroying the aaa-shiro-act bundle";
        LOG.debug(DEBUG_MESSAGE);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        final String DEBUG_MESSAGE = "Initializing the aaa-shiro-act bundle";
        LOG.debug(DEBUG_MESSAGE);
        ServiceProxy.getInstance().setEnabled(true);
    }

}
