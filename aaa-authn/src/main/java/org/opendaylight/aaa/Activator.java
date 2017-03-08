/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.Dictionary;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClientService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

/**
 * Activator to register {@link AuthenticationService} with OSGi.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {

    private static final String AUTHN_PID = "org.opendaylight.aaa.authn";

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        manager.add(createComponent().setInterface(
                new String[] { AuthenticationService.class.getName() }, null).setImplementation(
                AuthenticationManager.instance()));

        ClientManager cm = new ClientManager();
        manager.add(createComponent().setInterface(new String[] { ClientService.class.getName() },
                null).setImplementation(cm));
        context.registerService(ManagedService.class.getName(), cm, addPid(ClientManager.DEFAULTS));
        context.registerService(ManagedService.class.getName(), AuthenticationManager.instance(),
                addPid(AuthenticationManager.DEFAULTS));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

    private Dictionary<String, ?> addPid(Dictionary<String, String> dict) {
        dict.put(Constants.SERVICE_PID, AUTHN_PID);
        return dict;
    }
}
