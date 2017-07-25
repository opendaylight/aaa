/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */

import java.security.GeneralSecurityException;

public interface AAAEncryptionService {

    public String encrypt(String data) throws GeneralSecurityException;

    public String decrypt(String encData) throws GeneralSecurityException;

    public byte[] encrypt(byte[] data) throws GeneralSecurityException;

    public byte[] decrypt(byte[] encData) throws GeneralSecurityException;
}
