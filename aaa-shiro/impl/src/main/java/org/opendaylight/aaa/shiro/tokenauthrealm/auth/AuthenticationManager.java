/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.tokenauthrealm.auth;

import java.util.Dictionary;
import java.util.Hashtable;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;

/**
 * An {@link InheritableThreadLocal}-based {@link AuthenticationService}.
 *
 * @author liemmn
 */
public class AuthenticationManager implements AuthenticationService {
    private static final String AUTH_ENABLED_ERR = "Error setting authEnabled";

    protected static final String AUTH_ENABLED = "authEnabled";
    protected static final Dictionary<String, String> DEFAULTS = new Hashtable<>();

    static {
        DEFAULTS.put(AUTH_ENABLED, Boolean.FALSE.toString());
    }

    // In non-Karaf environments, authEnabled is set to false by default
    private static volatile boolean authEnabled = false;

    private static final AuthenticationManager AUTHENTICATION_MANAGER = new AuthenticationManager();
    private final ThreadLocal<Authentication> auth = new InheritableThreadLocal<>();

    private AuthenticationManager() {
    }

    public static AuthenticationManager instance() {
        return AUTHENTICATION_MANAGER;
    }

    @Override
    public Authentication get() {
        return auth.get();
    }

    @Override
    public void set(final Authentication authentication) {
        auth.set(authentication);
    }

    @Override
    public void clear() {
        auth.remove();
    }

    @Override
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void updated(final Dictionary<String, ?> properties) {
        if (properties == null) {
            return;
        }

        final String propertyValue = (String) properties.get(AUTH_ENABLED);
        final boolean isTrueString = Boolean.parseBoolean(propertyValue);
        if (!isTrueString && !"false".equalsIgnoreCase(propertyValue)) {
            throw new IllegalArgumentException(AUTH_ENABLED_ERR);
        }
        authEnabled = isTrueString;
    }
}
