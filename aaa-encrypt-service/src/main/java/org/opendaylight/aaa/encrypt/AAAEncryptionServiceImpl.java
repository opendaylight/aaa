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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptionServiceImpl implements AAAEncryptionService{

    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptionServiceImpl.class);
    public static final String ENCRYPTED_TAG = "Encrypted:";
    private static final String ENCRYPTION_METHOD = "PBKDF2WithHmacSHA1";
    private static final String ENCRYPTION_TYPE = "AES";
    private static final int ENCRYPTION_ITERATION_COUNT = 32768;
    private static final int ENCRYPTION_KEY_LENGTH = 128;
    private static final String CIPHER_TRANSFORMS = "AES/CBC/PKCS5Padding";

    private final SecretKey key;
    private final IvParameterSpec ivspec;

    public AAAEncryptionServiceImpl(String password, byte salt[]) {
        SecretKey tempKey = null;
        IvParameterSpec tempIvSpec = null;
        try {
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_METHOD);
            final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ENCRYPTION_ITERATION_COUNT, ENCRYPTION_KEY_LENGTH);
            tempKey = keyFactory.generateSecret(spec);
            tempKey = new SecretKeySpec(tempKey.getEncoded(), ENCRYPTION_TYPE);
            tempIvSpec = new IvParameterSpec(salt);
        }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            LOG.error("Failed to initialize secret key",e);
        }
        key = tempKey;
        ivspec = tempIvSpec;
    }

    @Override
    public String encrypt(String data) {
        //We could not instantiate the encryption key, hence no encryption or decryption will be done.
        if (key == null) {
            LOG.info("Encryption Key is NULL, will not encrypt data.");
            return data;
        }

        try {
            Cipher c = Cipher.getInstance(CIPHER_TRANSFORMS);
            c.init(Cipher.ENCRYPT_MODE, key, ivspec);
            byte[] cryptobytes = c.doFinal(data.getBytes());
            String cryptostring = DatatypeConverter.printBase64Binary(cryptobytes);
            return ENCRYPTED_TAG + cryptostring;
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchAlgorithmException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e){
            LOG.error("Failed to encrypt data.",e);
        }
        return data;
    }

    @Override
    public String decrypt(String encData) {
        if (key == null || encData == null || encData.length() == 0 || !encData.startsWith(ENCRYPTED_TAG)) {
            return encData;
        }

        try{
            Cipher c = Cipher.getInstance(CIPHER_TRANSFORMS);
            c.init(Cipher.DECRYPT_MODE, key, ivspec);
            byte[] cryptobytes = DatatypeConverter.parseBase64Binary(encData.substring(ENCRYPTED_TAG.length()));
            byte[] clearbytes = c.doFinal(cryptobytes);
            return new String(clearbytes);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchAlgorithmException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e){
            LOG.error("Failed to decrypt encoded data",e);
        }
        return encData;
    }
}
