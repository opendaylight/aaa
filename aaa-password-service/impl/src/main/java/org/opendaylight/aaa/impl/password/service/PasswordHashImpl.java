/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import org.opendaylight.aaa.api.password.service.PasswordHash;

public final class PasswordHashImpl implements PasswordHash {
    private final String algorithmName;
    private final String salt;
    private final int iterations;
    private final String hashedPassword;

    private PasswordHashImpl(final String algorithmName, final String salt, final int iterations,
                             final String hashedPassword) {
        this.algorithmName = algorithmName;
        this.salt = salt;
        this.iterations = iterations;
        this.hashedPassword = hashedPassword;
    }

    public static PasswordHash create(final String algorithmName, final String salt, final int iterations,
                                      final String hashedPassword) {

        return new PasswordHashImpl(algorithmName, salt, iterations, hashedPassword);
    }

    @Override
    public String getAlgorithmName() {
        return algorithmName;
    }

    @Override
    public String getSalt() {
        return salt;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    @Override
    public String getHashedPassword() {
        return hashedPassword;
    }
}