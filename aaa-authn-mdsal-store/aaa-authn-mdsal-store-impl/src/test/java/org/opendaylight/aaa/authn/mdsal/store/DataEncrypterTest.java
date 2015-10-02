package org.opendaylight.aaa.authn.mdsal.store;

import static org.junit.Assert.*;

import org.junit.Test;

public class DataEncrypterTest {

    @Test
    public void testEncrypt() {
        DataEncrypter dataEncry = new DataEncrypter("foo_key_test");
        String token = "foo_token_test";
        String eToken = dataEncry.encrypt(token);
        assertEquals(token, dataEncry.decrypt(eToken));
    }

    @Test
    public void testDecrypt() {
        DataEncrypter dataEncry = new DataEncrypter("foo_key_test");
        String eToken = "foo_etoken_test";
        assertEquals(dataEncry.decrypt(""), null);
        assertEquals(eToken, dataEncry.decrypt(eToken));
    }

}
