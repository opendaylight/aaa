/*
 * Copyright (c) 2016, 2017 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.aaa.idpmapping.Token.TokenStorageType;
import org.opendaylight.aaa.idpmapping.Token.TokenType;

@Ignore
public class TokenTest {

    private final Map<String, Object> namespace = new HashMap<String, Object>() {
        {
            put("foo1", new HashMap<String, String>() {
                {
                    put("0", "1");
                }
            });
        }
    };
    private final Object input = "$foo1[0]";
    private final Token token = new Token(input, namespace);
    private final Token mapToken = new Token(namespace, namespace);

    @Test
    public void testToken() {
        assertEquals(token.toString(), input);
        assertTrue(token.storageType == TokenStorageType.VARIABLE);
        assertEquals(mapToken.toString(), "{foo1={0=1}}");
        assertTrue(mapToken.storageType == TokenStorageType.CONSTANT);
    }

    @Test
    public void testClassify() {
        assertEquals(Token.classify(new ArrayList<>()), TokenType.ARRAY);
        assertEquals(Token.classify(true), TokenType.BOOLEAN);
        assertEquals(Token.classify(new Long(365)), TokenType.INTEGER);
        assertEquals(Token.classify(new HashMap<String, Object>()), TokenType.MAP);
        assertEquals(Token.classify(null), TokenType.NULL);
        assertEquals(Token.classify(365.00), TokenType.REAL);
        assertEquals(Token.classify("foo_str"), TokenType.STRING);
    }

    @Test
    public void testGet() {
        assertNotNull(token.get());
        assertTrue(token.get("0") == "1");
        assertNotNull(mapToken.get());
        assertTrue(mapToken.get(0) == namespace);
    }

    @Test
    public void testGetMapValue() {
        assertTrue(mapToken.getMapValue() == namespace);
    }
}
