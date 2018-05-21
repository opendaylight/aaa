/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.password.service;

import com.google.common.annotations.Beta;

/**
 * Service responsible for generating salts and comparing passwords.  This service is designed for one-way hashing only.
 */
@Beta
public interface PasswordHashService {

    /**
     * Extract a hashed password using a randomly generated salt.
     *
     * @param password a plaintext password
     * @return the result of hashing the password
     */
    PasswordHash getPasswordHash(String password);

    /**
     * Extract a hashed password using an input salt.
     *
     * @param password a plaintext password
     * @param salt the hash for <code>password</code>
     * @return the result of hashing the password
     */
    PasswordHash getPasswordHash(String password, String salt);

    /**
     * Password comparison.
     *
     * @param plaintext the &quot;input&quot; password in plaintext
     * @param stored the Base64-encoded stored password
     * @param salt the salt used to originally encode <code>stored</code>
     * @return whether or not the passwords match
     */
    boolean passwordsMatch(String plaintext, String stored, String salt);
}
