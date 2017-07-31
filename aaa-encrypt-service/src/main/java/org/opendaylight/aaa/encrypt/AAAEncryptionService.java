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

public interface AAAEncryptionService {

    public String encrypt(String data);

    public String decrypt(String encData);

    public byte[] encrypt(byte[] data);

    public byte[] decrypt(byte[] encData);
}
