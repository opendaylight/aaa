/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.lang.codec.Base64;
import org.apache.shiro.lang.util.ByteSource;
import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPasswordHashService implements PasswordHashService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPasswordHashService.class);

    public static final String DEFAULT_HASH_ALGORITHM = "SHA-512";
    public static final int DEFAULT_NUM_ITERATIONS = 20000;

    // Taken from shiro.crypto.hash.SimpleHashProvider.Parameters as it is not public but used to get parameters
    // Shiro 2.0.0 introduced parameters for hash request. These parameters may differ depending on algorithm used
    // propagation of iterations and private salt do not work for Argon2 and BCrypt and default values will be used
    private static final String PARAMETER_ITERATIONS = "SimpleHash.iterations";
    private static final String PARAMETER_SECRET_SALT = "SimpleHash.secretSalt";

    private final DefaultHashService hashService;
    private final Integer numIterations;
    private final String privateSalt;

    public DefaultPasswordHashService() {
        this(new PasswordServiceConfigBuilder().build());
    }

    public DefaultPasswordHashService(final PasswordServiceConfig passwordServiceConfig) {
        final var iter = passwordServiceConfig.getIterations();
        if (iter != null) {
            numIterations = iter;
            LOG.info("DefaultPasswordHashService will utilize configured iteration count={}", numIterations);
        } else {
            numIterations = DEFAULT_NUM_ITERATIONS;
            LOG.info("DefaultPasswordHashService will utilize default iteration count={}", numIterations);
        }
        privateSalt = passwordServiceConfig.getPrivateSalt();
        if (privateSalt != null) {
            LOG.info("DefaultPasswordHashService will utilize a configured private salt");
        } else {
            LOG.info("DefaultPasswordHashService will not utilize a private salt, since none was configured");
        }

        hashService = createHashService(passwordServiceConfig.getAlgorithm());
    }

    @Override
    public PasswordHash getPasswordHash(final String password) {
        final var requestBuilder = new HashRequest.Builder();
        if (privateSalt != null) {
            requestBuilder.addParameter(PARAMETER_SECRET_SALT, privateSalt);
        }
        final var hash =  hashService.computeHash(requestBuilder
            .setAlgorithmName(hashService.getDefaultAlgorithmName())
            .addParameter(PARAMETER_ITERATIONS, numIterations)
            .setSource(ByteSource.Util.bytes(password))
            .build());
        return PasswordHashImpl.create(
            hash.getAlgorithmName(),
            hash.getSalt().toBase64(),
            hash.getIterations(),
            hash.toBase64());
    }

    @Override
    public PasswordHash getPasswordHash(final String password, final String salt) {
        final var requestBuilder = new HashRequest.Builder();
        if (privateSalt != null) {
            requestBuilder.addParameter(PARAMETER_SECRET_SALT, privateSalt);
        }
        final var hash =  hashService.computeHash(requestBuilder
            .setAlgorithmName(hashService.getDefaultAlgorithmName())
            .addParameter(PARAMETER_ITERATIONS, numIterations)
            .setSource(ByteSource.Util.bytes(password))
            .setSalt(ByteSource.Util.bytes(Base64.decode(salt)))
            .build());
        return PasswordHashImpl.create(
            hash.getAlgorithmName(),
            hash.getSalt().toBase64(),
            hash.getIterations(),
            hash.toBase64());
    }

    @Override
    public boolean passwordsMatch(final String plaintext, final String stored, final String salt) {
        return getPasswordHash(plaintext, salt).getHashedPassword().equals(stored);
    }

    private static DefaultHashService createHashService(final String hashAlgorithm) {
        final DefaultHashService hashService = new DefaultHashService();

        if (hashAlgorithm != null) {
            hashService.setDefaultAlgorithmName(hashAlgorithm);
            LOG.info("DefaultPasswordHashService will utilize configured algorithm={}", hashAlgorithm);
        } else {
            hashService.setDefaultAlgorithmName(DEFAULT_HASH_ALGORITHM);
            LOG.info("DefaultPasswordHashService will utilize default algorithm={}", DEFAULT_HASH_ALGORITHM);
        }

        return hashService;
    }
}
