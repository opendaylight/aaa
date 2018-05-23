/*
 * Copyright (c) 2018 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.encrypt;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;

public class AAAEncryptionServiceToShiroCipherServiceAdaptor implements AAAEncryptionService {

    private final AesCipherService cipherService;
    private final byte [] key;

    public AAAEncryptionServiceToShiroCipherServiceAdaptor() {
        this.cipherService = new AesCipherService();
        key = cipherService.generateNewKey().getEncoded();
    }

    @Override
    public String encrypt(final String data) {
        return cipherService.encrypt(CodecSupport.toBytes(data), key).toBase64();
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return cipherService.encrypt(data, key).getBytes();
    }

    @Override
    public String decrypt(String encryptedData) {
        return CodecSupport.toString(cipherService.decrypt(Base64.decode(encryptedData), key).getBytes());
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) {
        return cipherService.decrypt(encryptedData, key).getBytes();
    }
}
