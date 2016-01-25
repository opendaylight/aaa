/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.DatatypeConverter;
import org.junit.Test;

public class DataEncrypterTest {

    @Test
    public void testEncrypt() {
        DataEncrypter dataEncry = new DataEncrypter("foo_key_test");
        String token = "foo_token_test";
        String eToken = dataEncry.encrypt(token);
        // check for decryption result
        String returnToken = dataEncry.decrypt(eToken);
        String tokenBase64 = DatatypeConverter.printBase64Binary(token.getBytes());
        assertEquals(tokenBase64, returnToken);
    }

    @Test
    public void testDecrypt() {
        DataEncrypter dataEncry = new DataEncrypter("foo_key_test");
        String eToken = "foo_etoken_test";
        assertEquals(dataEncry.decrypt(""), null);
        // check for encryption Tag
        assertEquals(eToken, dataEncry.decrypt(eToken));
    }

}
