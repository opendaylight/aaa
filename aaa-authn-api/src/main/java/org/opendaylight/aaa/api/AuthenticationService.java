/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

/**
 * Authentication service to provide authentication context.
 */
public interface AuthenticationService {
    /**
     * Retrieve the current security context, or null if none exists.
     *
     * @return security context
     */
    Authentication get();

    /**
     * Set the current security context. Only {@link TokenAuth} should set
     * security context based on the authentication result.
     *
     * @param auth
     *            security context
     */
    void set(Authentication auth);

    /**
     * Clear the current security context.
     */
    void clear();

    /**
     * Checks to see if authentication is enabled.
     *
     * @return true if it is, false otherwise
     */
    boolean isAuthEnabled();
}
