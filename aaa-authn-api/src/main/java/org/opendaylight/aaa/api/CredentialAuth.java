/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
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
     * @param cred
     *            credentials
     * @return authenticated claim
     * @throws AuthenticationException
     *             if failed authentication
     */
    Claim authenticate(T cred) throws AuthenticationException;
}
