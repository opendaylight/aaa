/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import org.opendaylight.aaa.encrypt.exception.DecryptionException;
import org.opendaylight.aaa.encrypt.exception.EncryptionException;

/**
 * A generic encryption/decryption service for encrypting various data in ODL.
 *
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface AAAEncryptionService {

    /**
     * Encrypt <code>data</code> using a 2-way encryption mechanism.
     *
     * @param data plaintext data
     * @return an encrypted representation of <code>data</code>
     * @throws EncryptionException exception thrown during encryption
     */
    String encrypt(String data) throws EncryptionException;

    /**
     * Encrypt <code>data</code> using a 2-way encryption mechanism.
     *
     * @param data plaintext data
     * @return an encrypted representation of <code>data</code>
     * @throws EncryptionException exception thrown during encryption
     */
    byte[] encrypt(byte[] data) throws EncryptionException;

    /**
     * Decrypt <code>data</code> using a 2-way decryption mechanism.
     *
     * @param encryptedData encrypted data
     * @return plaintext <code>data</code>
     * @throws DecryptionException exception thrown during decryption
     */
    String decrypt(String encryptedData) throws DecryptionException;

    /**
     * Decrypt <code>data</code> using a 2-way decryption mechanism.
     *
     * @param encryptedData encrypted data
     * @return plaintext <code>data</code>
     * @throws DecryptionException exception thrown during decryption
     */
    byte[] decrypt(byte[] encryptedData) throws DecryptionException;
}
