/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426.AAAEncryptServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptionServiceImpl implements AAAEncryptionService {

    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptionServiceImpl.class);

    private final SecretKey key;
    private final IvParameterSpec ivspec;
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;
    private final String encryptTag;

    public AAAEncryptionServiceImpl(AAAEncryptServiceModule module) {
        SecretKey tempKey = null;
        IvParameterSpec tempIvSpec = null;
        this.encryptTag = module.getEncryptTag();
        final byte[] enryptionKeySalt = module.getEncryptionKeySalt();
        try {
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(module.getEncryptMethod());
            final KeySpec spec = new PBEKeySpec(module.getEncryptKey().toCharArray(), enryptionKeySalt, module.getEncryptIterationCount(), module.getEncryptKeyLength());
            tempKey = keyFactory.generateSecret(spec);
            tempKey = new SecretKeySpec(tempKey.getEncoded(), module.getEncryptType());
            tempIvSpec = new IvParameterSpec(enryptionKeySalt);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error("Failed to initialize secret key", e);
        }

        key = tempKey;
        ivspec = tempIvSpec;
        Cipher c = null;
        try {
            c = Cipher.getInstance(module.getCipherTransforms());
            c.init(Cipher.ENCRYPT_MODE, key, ivspec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            LOG.error("Failed to create encrypt cipher.", e);
        }
        this.encryptCipher = c;
        c = null;
        try {
            c = Cipher.getInstance(module.getCipherTransforms());
            c.init(Cipher.DECRYPT_MODE, key, ivspec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            LOG.error("Failed to create decrypt cipher.", e);
        }
        this.decryptCipher = c;
    }

    @Override
    public String encrypt(String data) {
        //We could not instantiate the encryption key, hence no encryption or decryption will be done.
        if (key == null) {
            LOG.warn("Encryption Key is NULL, will not encrypt data.");
            return data;
        }

        try {
            synchronized(encryptCipher) {
                byte[] cryptobytes = encryptCipher.doFinal(data.getBytes());
                String cryptostring = DatatypeConverter.printBase64Binary(cryptobytes);
                return encryptTag + cryptostring;
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to encrypt data.", e);
        }

        return data;
    }

    @Override
    public String decrypt(String encData) {
        if (key == null || encData == null || encData.length() == 0 || !encData.startsWith(encryptTag)) {
            LOG.warn("String {} was not decrypted.", encData);
            return encData;
        }

        try{
            byte[] cryptobytes = DatatypeConverter.parseBase64Binary(encData.substring(encryptTag.length()));
            byte[] clearbytes = decryptCipher.doFinal(cryptobytes);
            return new String(clearbytes);
        } catch (IllegalBlockSizeException | BadPaddingException e){
            LOG.error("Failed to decrypt encoded data", e);
        }
        return encData;
    }

    @Override
    public byte[] encrypt(byte[] data) {
        //We could not instantiate the encryption key, hence no encryption or decryption will be done.
        if (key == null) {
            LOG.warn("Encryption Key is NULL, will not encrypt data.");
            return data;
        }

        try {
            synchronized(encryptCipher) {
                return encryptCipher.doFinal(data);
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to encrypt data.", e);
        }
        return data;
    }

    @Override
    public byte[] decrypt(byte[] encData) {
        if (encData == null) {
            LOG.warn("encData is null.");
            return encData;
        }

        try {
            return decryptCipher.doFinal(encData);
        } catch (IllegalBlockSizeException | BadPaddingException e){
            LOG.error("Failed to decrypt encoded data", e);
        }
        return encData;
    }
}
