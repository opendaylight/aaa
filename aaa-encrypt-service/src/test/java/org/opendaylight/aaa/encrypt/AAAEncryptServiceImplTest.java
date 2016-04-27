/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.aaa.encrypt.AAAEncryptServiceImpl;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptServiceImplTest {
    private AAAEncryptServiceImpl impl = new AAAEncryptServiceImpl("Test Key",new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});

    @Test
    public void testShortString(){
        String before = "shortone";
        String encrypt = impl.encrypt(before);
        Assert.assertNotEquals(before,encrypt);
        String after = impl.decrypt(encrypt);
        Assert.assertEquals(before,after);
    }

    @Test
    public void testLongString(){
        String before = "This is a very long string to encrypt for testing 1...2...3";
        String encrypt = impl.encrypt(before);
        Assert.assertNotEquals(before,encrypt);
        String after = impl.decrypt(encrypt);
        Assert.assertEquals(before,after);
    }
}
