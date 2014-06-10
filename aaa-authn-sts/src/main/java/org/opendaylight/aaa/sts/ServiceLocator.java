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
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * @author liemmn
 *
 */
public enum ServiceLocator {
    INSTANCE;

    volatile ClaimAuth ca;

    volatile CredentialAuth<PasswordCredentials> da;

    volatile TokenStore ts;

    volatile AuthenticationService as;

}
