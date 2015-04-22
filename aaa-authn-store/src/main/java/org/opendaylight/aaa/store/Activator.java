/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.store;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;

/**
 * An activator for the default datastore implementation of {@link TokenStore}.
 *
 * @author liemmn
 */
public class Activator extends DependencyActivatorBase {

    private static final String TOKEN_PID = "org.opendaylight.aaa.tokens";

    @Override
    public void init(BundleContext context, DependencyManager manager)
        throws Exception {
        DefaultTokenStore ts = new DefaultTokenStore();
        manager.add(createComponent().setInterface(
            new String[]{TokenStore.class.getName()}, null)
            .setImplementation(ts));
        context.registerService(ManagedService.class.getName(), ts,
            addPid(DefaultTokenStore.defaults));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
        throws Exception {
    }

    private Dictionary<String, ?> addPid(Dictionary<String, String> dict) {
        dict.put(Constants.SERVICE_PID, TOKEN_PID);
        return dict;
    }
}
