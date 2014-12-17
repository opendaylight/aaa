/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.Converter;
import com.hp.util.common.type.Interval.Type;
import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class IntervalTest {

    @Test
    public void testConstruction() {
        Interval<Integer> interval = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(1), Type.CLOSED);
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(1), interval.getRightEndpoint());
        Assert.assertEquals(Type.CLOSED, interval.getType());
    }

    @SuppressWarnings("unused")
    @Test
    public void testInvalidConstruction() {
        try {
            Interval<Integer> interval = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(1), null);
            Assert.fail("Invalid interval type, exception expected");
        }
        catch (Exception e) {
            Assert.assertTrue(NullPointerException.class.isInstance(e));
        }

        for (Type type : Arrays.asList(Type.OPEN, Type.CLOSED,
            Type.LEFT_CLOSED_RIGHT_OPEN, Type.LEFT_OPEN_RIGHT_CLOSED)) {
            try {
                Interval<Integer> interval = new Interval<Integer>(null, null, type);
                Assert.fail("Invalid limits for type " + interval.getType() + ", exception expected");
            }
            catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }

            try {
                Interval<Integer> interval = new Interval<Integer>(null, Integer.valueOf(1), type);
                Assert.fail("Invalid leftpoint for type " + interval.getType() + ", exception expected");
            }
            catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }

            try {
                Interval<Integer> interval = new Interval<Integer>(Integer.valueOf(1), null, type);
                Assert.fail("Invalid rightpoint for type " + interval.getType() + ", exception expected");
            }
            catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }

            try {
                Interval<Integer> interval = new Interval<Integer>(Integer.valueOf(1), Integer.valueOf(0), type);
                Assert.fail("Invalid interval for type " + interval.getType() + ", exception expected");
            }
            catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }
        }

        for (Type type : Arrays.asList(Type.LEFT_OPEN_RIGHT_UNBOUNDED,
            Type.LEFT_CLOSED_RIGHT_UNBOUNDED)) {
            try {
                Interval<Integer> interval = new Interval<Integer>(null, null, type);
                Assert.fail("Invalid leftpoint for type " + interval.getType() + ", exception expected");
            }
            catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }
        }

        for (Type type : Arrays.asList(Type.LEFT_UNBOUNDED_RIGHT_OPEN,
            Type.LEFT_UNBOUNDED_RIGHT_CLOSED)) {
            try {
                Interval<Integer> interval = new Interval<Integer>(null, null, type);
                Assert.fail("Invalid rightpoint for type " + interval.getType() + ", exception expected");
            }
            catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }
        }
    }

    @Test
    public void testOpen() {
        Interval<Integer> interval = Interval.open(Integer.valueOf(0), Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Type.OPEN, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertFalse(interval.contains(Integer.valueOf(0)));
        Assert.assertFalse(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testClosed() {
        Interval<Integer> interval = Interval.closed(Integer.valueOf(0), Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Type.CLOSED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testLeftClosedRightOpen() {
        Interval<Integer> interval = Interval.leftClosedRightOpen(Integer.valueOf(0), Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Type.LEFT_CLOSED_RIGHT_OPEN, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertFalse(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testLeftOpenRightClosed() {
        Interval<Integer> interval = Interval.leftOpenRightClosed(Integer.valueOf(0), Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Type.LEFT_OPEN_RIGHT_CLOSED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertFalse(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testLeftOpenRightUnbounded() {
        Interval<Integer> interval = Interval.leftOpenRightUnbounded(Integer.valueOf(0));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertNull(interval.getRightEndpoint());
        Assert.assertEquals(Type.LEFT_OPEN_RIGHT_UNBOUNDED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(10)));

        Assert.assertFalse(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testLeftClosedRightUnbounded() {
        Interval<Integer> interval = Interval.leftClosedRightUnbounded(Integer.valueOf(0));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertNull(interval.getRightEndpoint());
        Assert.assertEquals(Type.LEFT_CLOSED_RIGHT_UNBOUNDED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testLeftUnboundedRightOpen() {
        Interval<Integer> interval = Interval.leftUnboundedRightOpen(Integer.valueOf(9));
        Assert.assertNull(interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Type.LEFT_UNBOUNDED_RIGHT_OPEN, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertFalse(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testLeftUnboundedRightClosed() {
        Interval<Integer> interval = Interval.leftUnboundedRightClosed(Integer.valueOf(9));
        Assert.assertNull(interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Type.LEFT_UNBOUNDED_RIGHT_CLOSED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testUnbounded() {
        Interval<Integer> interval = Interval.unbounded();
        Assert.assertNull(interval.getLeftEndpoint());
        Assert.assertNull(interval.getRightEndpoint());
        Assert.assertEquals(Type.UNBOUNDED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(-1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testCreateOpen() {
        Interval<Integer> interval = Interval.createOpen(null, null);
        Assert.assertEquals(Interval.Type.UNBOUNDED, interval.getType());

        Integer start = Integer.valueOf(1);

        interval = Interval.createOpen(start, null);
        Assert.assertEquals(Interval.Type.LEFT_OPEN_RIGHT_UNBOUNDED, interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());

        Integer end = Integer.valueOf(2);

        interval = Interval.createOpen(null, end);
        Assert.assertEquals(Interval.Type.LEFT_UNBOUNDED_RIGHT_OPEN, interval.getType());
        Assert.assertEquals(end, interval.getRightEndpoint());

        interval = Interval.createOpen(start, end);
        Assert.assertEquals(Interval.Type.OPEN, interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());
        Assert.assertEquals(end, interval.getRightEndpoint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCreateOpen() {
        Interval.createOpen(Integer.valueOf(2), Integer.valueOf(1));
    }

    @Test
    public void testCreateClosed() {
        Interval<Integer> interval = Interval.createClosed(null, null);
        Assert.assertEquals(Interval.Type.UNBOUNDED, interval.getType());

        Integer start = Integer.valueOf(1);

        interval = Interval.createClosed(start, null);
        Assert.assertEquals(Interval.Type.LEFT_CLOSED_RIGHT_UNBOUNDED, interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());

        Integer end = Integer.valueOf(2);

        interval = Interval.createClosed(null, end);
        Assert.assertEquals(Interval.Type.LEFT_UNBOUNDED_RIGHT_CLOSED, interval.getType());
        Assert.assertEquals(end, interval.getRightEndpoint());

        interval = Interval.createClosed(start, end);
        Assert.assertEquals(Interval.Type.CLOSED, interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());
        Assert.assertEquals(end, interval.getRightEndpoint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCreateClosed() {
        Interval.createClosed(Integer.valueOf(2), Integer.valueOf(1));
    }

    @Test
    public void testIsEmpty() {
        Interval<Integer> interval = Interval.open(Integer.valueOf(0), Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.closed(Integer.valueOf(0), Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.leftClosedRightOpen(Integer.valueOf(0), Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.leftOpenRightClosed(Integer.valueOf(0), Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.leftOpenRightUnbounded(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.leftClosedRightUnbounded(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.leftUnboundedRightOpen(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.leftUnboundedRightClosed(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.unbounded();
        Assert.assertFalse(interval.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        Interval<Integer> obj = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(1), Type.CLOSED);
        Interval<Integer> equal1 = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(1), Type.CLOSED);
        Interval<Integer> equal2 = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(1), Type.CLOSED);
        Interval<Integer> unequal1 = new Interval<Integer>(Integer.valueOf(-1), Integer.valueOf(1), Type.CLOSED);
        Interval<Integer> unequal2 = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(2), Type.CLOSED);
        Interval<Integer> unequal3 = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(1), Type.OPEN);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2, unequal3);
    }

    @Test
    public void testConvert() {
        Converter<Integer, String> converter = new Converter<Integer, String>() {
            @Override
            public String convert(Integer source) {
                return source.toString();
            }
        };

        Integer leftEndpoint = Integer.valueOf(1);
        Integer rightEndpoint = Integer.valueOf(2);
        final Interval<Integer> interval = Interval.open(leftEndpoint, rightEndpoint);
        Interval<String> convertedInterval = interval.convert(converter);
        Assert.assertEquals(converter.convert(leftEndpoint), convertedInterval.getLeftEndpoint());
        Assert.assertEquals(converter.convert(rightEndpoint), convertedInterval.getRightEndpoint());
        Assert.assertEquals(interval.getType(), convertedInterval.getType());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                interval.convert(null);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Interval<?>> semanticVerifier = new SemanticCompatibilityVerifier<Interval<?>>() {
            @Override
            public void assertSemanticCompatibility(Interval<?> original, Interval<?> replica) {
                Assert.assertEquals(original.getLeftEndpoint(), replica.getLeftEndpoint());
                Assert.assertEquals(original.getRightEndpoint(), replica.getRightEndpoint());
                Assert.assertEquals(original.getType(), replica.getType());
            }
        };

        SerializabilityTester.testSerialization(new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(3),
                Type.CLOSED), semanticVerifier);
    }

    @Test
    public void testToString() {
        Interval<Integer> interval = new Interval<Integer>(Integer.valueOf(0), Integer.valueOf(3), Type.CLOSED);
        Assert.assertFalse(interval.toString().isEmpty());
    }
}
