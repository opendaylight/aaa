/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import java.util.Optional;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.SimpleHashRequest;
import org.apache.shiro.util.ByteSource;
import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPasswordHashService implements PasswordHashService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPasswordHashService.class);

    public static final String DEFAULT_HASH_ALGORITHM = "SHA-512";
    public static final int DEFAULT_NUM_ITERATIONS = 10000;

    private final DefaultHashService hashService = new DefaultHashService();

    public DefaultPasswordHashService(final PasswordServiceConfig passwordServiceConfig) {
        final Optional<Integer> numIterationsOptional = Optional.ofNullable(passwordServiceConfig.getIterations());
        setNumIterations(numIterationsOptional);

        final Optional<String> hashAlgorithmOptional = Optional.ofNullable(passwordServiceConfig.getAlgorithm());
        setHashAlgorithm(hashAlgorithmOptional);

        final Optional<String> privateSaltOptional = Optional.ofNullable(passwordServiceConfig.getPrivateSalt());
        setPrivateSalt(privateSaltOptional);

        hashService.setRandomNumberGenerator(new SecureRandomNumberGenerator());
        hashService.setGeneratePublicSalt(true);
    }

    private void setNumIterations(final Optional<Integer> numIterationsOptional) {
        if (numIterationsOptional.isPresent()) {
            final Integer numIterations = numIterationsOptional.get();
            hashService.setHashIterations(numIterations);
            LOG.info("DefaultPasswordHashService will utilize configured iteration count={}", numIterations);
        } else {
            hashService.setHashIterations(DEFAULT_NUM_ITERATIONS);
            LOG.info("DefaultPasswordHashService will utilize default iteration count={}", DEFAULT_NUM_ITERATIONS);
        }
    }

    private void setHashAlgorithm(final Optional<String> hashAlgorithmOptional) {
        if (hashAlgorithmOptional.isPresent()) {
            final String hashAlgorithm = hashAlgorithmOptional.get();
            hashService.setHashAlgorithmName(hashAlgorithm);
            LOG.info("DefaultPasswordHashService will utilize configured algorithm={}", hashAlgorithm);
        } else {
            hashService.setHashAlgorithmName(DEFAULT_HASH_ALGORITHM);
            LOG.info("DefaultPasswordHashService will utilize default algorithm={}", DEFAULT_HASH_ALGORITHM);
        }
    }

    private void setPrivateSalt(final Optional<String> privateSaltOptional) {
        if (privateSaltOptional.isPresent()) {
            hashService.setPrivateSalt(ByteSource.Util.bytes(privateSaltOptional.get()));
            LOG.info("DefaultPasswordHashService will utilize a configured private salt");
        } else {
            LOG.info("DefaultPasswordHashService will not utilize a private salt, since none was configured");
        }
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
        final String hash = getPasswordHash(plaintext, salt).getHashedPassword();
        return hash.equals(stored);
    }
}
