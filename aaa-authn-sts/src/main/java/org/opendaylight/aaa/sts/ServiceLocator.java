/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * @author liemmn
 *
 */
public enum ServiceLocator {
    INSTANCE;

    volatile List<ClaimAuth> ca = new LinkedList<>();

    volatile List<TokenAuth> ta = new LinkedList<>();

    volatile CredentialAuth<PasswordCredentials> da;

    volatile TokenStore ts;

    volatile AuthenticationService as;

    volatile IdMService is;

    protected void claimAuthAdded(ClaimAuth ca) {
        this.ca.add(ca);
    }

    protected void claimAuthRemoved(ClaimAuth ca) {
        this.ca.remove(ca);
    }

    protected void tokenAuthAdded(TokenAuth ta) {
        this.ta.add(ta);
    }

    protected void tokenAuthRemoved(TokenAuth ta) {
        this.ta.remove(ta);
    }

}
