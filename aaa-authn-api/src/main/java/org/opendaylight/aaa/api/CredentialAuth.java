/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An interface for direct authentication with some given credentials. Note this interface is not type-safe.
 *
 * @author liemmn
 */
public interface CredentialAuth<T extends Credentials> {
    /**
     * Authenticate a claim with the given credentials and domain scope.
     *
     * @param cred credentials
     * @return authenticated claim
     * @throws AuthenticationException if failed authentication
     * @throws NullPointerException if credentials are null
     */
    Claim authenticate(T cred) throws AuthenticationException;

    /**
     * Return the credential class that is required by this services. This acts as a type check allowing discovery
     * of the type at runtime.
     *
     * <p>
     * Note: this method should be defined in subclasses specializations for a particular credential class as a default
     *       (in case of an interface) or a final (in case of a class) method.
     *
     * @return Required credential class
     */
    @NonNull Class<T> credentialClass();
}
