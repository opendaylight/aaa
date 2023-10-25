/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import java.security.GeneralSecurityException;

/**
 * A generic encryption/decryption service for encrypting various data in ODL.
 *
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface AAAEncryptionService {
    /**
     * Encrypt {@code data} using a 2-way encryption mechanism.
     *
     * @param data plaintext data
     * @return an encrypted representation of {@code data}
     * @throws NullPointerException when {@code data} is {@code null}
     * @throws GeneralSecurityException when encryption fails
     */
    byte[] encrypt(byte[] data) throws GeneralSecurityException;

    /**
     * Decrypt {@code encryptedData} using a 2-way decryption mechanism.
     *
     * @param encryptedData encrypted data
     * @return plaintext bytes
     * @throws NullPointerException when {@code encryptedData} is {@code null}
     * @throws GeneralSecurityException when decryption fails
     */
    byte[] decrypt(byte[] encryptedData) throws GeneralSecurityException;
}
