/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

/**
 * An interface for direct authentication with some given credentials.
 *
 * @author liemmn
 */
public interface CredentialAuth<T extends Credentials> {

    /**
     * Authenticate a claim with the given credentials and domain scope.
     *
     * @param cred credentials
     * @param domain domain name, or null if not known
     * @throws AuthenticationException if failed authentication
     * @return authenticated claim
     */
    Claim authenticate(T cred, String domain) throws AuthenticationException;
}
