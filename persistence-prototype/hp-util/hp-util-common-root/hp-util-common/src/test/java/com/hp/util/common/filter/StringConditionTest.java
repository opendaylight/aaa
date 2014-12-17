/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.filter;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.filter.StringCondition.Mode;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class StringConditionTest {

    @Test
    public void testEqualTo() {
        String value = "value";
        StringCondition condition = StringCondition.equalTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.EQUAL, condition.getMode());
    }

    @Test
    public void testUnequalTo() {
        String value = "value";
        StringCondition condition = StringCondition.unequalTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.UNEQUAL, condition.getMode());
    }

    @Test
    public void testStartWith() {
        String value = "value";
        StringCondition condition = StringCondition.startWith(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.STARTS_WITH, condition.getMode());
    }

    @Test
    public void testContain() {
        String value = "value";
        StringCondition condition = StringCondition.contain(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.CONTAINS, condition.getMode());
    }

    @Test
    public void testEndWith() {
        String value = "value";
        StringCondition condition = StringCondition.endWith(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.ENDS_WITH, condition.getMode());
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<StringCondition> semanticVerifier = new SemanticCompatibilityVerifier<StringCondition>() {
            @Override
            public void assertSemanticCompatibility(StringCondition original, StringCondition replica) {
                Assert.assertEquals(original.getMode(), replica.getMode());
                Assert.assertEquals(original.getValue(), replica.getValue());
            }
        };

        SerializabilityTester.testSerialization(StringCondition.equalTo("value"), semanticVerifier);
    }

    @Test
    public void testToString() {
        StringCondition condition = StringCondition.equalTo("value");
        Assert.assertFalse(condition.toString().isEmpty());
    }
}
