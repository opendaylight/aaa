/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sssd;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.ClaimAuth;
import org.osgi.framework.BundleContext;

@Deprecated
public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        manager.add(createComponent().setInterface(new String[] { ClaimAuth.class.getName() }, null)
                                     .setImplementation(SssdClaimAuth.class));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
