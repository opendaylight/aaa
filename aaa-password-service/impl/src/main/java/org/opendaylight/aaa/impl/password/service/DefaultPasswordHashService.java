/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import com.google.common.base.Strings;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.SimpleHashRequest;
import org.apache.shiro.util.ByteSource;
import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPasswordHashService implements PasswordHashService {
    @ObjectClassDefinition
    public @interface Configuration {
        /**
         * The algorithm utilized for hashing.
         *
         * @return The algorithm used for hashing.
         */
        String algorithm() default DEFAULT_HASH_ALGORITHM;

        /**
         * The number of times to hash.
         *
         * @return Number of times to hash.
         */
        int iterations() default DEFAULT_NUM_ITERATIONS;

        /**
         * The private salt for password hashing.
         *
         * @return The private salt.
         */
        @AttributeDefinition(name = "private-salt")
        String privateSalt() default "";
    }

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPasswordHashService.class);

    public static final String DEFAULT_HASH_ALGORITHM = "SHA-512";
    public static final int DEFAULT_NUM_ITERATIONS = 20000;

    private final DefaultHashService hashService;

    public DefaultPasswordHashService() {
        hashService = createHashService(DEFAULT_NUM_ITERATIONS, null, null);
    }

    public DefaultPasswordHashService(final Configuration configuration) {
        hashService = createHashService(configuration.iterations(), configuration.algorithm(),
            configuration.privateSalt());
    }

    @Override
    public PasswordHash getPasswordHash(final String password) {
        final HashRequest hashRequest = new HashRequest.Builder()
                .setAlgorithmName(hashService.getHashAlgorithmName())
                .setIterations(hashService.getHashIterations())
                .setSource(ByteSource.Util.bytes(password)).build();

        final Hash hash =  hashService.computeHash(hashRequest);
        return PasswordHashImpl.create(
                hash.getAlgorithmName(),
                hash.getSalt().toBase64(),
                hash.getIterations(),
                hash.toBase64());
    }

    @Override
    public PasswordHash getPasswordHash(final String password, final String salt) {
        final HashRequest hashRequest = new SimpleHashRequest(
                hashService.getHashAlgorithmName(),
                ByteSource.Util.bytes(password),
                ByteSource.Util.bytes(Base64.decode(salt)),
                hashService.getHashIterations());

        final Hash hash =  hashService.computeHash(hashRequest);
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

    private static DefaultHashService createHashService(final int numIterations, final String hashAlgorithm,
            final String privateSalt) {
        final DefaultHashService hashService = new DefaultHashService();

        hashService.setHashIterations(numIterations);
        LOG.info("DefaultPasswordHashService will utilize iteration count={}", numIterations);

        if (hashAlgorithm != null) {
            hashService.setHashAlgorithmName(hashAlgorithm);
            LOG.info("DefaultPasswordHashService will utilize algorithm={}", hashAlgorithm);
        } else {
            hashService.setHashAlgorithmName(DEFAULT_HASH_ALGORITHM);
            LOG.info("DefaultPasswordHashService will utilize default algorithm={}", DEFAULT_HASH_ALGORITHM);
        }

        if (!Strings.isNullOrEmpty(privateSalt)) {
            hashService.setPrivateSalt(ByteSource.Util.bytes(privateSalt));
            LOG.info("DefaultPasswordHashService will utilize a configured private salt");
        } else {
            hashService.setGeneratePublicSalt(true);
            LOG.info("DefaultPasswordHashService will not utilize a private salt, since none was configured");
        }

        return hashService;
    }
}
