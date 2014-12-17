/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.EqualityTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ValueTypeTest {

    @Test(expected = NullPointerException.class)
    public void testInvalidConstruction() {
        final String invalidValue = null;

        @SuppressWarnings("unused")
        ConcreteValueType valueType = new ConcreteValueType(invalidValue);
    }

    @Test
    public void testEqualsAndHashCode() {
        ConcreteValueType obj = new ConcreteValueType("internal representation 1");
        ConcreteValueType equal1 = new ConcreteValueType("internal representation 1");
        ConcreteValueType equal2 = new ConcreteValueType("internal representation 1");
        ConcreteValueType unequal = new ConcreteValueType("internal representation 2");

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
    }

    @Test
    public void testGetValue() {
        Assert.assertEquals("internal representation", new ConcreteValueType("internal representation").getValue());
    }

    @Test
    public void testToString() {
        Assert.assertFalse(new ConcreteValueType("internal representation").toString().isEmpty());
    }

    @Test
    public void testToValue() {
        ConcreteValueType valueType = new ConcreteValueType("internal representation");
        Assert.assertEquals("internal representation", ValueType.toValue(valueType));
    }

    @Test
    public void testToNullValue() {
        ConcreteValueType valueType = null;
        Assert.assertEquals(null, ValueType.toValue(valueType));
    }

    private static class ConcreteValueType extends ValueType<String> {

        public ConcreteValueType(String value) {
            super(value);
        }
    }
}
