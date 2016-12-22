/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import java.util.Dictionary;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

/**
 * An activator for the secure token server to inject in a
 * <code>CredentialAuth</code> implementation.
 *
 * @author liemmn
 *
 */
@Deprecated
public class Activator extends DependencyActivatorBase {
    private static final String FEDERATION_PID = "org.opendaylight.aaa.federation";

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        manager.add(createComponent()
                .setImplementation(ServiceLocator.getInstance())
                .add(createServiceDependency().setService(TokenStore.class).setRequired(true))
                .add(createServiceDependency().setService(IdMService.class).setRequired(true))
                .add(createServiceDependency().setService(ClaimAuth.class).setRequired(false)
                        .setCallbacks("claimAuthAdded", "claimAuthRemoved")));
        context.registerService(ManagedService.class, FederationConfiguration.instance(),
                addPid(FederationConfiguration.defaults));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

    private Dictionary<String, ?> addPid(Dictionary<String, String> dict) {
        dict.put(Constants.SERVICE_PID, FEDERATION_PID);
        return dict;
    }
}
