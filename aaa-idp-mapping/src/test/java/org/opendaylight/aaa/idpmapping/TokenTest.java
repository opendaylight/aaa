package org.opendaylight.aaa.idpmapping;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TokenTest {

    private final Map<String, Object> namespace = new HashMap<String, Object>() {{
        put("foo1", new HashMap<String, String>(){{put("0", "1"); }});
    }};
    private Object input = "$foo1[0]";
    private Token token = new Token(input, namespace);
    private Token mapToken = new Token(namespace, namespace);

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
        assertEquals(Token.classify(new HashMap<String , Object>()), TokenType.MAP);
        assertEquals(Token.classify(null), TokenType.NULL);
        assertEquals(Token.classify(365.00), TokenType.REAL);
        assertEquals(Token.classify("foo_str"), TokenType.STRING);
    }

    @Test
    public void testGet() {
        assertNotNull(token.get());
        assertTrue( token.get("0") == "1");
        assertNotNull(mapToken.get());
        assertTrue( mapToken.get(0) == namespace);
    }

    @Test
    public void testGetMapValue() {
        assertTrue(mapToken.getMapValue() == namespace);
    }
}
