/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptionServiceImpl implements AAAEncryptionService {

    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptionServiceImpl.class);
    private final String DEFAULT_CONFIG_FILE_PATH = "etc" + File.separator + "opendaylight" + File.separator
            + "datastore" + File.separator + "initial" + File.separator + "config" + File.separator + "aaa-encrypt-service-config.xml";

    private final SecretKey key;
    private final IvParameterSpec ivspec;
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public AAAEncryptionServiceImpl(AaaEncryptServiceConfig encrySrvConfig) {
        SecretKey tempKey = null;
        IvParameterSpec tempIvSpec = null;
        if (encrySrvConfig.getEncryptSalt() == null) {
            throw new IllegalArgumentException("null encryptSalt in AaaEncryptServiceConfig: " + encrySrvConfig.toString());
        }
        if (encrySrvConfig.getEncryptKey() != null && encrySrvConfig.getEncryptKey().isEmpty()) {
            LOG.debug("Set the Encryption service password and encrypt salt");
            String newPwd = RandomStringUtils.random(encrySrvConfig.getPasswordLength(), true, true);
            final Random random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            encrySrvConfig = new AaaEncryptServiceConfigBuilder(encrySrvConfig)
                    .setEncryptKey(newPwd)
                    .setEncryptSalt(encodedSalt).build();
            updateEncrySrvConfig(newPwd, encodedSalt);
        }
        final byte[] enryptionKeySalt = Base64.getDecoder().decode(encrySrvConfig.getEncryptSalt());
        try {
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encrySrvConfig.getEncryptMethod());
            final KeySpec spec = new PBEKeySpec(encrySrvConfig.getEncryptKey().toCharArray(), enryptionKeySalt,
                        encrySrvConfig.getEncryptIterationCount(), encrySrvConfig.getEncryptKeyLength());
            tempKey = keyFactory.generateSecret(spec);
            tempKey = new SecretKeySpec(tempKey.getEncoded(), encrySrvConfig.getEncryptType());
            tempIvSpec = new IvParameterSpec(enryptionKeySalt);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error("Failed to initialize secret key", e);
        }
        key = tempKey;
        ivspec = tempIvSpec;
        Cipher c = null;
        try {
            c = Cipher.getInstance(encrySrvConfig.getCipherTransforms());
            c.init(Cipher.ENCRYPT_MODE, key, ivspec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            LOG.error("Failed to create encrypt cipher.", e);
        }
        this.encryptCipher = c;
        c = null;
        try {
            c = Cipher.getInstance(encrySrvConfig.getCipherTransforms());
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

    private void updateEncrySrvConfig(String newPwd, String newSalt) {
        try {
            final String encryptKeyTag = "encrypt-key";
            final String encryptSaltTag = "encrypt-salt";
            LOG.debug("Update encryption service config file");
            final File configFile = new File(DEFAULT_CONFIG_FILE_PATH);
            if (configFile.exists()) {
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                final Document doc = docBuilder.parse(configFile);
                final Node key = doc.getElementsByTagName(encryptKeyTag).item(0);
                key.setTextContent(newPwd);
                final Node salt = doc.getElementsByTagName(encryptSaltTag).item(0);
                salt.setTextContent(newSalt);
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                final Transformer transformer = transformerFactory.newTransformer();
                final DOMSource source = new DOMSource(doc);
                final StreamResult result = new StreamResult(new File(DEFAULT_CONFIG_FILE_PATH));
                transformer.transform(source, result);
            } else {
                LOG.warn("The encryption service config file does not exist {}", DEFAULT_CONFIG_FILE_PATH);
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
            LOG.error("Error while updating the encryption service config file", e);
        }
    }
}