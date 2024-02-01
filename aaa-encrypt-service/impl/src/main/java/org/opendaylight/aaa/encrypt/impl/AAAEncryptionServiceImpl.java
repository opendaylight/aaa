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
import javax.crypto.spec.GCMParameterSpec;
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
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public AAAEncryptionServiceImpl(final EncryptServiceConfig configuration) {
        final byte[] encryptionKeySalt = configuration.requireEncryptSalt();
        final GCMParameterSpec gcmSpec;
        try {
            final var keyFactory = SecretKeyFactory.getInstance(configuration.getEncryptMethod());
            final var spec = new PBEKeySpec(configuration.requireEncryptKey().toCharArray(), encryptionKeySalt,
                    configuration.getEncryptIterationCount(), configuration.getEncryptKeyLength());
            key = new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), configuration.getEncryptType());
            gcmSpec = new GCMParameterSpec(configuration.getAuthTagLength(), encryptionKeySalt);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize secret key", e);
        }
        try {
            final var cipher = Cipher.getInstance(configuration.getCipherTransforms());
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            encryptCipher = cipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create encrypt cipher.", e);
        }
        try {
            final var cipher = Cipher.getInstance(configuration.getCipherTransforms());
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            decryptCipher = cipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create decrypt cipher.", e);
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
        synchronized (encryptCipher) {
            return encryptCipher.doFinal(requireNonNull(data));
        }
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) throws BadPaddingException, IllegalBlockSizeException {
        return decryptCipher.doFinal(requireNonNull(encryptedData));
    }
}
