/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a basic encryption service implementation with configuration knobs.
 *
 * @author - Sharon Aicler (saichler@gmail.com)
 */
@Deprecated
@Component(factory = AAAEncryptionServiceImpl.FACTORY_NAME)
public final class AAAEncryptionServiceImpl implements AAAEncryptionService {
    static final String FACTORY_NAME = "org.opendaylight.aaa.encrypt.impl.AAAEncryptionServiceImpl";

    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptionServiceImpl.class);
    private static final String CONFIG_PROP = ".config";

    private final SecretKey key;
    private final EncryptServiceConfig configuration;
    private final IvParameterSpec ivSpec;
    // Thread-safe map of Cipher instances, keyed by Cipher mode (ENCRYPT_MODE, DECRYPT_MODE).
    private final Map<Integer, Cipher> ciphers = new ConcurrentHashMap<>();

    public AAAEncryptionServiceImpl(final EncryptServiceConfig configuration) {
        this.configuration = configuration;
        final byte[] encryptionKeySalt = configuration.requireEncryptSalt();
        try {
            final var keyFactory = SecretKeyFactory.getInstance(configuration.getEncryptMethod());
            final var spec = new PBEKeySpec(configuration.requireEncryptKey().toCharArray(), encryptionKeySalt,
                    configuration.getEncryptIterationCount(), configuration.getEncryptKeyLength());
            key = new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), configuration.getEncryptType());
            ivSpec = new IvParameterSpec(encryptionKeySalt);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize secret key", e);
        }
        ciphers.put(Cipher.ENCRYPT_MODE, initCipher(Cipher.ENCRYPT_MODE));
        ciphers.put(Cipher.DECRYPT_MODE, initCipher(Cipher.DECRYPT_MODE));
        LOG.info("AAAEncryptionService activated");
    }

    @Activate
    public AAAEncryptionServiceImpl(final Map<String, ?> properties) {
        this((EncryptServiceConfig) verifyNotNull(properties.get(CONFIG_PROP)));
    }

    static Map<String, ?> props(final EncryptServiceConfig configuration) {
        return Map.of(CONFIG_PROP, requireNonNull(configuration));
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("AAAEncryptionService deactivated");
    }

    @Override
    public byte[] encrypt(final byte[] data) throws BadPaddingException, IllegalBlockSizeException {
        try {
            final var cipher = getCipher(Cipher.ENCRYPT_MODE);
            return cipher.doFinal(requireNonNull(data));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            // Remove Cipher after exception and re-throw.
            LOG.warn("Failed to encrypt data, resetting encrypt Cipher.", e);
            ciphers.remove(Cipher.ENCRYPT_MODE);
            throw e;
        }
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) throws BadPaddingException, IllegalBlockSizeException {
        try {
            final var cipher = getCipher(Cipher.DECRYPT_MODE);
            return cipher.doFinal(requireNonNull(encryptedData));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            // Remove Cipher after exception and re-throw.
            LOG.warn("Failed to decrypt data, resetting decrypt Cipher.", e);
            ciphers.remove(Cipher.DECRYPT_MODE);
            throw e;
        }
    }

    private Cipher getCipher(final int mode) {
        return ciphers.computeIfAbsent(mode, this::initCipher);
    }

    private Cipher initCipher(final int mode) {
        try {
            final var cipher = Cipher.getInstance(configuration.getCipherTransforms());
            cipher.init(mode, key, ivSpec);
            return cipher;
        } catch (GeneralSecurityException e) {
            final var stringMode = mode == Cipher.DECRYPT_MODE ? "decrypt" : "encrypt";
            throw new IllegalStateException("Failed to create " + stringMode + " cipher.", e);
        }
    }
}
