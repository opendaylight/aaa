/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator for <code>aaa-filterchain</code>, a bundle which provides the ability
 * to inject custom <code>Filter</code>(s) in front of servlets.
 *
 * <p>
 * This class is also responsible for offering contextual <code>DEBUG</code>
 * level clues concerning the activation of the <code>aaa-filterchain</code> bundle.
 * To enable these debug messages, issue the following command in the karaf
 * shell: <code>log:set debug org.opendaylight.aaa.filterchain.Activator</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class Activator extends DependencyActivatorBase {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<ManagedService> managedServiceServiceRegistration;

    @Override
    public void destroy(final BundleContext bc, final DependencyManager dm) throws Exception {
        LOG.debug("Destroying the aaa-filterchain bundle");
        managedServiceServiceRegistration.unregister();
    }

    @Override
    public void init(final BundleContext bc, final DependencyManager dm) throws Exception {
        LOG.debug("Initializing the aaa-filterchain bundle");
        // Register the CustomFilterAdapterConfiguration ManagedService with the
        // BundleContext so config values can be loaded from the config admin
        managedServiceServiceRegistration = bc.registerService(ManagedService.class,
                CustomFilterAdapterConfiguration.getInstance(),
                CustomFilterAdapterConfiguration.getInstance().getDefaultProperties());
    }
}
