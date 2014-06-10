/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;

/**
 * An {@link InheritableThreadLocal}-based {@link AuthenticationService}.
 *
 * @author liemmn
 */
public class AuthenticationManager implements AuthenticationService {
    private final static AuthenticationManager am = new AuthenticationManager();
    private final ThreadLocal<Authentication> auth = new InheritableThreadLocal<>();

    private AuthenticationManager() {}

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

}
