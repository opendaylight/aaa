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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.checkerframework.checker.lock.qual.GuardedBy;
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
    private final IvParameterSpec ivSpec;
    private final Cipher baseEncryptCipher;
    private final Cipher baseDecryptCipher;

    @GuardedBy("baseEncryptCipher")
    private Cipher encryptCipher = null;
    @GuardedBy("baseDecryptCipher")
    private Cipher decryptCipher = null;

    public AAAEncryptionServiceImpl(final EncryptServiceConfig configuration) {
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
        final var cipherTransforms = configuration.getCipherTransforms();
        try {
            baseEncryptCipher = Cipher.getInstance(cipherTransforms);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create base encrypt cipher", e);
        }
        try {
            baseDecryptCipher = Cipher.getInstance(cipherTransforms);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create base decrypt cipher", e);
        }
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
        synchronized (baseEncryptCipher) {
            if (encryptCipher == null) {
                encryptCipher = initCipher(Cipher.ENCRYPT_MODE, baseEncryptCipher);
            }
            try {
                return encryptCipher.doFinal(requireNonNull(data));
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                // Remove Cipher after exception and re-throw.
                LOG.warn("Failed to encrypt data, resetting encrypt Cipher.", e);
                encryptCipher = null;
                throw e;
            }
        }
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) throws BadPaddingException,
            IllegalBlockSizeException {
        synchronized (baseDecryptCipher) {
            if (decryptCipher == null) {
                decryptCipher = initCipher(Cipher.DECRYPT_MODE, baseDecryptCipher);
            }
            try {
                return decryptCipher.doFinal(requireNonNull(encryptedData));
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                // Remove Cipher after exception and re-throw.
                LOG.warn("Failed to decrypt data, resetting decrypt Cipher.", e);
                decryptCipher = null;
                throw e;
            }
        }
    }

    private Cipher initCipher(final int mode, final Cipher cipher) {
        try {
            cipher.init(mode, key, ivSpec);
            return cipher;
        } catch (GeneralSecurityException e) {
            final var stringMode = mode == Cipher.DECRYPT_MODE ? "decrypt" : "encrypt";
            throw new IllegalStateException("Failed to create " + stringMode + " cipher.", e);
        }
    }
}
