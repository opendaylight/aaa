/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.auth;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class UsernameTest {

    @Test
    public void testValueOf() {
        Username username = Username.valueOf("user");
        Assert.assertEquals("user", username.getValue());
    }

    @Test
    public void testValueOfInvalid() {
        // null value is already tested by SerializableValueTypeTest
        final String invalidValue = "";

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Username.valueOf(invalidValue);
            }
        });
    }
}
