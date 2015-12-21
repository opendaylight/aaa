/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store;

import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public class DataEncrypter {

    final protected SecretKey k;
    private static final Logger LOG = LoggerFactory.getLogger(DataEncrypter.class);
    private static final byte[] iv = { 0, 5, 0, 0, 7, 81, 0, 3, 0, 0, 0, 0, 0, 43, 0, 1 };
    private static final IvParameterSpec ivspec = new IvParameterSpec(iv);
    public static final String ENCRYPTED_TAG = "Encrypted:";

    public DataEncrypter(final String ckey) {
        SecretKey tmp = null;
        if (ckey != null && !ckey.isEmpty()) {

            try {
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(ckey.toCharArray(), iv, 32768, 128);
                tmp = keyFactory.generateSecret(spec);
            } catch (Exception e) {
                LOG.error("Couldn't initialize key factory", e);
            }
            if (tmp != null) {
                k = new SecretKeySpec(tmp.getEncoded(), "AES");
            } else {
                throw new RuntimeException("Couldn't initalize encryption key");
            }
        } else {
            k = null;
            LOG.warn("Void crypto key passed! AuthN Store Encryption disabled");
        }

    }

    protected String encrypt(String token) {

        if (k == null) {
            return token;
        }

        String cryptostring = null;
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, k, ivspec);
            byte[] cryptobytes = c.doFinal(token.getBytes());
            cryptostring = DatatypeConverter.printBase64Binary(cryptobytes);
            return ENCRYPTED_TAG + cryptostring;
        } catch (Exception e) {
            LOG.error("Couldn't encrypt token", e);
            return null;
        }
    }

    protected String decrypt(String eToken) {
        if (k == null) {
            return eToken;
        }

        if (eToken == null || eToken.length() == 0) {
            return null;
        }

        if (!eToken.startsWith(ENCRYPTED_TAG)) {
            return eToken;
        }

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, k, ivspec);

            byte[] cryptobytes = DatatypeConverter.parseBase64Binary(eToken.substring(ENCRYPTED_TAG.length()));
            byte[] clearbytes = c.doFinal(cryptobytes);
            return DatatypeConverter.printBase64Binary(clearbytes);

        } catch (Exception e) {
            LOG.error("Couldn't decrypt token", e);
            return null;
        }
    }
}
