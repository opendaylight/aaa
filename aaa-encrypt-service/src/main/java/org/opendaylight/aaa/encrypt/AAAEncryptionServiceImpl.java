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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
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

    public AAAEncryptionServiceImpl(AaaEncryptServiceConfig module) {
        SecretKey tempKey = null;
        IvParameterSpec tempIvSpec = null;
        if (module.getEncryptSalt() == null) {
			throw new IllegalArgumentException("null encryptSalt in AaaEncryptServiceConfig: " + module.toString());
		}
        final byte[] enryptionKeySalt = getEncryptionKeySalt(module.getEncryptSalt());
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
                return cryptostring;
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Failed to encrypt data.", e);
        }

        return data;
    }

    @Override
    public String decrypt(String encData) {
        if (key == null || encData == null || encData.length() == 0) {
            LOG.warn("String {} was not decrypted.", encData);
            return encData;
        }

        try{
            byte[] cryptobytes = DatatypeConverter.parseBase64Binary(encData);
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

    private byte[] getEncryptionKeySalt(String encryptSalt) {
        StringTokenizer tokens = new StringTokenizer(encryptSalt, ",");
        List<Byte> saltList = new ArrayList<>();
        while (tokens.hasMoreTokens()) {
            String by = tokens.nextToken();
            saltList.add(Byte.parseByte(by.trim()));
        }
        byte salt[] = new byte[saltList.size()];
        int i = 0;
        for (Byte b : saltList) {
            salt[i] = b;
            i++;
        }
        return salt;
    }

}
