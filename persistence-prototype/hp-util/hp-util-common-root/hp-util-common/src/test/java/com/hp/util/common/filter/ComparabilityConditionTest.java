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

import com.hp.util.common.Converter;
import com.hp.util.common.filter.ComparabilityCondition.Mode;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ComparabilityConditionTest {

    @Test
    public void testLessThan() {
        Integer value = Integer.valueOf(1);
        ComparabilityCondition<Integer> condition = ComparabilityCondition.lessThan(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.LESS_THAN, condition.getMode());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                ComparabilityCondition.lessThan(null);
            }
        });
    }

    @Test
    public void testLessThanOrEqualTo() {
        Integer value = Integer.valueOf(1);
        ComparabilityCondition<Integer> condition = ComparabilityCondition.lessThanOrEqualTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.LESS_THAN_OR_EQUAL_TO, condition.getMode());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                ComparabilityCondition.lessThanOrEqualTo(null);
            }
        });
    }

    @Test
    public void testEqualTo() {
        Integer value = Integer.valueOf(1);
        ComparabilityCondition<Integer> condition = ComparabilityCondition.equalTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.EQUAL, condition.getMode());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                ComparabilityCondition.equalTo(null);
            }
        });
    }

    @Test
    public void testGreaterThanOrEqualTo() {
        Integer value = Integer.valueOf(1);
        ComparabilityCondition<Integer> condition = ComparabilityCondition.greaterThanOrEqualTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.GREATER_THAN_OR_EQUAL_TO, condition.getMode());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                ComparabilityCondition.greaterThanOrEqualTo(null);
            }
        });
    }

    @Test
    public void testGreaterThan() {
        Integer value = Integer.valueOf(1);
        ComparabilityCondition<Integer> condition = ComparabilityCondition.greaterThan(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.GREATER_THAN, condition.getMode());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                ComparabilityCondition.greaterThan(null);
            }
        });
    }

    @Test
    public void testConvert() {
        Converter<Integer, String> converter = new Converter<Integer, String>() {
            @Override
            public String convert(Integer source) {
                return source.toString();
            }
        };

        Integer value = Integer.valueOf(1);
        final ComparabilityCondition<Integer> condition = ComparabilityCondition.equalTo(value);
        ComparabilityCondition<String> convertedCondition = condition.convert(converter);
        Assert.assertEquals(converter.convert(condition.getValue()), convertedCondition.getValue());
        Assert.assertEquals(condition.getMode(), convertedCondition.getMode());
    }

    @Test
    public void testInvalidConvert() {
        final Converter<Integer, String> invalidConverter = null;
        final ComparabilityCondition<Integer> condition = ComparabilityCondition.equalTo(Integer.valueOf(1));

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                condition.convert(invalidConverter);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<ComparabilityCondition<Integer>> semanticVerifier = new SemanticCompatibilityVerifier<ComparabilityCondition<Integer>>() {
            @Override
            public void assertSemanticCompatibility(ComparabilityCondition<Integer> original,
                    ComparabilityCondition<Integer> replica) {
                Assert.assertEquals(original.getMode(), replica.getMode());
                Assert.assertEquals(original.getValue(), replica.getValue());
            }
        };

        SerializabilityTester.testSerialization(ComparabilityCondition.equalTo(Integer.valueOf(1)),
                semanticVerifier);
    }

    @Test
    public void testToString() {
        ComparabilityCondition<Integer> condition = ComparabilityCondition.equalTo(Integer.valueOf(1));
        Assert.assertFalse(condition.toString().isEmpty());
    }
}
