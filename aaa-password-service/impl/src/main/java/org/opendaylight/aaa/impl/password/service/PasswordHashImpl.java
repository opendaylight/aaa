package org.opendaylight.aaa.impl.password.service;

import org.opendaylight.aaa.api.password.service.PasswordHash;

public class PasswordHashImpl implements PasswordHash {

    private final String algorithmName;
    private final String salt;
    private final int iterations;
    private final String hashedPassword;

    private PasswordHashImpl(final String algorithmName, final String salt, final int iterations, final String hashedPassword) {
        this.algorithmName = algorithmName;
        this.salt = salt;
        this.iterations = iterations;
        this.hashedPassword = hashedPassword;
    }

    public static PasswordHash create(final String algorithmName, final String salt, final int iterations, final String hashedPassword) {
        return new PasswordHashImpl(algorithmName, salt, iterations, hashedPassword);
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }

    public String getSalt() {
        return this.salt;
    }

    public int getIterations() {
        return this.iterations;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }
}