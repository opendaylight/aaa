/*
 * Copyright (c) 2017 Brocade Communication Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public interface PKIUtilService {
    public PublicKey decodePublicKey(String keyLine) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException;
    public String encodePublicKey(PublicKey publicKey) throws IOException ;
    public KeyPair decodePrivateKey(String keyPath, String passphrase) throws IOException;
}
