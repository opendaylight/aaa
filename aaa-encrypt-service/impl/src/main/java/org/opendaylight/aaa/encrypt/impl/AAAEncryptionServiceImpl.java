/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a basic encryption service implementation with configuration knobs.
 *
 * @author - Sharon Aicler (saichler@gmail.com)
 */
@Deprecated
public class AAAEncryptionServiceImpl implements AAAEncryptionService {
    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptionServiceImpl.class);

    private final SecretKey key;
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public AAAEncryptionServiceImpl(final EncryptServiceConfig encrySrvConfig) {
        final byte[] encryptionKeySalt = encrySrvConfig.requireEncryptSalt();
        IvParameterSpec tempIvSpec = null;
        SecretKey tempKey = null;
        try {
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encrySrvConfig.getEncryptMethod());
            final KeySpec spec = new PBEKeySpec(encrySrvConfig.requireEncryptKey().toCharArray(), encryptionKeySalt,
                    encrySrvConfig.getEncryptIterationCount(), encrySrvConfig.getEncryptKeyLength());
            tempKey = new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), encrySrvConfig.getEncryptType());
            tempIvSpec = new IvParameterSpec(encryptionKeySalt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error("Failed to initialize secret key", e);
        }
        key = tempKey;
        final var ivSpec = tempIvSpec;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(encrySrvConfig.getCipherTransforms());
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | InvalidKeyException e) {
            LOG.error("Failed to create encrypt cipher.", e);
        }
        encryptCipher = cipher;
        cipher = null;
        try {
            cipher = Cipher.getInstance(encrySrvConfig.getCipherTransforms());
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | InvalidKeyException e) {
            LOG.error("Failed to create decrypt cipher.", e);
        }
        decryptCipher = cipher;
    }

    @Override
    public String encrypt(final String data) {
        // We could not instantiate the encryption key, hence no encryption or
        // decryption will be done.
        if (key == null) {
            LOG.warn("Encryption Key is NULL, will not encrypt data.");
            return data;
        }

        final byte[] cryptobytes;
        try {
            synchronized (encryptCipher) {
                cryptobytes = encryptCipher.doFinal(data.getBytes(Charset.defaultCharset()));
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to encrypt data.", e);
            return data;
        }
        return Base64.getEncoder().encodeToString(cryptobytes);
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        // We could not instantiate the encryption key, hence no encryption or
        // decryption will be done.
        if (key == null) {
            LOG.warn("Encryption Key is NULL, will not encrypt data.");
            return data;
        }
        try {
            synchronized (encryptCipher) {
                return encryptCipher.doFinal(data);
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to encrypt data.", e);
            return data;
        }
    }

    @Override
    public String decrypt(final String encryptedData) {
        if (key == null || encryptedData == null || encryptedData.length() == 0) {
            LOG.warn("String {} was not decrypted.", encryptedData);
            return encryptedData;
        }

        final byte[] cryptobytes = Base64.getDecoder().decode(encryptedData);
        final byte[] clearbytes;
        try {
            clearbytes = decryptCipher.doFinal(cryptobytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to decrypt encoded data", e);
            return encryptedData;
        }
        return new String(clearbytes, Charset.defaultCharset());
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) {
        if (encryptedData == null) {
            LOG.warn("encryptedData is null.");
            return encryptedData;
        }
        try {
            return decryptCipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to decrypt encoded data", e);
        }
        return encryptedData;
    }
}
