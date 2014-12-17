/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class TestUtilTest {

    @Test
    public void testGetSetPrivateField() throws Exception {
        Object object = new Object() {
            @SuppressWarnings("unused")
            private String field = "Hello World";
        };

        final String fieldName = "field";

        Assert.assertEquals("Hello World", TestUtil.getPrivateField(fieldName, object));

        final String newValue = "Goodbye world";
        TestUtil.setPrivateField(fieldName, newValue, object);
        Assert.assertEquals(newValue, TestUtil.getPrivateField(fieldName, object));
    }

    @Test
    public void testGetExecutingMethod() {
        StackTraceElement executingMethod = TestUtil.getExecutingMethod();
        Assert.assertEquals(getClass().getName(), executingMethod.getClassName());
        Assert.assertEquals("testGetExecutingMethod", executingMethod.getMethodName());
    }
}
