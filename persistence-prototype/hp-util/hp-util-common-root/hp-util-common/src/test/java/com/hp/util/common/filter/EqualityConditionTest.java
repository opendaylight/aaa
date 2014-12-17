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
import com.hp.util.common.filter.EqualityCondition.Mode;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class EqualityConditionTest {

    @Test
    public void testEqualTo() {
        Integer value = Integer.valueOf(1);
        EqualityCondition<Integer> condition = EqualityCondition.equalTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.EQUAL, condition.getMode());
    }

    @Test
    public void testUnequalTo() {
        Integer value = Integer.valueOf(1);
        EqualityCondition<Integer> condition = EqualityCondition.unequalTo(value);
        Assert.assertEquals(value, condition.getValue());
        Assert.assertEquals(Mode.UNEQUAL, condition.getMode());
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
        final EqualityCondition<Integer> condition = EqualityCondition.equalTo(value);
        EqualityCondition<String> convertedCondition = condition.convert(converter);
        Assert.assertEquals(converter.convert(condition.getValue()), convertedCondition.getValue());
        Assert.assertEquals(condition.getMode(), convertedCondition.getMode());
    }

    @Test
    public void testInvalidConvert() {
        final Converter<Integer, String> invalidConverter = null;
        final EqualityCondition<Integer> condition = EqualityCondition.equalTo(Integer.valueOf(1));

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                condition.convert(invalidConverter);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<EqualityCondition<Integer>> semanticVerifier = new SemanticCompatibilityVerifier<EqualityCondition<Integer>>() {
            @Override
            public void assertSemanticCompatibility(EqualityCondition<Integer> original,
                    EqualityCondition<Integer> replica) {
                Assert.assertEquals(original.getMode(), replica.getMode());
                Assert.assertEquals(original.getValue(), replica.getValue());
            }
        };

        SerializabilityTester.testSerialization(EqualityCondition.equalTo(Integer.valueOf(1)), semanticVerifier);
    }

    @Test
    public void testToString() {
        EqualityCondition<Integer> condition = EqualityCondition.equalTo(Integer.valueOf(1));
        Assert.assertFalse(condition.toString().isEmpty());
    }
}
