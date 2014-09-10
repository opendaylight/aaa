/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.Dictionary;
import java.util.Hashtable;

import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * An {@link InheritableThreadLocal}-based {@link AuthenticationService}.
 *
 * @author liemmn
 */
public class AuthenticationManager implements AuthenticationService,
        ManagedService {
    private static final String AUTH_ENABLED_ERR = "Error setting authEnabled";

    static final String AUTH_ENABLED = "authEnabled";
    static final Dictionary<String, String> defaults = new Hashtable<>();
    static {
        defaults.put(AUTH_ENABLED, Boolean.FALSE.toString());
    }

    // In non-Karaf environments, authEnabled is set to false by default
    private static volatile boolean authEnabled = false;

    private final static AuthenticationManager am = new AuthenticationManager();
    private final ThreadLocal<Authentication> auth = new InheritableThreadLocal<>();

    private AuthenticationManager() {
    }

    static AuthenticationManager instance() {
        return am;
    }

    @Override
    public Authentication get() {
        return auth.get();
    }

    @Override
    public void set(Authentication a) {
        auth.set(a);
    }

    @Override
    public void clear() {
        auth.remove();
    }

    @Override
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    @Override
    public void updated(Dictionary<String, ?> properties)
            throws ConfigurationException {
        try {
            authEnabled = Boolean.valueOf((String) properties.get(AUTH_ENABLED));
        } catch (Throwable t) {
            throw new ConfigurationException(AUTH_ENABLED, AUTH_ENABLED_ERR);
        }
    }

}
