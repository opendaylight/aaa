/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
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

    String encrypt(String data);

    byte[] encrypt(byte[] data);

    String decrypt(String encData);

    byte[] decrypt(byte[] encData);
}
