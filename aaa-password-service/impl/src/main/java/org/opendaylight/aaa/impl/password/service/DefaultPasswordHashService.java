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

    //TODO: found in SimpleHashProvider.Parameters.PARAMETER_ITERATIONS
    private static final String ITERATIONS = "SimpleHash.iterations";

    private final DefaultHashService hashService;
    private final Integer numIterations;
    private final String privateSalt;

    public DefaultPasswordHashService() {
        this(new PasswordServiceConfigBuilder().build());
    }

    public DefaultPasswordHashService(final PasswordServiceConfig passwordServiceConfig) {
        if (passwordServiceConfig.getIterations() != null) {
            numIterations = passwordServiceConfig.getIterations();
            LOG.info("DefaultPasswordHashService will utilize configured iteration count={}", numIterations);
        } else {
            numIterations = DEFAULT_NUM_ITERATIONS;
            LOG.info("DefaultPasswordHashService will utilize default iteration count={}", DEFAULT_NUM_ITERATIONS);
        }
        if (passwordServiceConfig.getPrivateSalt() != null) {
            privateSalt = passwordServiceConfig.getPrivateSalt();
            LOG.info("DefaultPasswordHashService will utilize a configured private salt");
        } else {
            privateSalt = null;
            LOG.info("DefaultPasswordHashService will not utilize a private salt, since none was configured");
        }

        hashService = createHashService(passwordServiceConfig.getAlgorithm());
    }

    @Override
    public PasswordHash getPasswordHash(final String password) {
        final var requestBuilder = new HashRequest.Builder();
        if (privateSalt != null) {
            requestBuilder.setSalt(privateSalt);
        }
        final var hash =  hashService.computeHash(requestBuilder
            .setAlgorithmName(hashService.getDefaultAlgorithmName())
            .addParameter(ITERATIONS, numIterations)
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
        final var hash =  hashService.computeHash(new HashRequest.Builder()
            .setAlgorithmName(hashService.getDefaultAlgorithmName())
            .addParameter(ITERATIONS, numIterations)
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
