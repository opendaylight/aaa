/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.password.service;

/**
 * Service responsible for generating and comparing passwords using one-way hashing.
 */
public interface PasswordService {

    /**
     * Extract a hashed password using a randomly generated salt.
     *
     * @param password a plaintext password
     * @return the result of hashing the password
     */
    PasswordHash getHashedPassword(String password);

    /**
     * Extract a hashed password using an input salt.
     *
     * @param password a plaintext password
     * @param salt the hash for <code>password</code>
     * @return the result of hashing the password
     */
    PasswordHash getHashedPassword(String password, String salt);

    /**
     * Password comparison.
     *
     * @param plaintext the &quotinput&quot password in plaintext
     * @param stored the Base64-encoded stored password
     * @param salt the salt used to originally encode <code>stored</code>
     * @return
     */
    boolean passwordsMatch(String plaintext, String stored, String salt);
}
