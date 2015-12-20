/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.basic;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.TokenAuth;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        manager.add(createComponent()
                .setInterface(new String[] { TokenAuth.class.getName() }, null)
                .setImplementation(HttpBasicAuth.class)
                .add(createServiceDependency().setService(CredentialAuth.class).setRequired(true)));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
