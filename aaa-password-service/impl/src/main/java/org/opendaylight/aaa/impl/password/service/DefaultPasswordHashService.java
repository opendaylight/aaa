/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.SimpleHashRequest;
import org.apache.shiro.util.ByteSource;
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

    private final DefaultHashService hashService;

    public DefaultPasswordHashService() {
        this(new PasswordServiceConfigBuilder().build());
    }

    public DefaultPasswordHashService(final PasswordServiceConfig passwordServiceConfig) {
        hashService = createHashService(passwordServiceConfig.getIterations(), passwordServiceConfig.getAlgorithm(),
            passwordServiceConfig.getPrivateSalt());
    }

    @Override
    public PasswordHash getPasswordHash(final String password) {
        final var hash =  hashService.computeHash(new HashRequest.Builder()
            .setAlgorithmName(hashService.getHashAlgorithmName())
            .setIterations(hashService.getHashIterations())
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
        final var hash = hashService.computeHash(new SimpleHashRequest(
            hashService.getHashAlgorithmName(),
            ByteSource.Util.bytes(password),
            ByteSource.Util.bytes(Base64.decode(salt)),
            hashService.getHashIterations()));
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

    private static DefaultHashService createHashService(final Integer numIterations, final String hashAlgorithm,
            final String privateSalt) {
        final DefaultHashService hashService = new DefaultHashService();

        if (numIterations != null) {
            hashService.setHashIterations(numIterations);
            LOG.info("DefaultPasswordHashService will utilize configured iteration count={}", numIterations);
        } else {
            hashService.setHashIterations(DEFAULT_NUM_ITERATIONS);
            LOG.info("DefaultPasswordHashService will utilize default iteration count={}", DEFAULT_NUM_ITERATIONS);
        }

        if (hashAlgorithm != null) {
            hashService.setHashAlgorithmName(hashAlgorithm);
            LOG.info("DefaultPasswordHashService will utilize configured algorithm={}", hashAlgorithm);
        } else {
            hashService.setHashAlgorithmName(DEFAULT_HASH_ALGORITHM);
            LOG.info("DefaultPasswordHashService will utilize default algorithm={}", DEFAULT_HASH_ALGORITHM);
        }

        if (privateSalt != null) {
            hashService.setPrivateSalt(ByteSource.Util.bytes(privateSalt));
            LOG.info("DefaultPasswordHashService will utilize a configured private salt");
        } else {
            hashService.setGeneratePublicSalt(true);
            LOG.info("DefaultPasswordHashService will not utilize a private salt, since none was configured");
        }

        return hashService;
    }
}
