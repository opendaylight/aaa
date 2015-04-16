/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;

import java.util.LinkedList;
import java.util.List;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * @author liemmn
 *
 */
public enum ServiceLocator {
    INSTANCE;

    volatile List<TokenAuth> ta = new LinkedList<>();

    volatile CredentialAuth<PasswordCredentials> da;

    volatile TokenStore ts;

    volatile AuthenticationService as;

    volatile IdMService is;

    volatile ClientService cs;

    protected void tokenAuthAdded(TokenAuth ta) {
        this.ta.add(ta);
    }

    protected void tokenAuthRemoved(TokenAuth ta) {
        this.ta.remove(ta);
    }
    protected void tokenStoreAdded(TokenStore ts) {
        this.ts = ts;
    }

    protected void tokenStoreRemoved(TokenStore ts) {
        this.ts = null;
    }
}
