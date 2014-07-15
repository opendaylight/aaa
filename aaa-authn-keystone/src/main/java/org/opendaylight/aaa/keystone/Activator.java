/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.keystone;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.TokenAuth;
import org.osgi.framework.BundleContext;

/**
 * An activator for {@link KeystoneTokenAuth}.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager)
            throws Exception {
        manager.add(createComponent().setInterface(
                new String[] { TokenAuth.class.getName() }, null)
                .setImplementation(KeystoneTokenAuth.class));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }

}
