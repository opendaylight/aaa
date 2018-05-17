package org.opendaylight.aaa.impl.password.service;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.SimpleHashRequest;
import org.apache.shiro.util.ByteSource;
import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordService;

public class DefaultPasswordService implements PasswordService {

    private static final String HASH_ALGORITHM = "SHA-512";
    private static final int NUM_ITERATIONS = 10000;

    private final DefaultHashService hashService = new DefaultHashService();

    public DefaultPasswordService() {
        hashService.setHashIterations(NUM_ITERATIONS);
        hashService.setHashAlgorithmName(HASH_ALGORITHM);
        hashService.setRandomNumberGenerator(new SecureRandomNumberGenerator());
        hashService.setGeneratePublicSalt(true);
    }

    @Override
    public PasswordHash getHashedPassword(final String password) {
        final HashRequest hashRequest = new HashRequest.Builder().setAlgorithmName(HASH_ALGORITHM)
                .setIterations(NUM_ITERATIONS)
                .setSource(ByteSource.Util.bytes(password)).build();

        final Hash hash =  hashService.computeHash(hashRequest);
        return PasswordHashImpl.create(
                HASH_ALGORITHM,
                hash.getSalt().toBase64(),
                NUM_ITERATIONS,
                hash.toBase64());
    }

    @Override
    public PasswordHash getHashedPassword(final String password, final String salt) {
        final HashRequest hashRequest = new SimpleHashRequest(
                HASH_ALGORITHM,
                ByteSource.Util.bytes(password),
                ByteSource.Util.bytes(Base64.decode(salt)),
                NUM_ITERATIONS);

        final Hash hash =  hashService.computeHash(hashRequest);
        return PasswordHashImpl.create(
                hash.getAlgorithmName(),
                salt,
                hash.getIterations(),
                hash.toBase64());
    }

    @Override
    public boolean passwordsMatch(final String plaintext, final String stored, final String salt) {
        final String hash = getHashedPassword(plaintext, salt).getHashedPassword();

        return hash.equals(stored);
    }
}
