/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.framework.BundleContext;

/**
 * An activator for the secure token server to inject in a
 * {@link CredentialAuth} implementation.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager)
            throws Exception {
        manager.add(createComponent()
                .setImplementation(ServiceLocator.INSTANCE)
                .add(createServiceDependency().setService(CredentialAuth.class)
                  .setRequired(true))
                .add(createServiceDependency().setService(ClaimAuth.class)
                        .setRequired(false)
                        .setCallbacks("claimAuthAdded", "claimAuthRemoved"))
                .add(createServiceDependency().setService(TokenAuth.class)
                        .setRequired(false)
                        .setCallbacks("tokenAuthAdded", "tokenAuthRemoved"))
                .add(createServiceDependency().setService(TokenStore.class)
                        .setRequired(true)
                        .setCallbacks("tokenStoreAdded", "tokenStoreRemoved"))
                .add(createServiceDependency().setService(TokenStore.class)
                  .setRequired(true))
                .add(createServiceDependency().setService(
                        AuthenticationService.class).setRequired(true))
                .add(createServiceDependency().setService(IdMService.class)
                        .setRequired(true))
                .add(createServiceDependency().setService(ClientService.class)
                        .setRequired(true)));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }
}
