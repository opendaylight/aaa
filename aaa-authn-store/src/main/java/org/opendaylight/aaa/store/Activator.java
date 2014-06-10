/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.store;

import static org.opendaylight.aaa.store.DefaultTokenStore.MAX_CACHED;
import static org.opendaylight.aaa.store.DefaultTokenStore.SECS_TO_IDLE;
import static org.opendaylight.aaa.store.DefaultTokenStore.SECS_TO_LIVE;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;

/**
 * An activator for the default datastore implementation of {@link TokenStore}.
 *
 * @author liemmn
 *
 */
public class Activator extends ComponentActivatorAbstractBase {
    // Defaults
    private static final int maxCachedTokens = 8192;
    private static final int secondsToLive = 3600;
    private static final int secondsToIdle = 3600;

    @Override
    public Object[] getImplementations() {
        Object[] res = { DefaultTokenStore.class };
        return res;
    }

    @Override
    public void configureInstance(Component c, Object imp, String container) {
        if (imp.equals(DefaultTokenStore.class)) {
            Dictionary<String, String> props = new Hashtable<>();
            props.put(MAX_CACHED, Integer.toString(maxCachedTokens));
            props.put(SECS_TO_IDLE, Integer.toString(secondsToIdle));
            props.put(SECS_TO_LIVE, Integer.toString(secondsToLive));

            c.setInterface(TokenStore.class.getName(), props);
        }
    }
}
