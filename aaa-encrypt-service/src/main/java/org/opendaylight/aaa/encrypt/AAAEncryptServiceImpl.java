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
import org.opendaylight.aaa.api.AAAEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptServiceImpl implements AAAEncryptionService{

    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptServiceImpl.class);
    public static final String ENCRYPTED_TAG = "Encrypted:";

    private final SecretKey key;
    private final IvParameterSpec ivspec;

    public AAAEncryptServiceImpl(String password,byte salt[]) {
        SecretKey tempKey = null;
        IvParameterSpec tempIvSpec = null;
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 32768, 128);
            tempKey = keyFactory.generateSecret(spec);
            tempKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
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
            return data;
        }

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
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
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, key, ivspec);
            byte[] cryptobytes = DatatypeConverter.parseBase64Binary(encData.substring(ENCRYPTED_TAG.length()));
            byte[] clearbytes = c.doFinal(cryptobytes);
            return DatatypeConverter.printBase64Binary(clearbytes);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchAlgorithmException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e){
            LOG.error("Failed to decrypt encoded data",e);
        }
        return encData;
    }
}
