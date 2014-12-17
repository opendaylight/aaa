/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.Converter;
import com.hp.util.common.type.Property;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ObjectToStringConverterTest {

    @Test
    public void testToString() {
        ConcreteObject concreteObject = new ConcreteObject(1, true);
        String expected = "ConcreteObject[property1=1, property2=true]";
        Assert.assertEquals(expected, concreteObject.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testToStringInvalidSubject() {
        ObjectToStringConverter.toString(null, Property.valueOf("property", "propertyValue"));
    }

    @Test
    public void testCsvWithConverter() {
        String subject1 = "1";
        String subject2 = null;
        String subject3 = "3";

        Converter<String, String> converter = new Converter<String, String>() {
            @Override
            public String convert(String source) {
                return "subject " + source;
            }
        };

        String expected = "subject 1, null, subject 3";
        Assert.assertEquals(expected, ObjectToStringConverter.toCsv(converter, subject1, subject2, subject3));
        Assert.assertEquals(expected,
                ObjectToStringConverter.toCsv(converter, Arrays.asList(subject1, subject2, subject3)));
        ObjectToStringConverter.toCsv(subject1, Integer.valueOf(1));
    }

    @Test
    public void testCsvWithoutConverter() {
        String subject1 = "1";
        String subject2 = null;
        String subject3 = "3";

        String expected = "1, null, 3";
        Assert.assertEquals(expected, ObjectToStringConverter.toCsv(subject1, subject2, subject3));
        Assert.assertEquals(expected, ObjectToStringConverter.toCsv(Arrays.asList(subject1, subject2, subject3)));
        ObjectToStringConverter.toCsv(subject1, Integer.valueOf(1));
    }

    @Test
    public void testEmptyCsv() {
        String expected = "";
        Assert.assertEquals(expected, ObjectToStringConverter.toCsv());
    }

    @Test
    public void testThrowableToString() {
        Throwable throwable = new Exception("test");
        String str = ObjectToStringConverter.toString(throwable);
        Assert.assertTrue(str.startsWith("java.lang.Exception: test"));
        Assert.assertTrue(str
                .contains("at com.hp.util.common.converter.ObjectToStringConverterTest.testThrowableToString(ObjectToStringConverterTest.java:"));
    }

    @Test
    public void testStackTraceToString() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String str = ObjectToStringConverter.toString(stackTrace);
        Assert.assertTrue(str
                .contains("at com.hp.util.common.converter.ObjectToStringConverterTest.testStackTraceToString(ObjectToStringConverterTest.java:"));
    }

    private static class ConcreteObject {

        private int property1;
        private boolean property2;

        public ConcreteObject(int property1, boolean property2) {
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this, 
                    Property.valueOf("property1", Integer.valueOf(this.property1)),
                    Property.valueOf("property2", Boolean.valueOf(this.property2))
            ); 
        }
    }
}
