/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.type.SerialNumber;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class SerialNumberStringConverterTest {

    @Test
    public void testConversion() {
        BidirectionalConverter<SerialNumber, String> converter = SerialNumberStringConverter.getInstance();
        SerialNumber expected = SerialNumber.valueOf("serial-number");
        SerialNumber actual = converter.restore(converter.convert(expected));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertNull() {
        String actual = SerialNumberStringConverter.getInstance().convert(null);
        Assert.assertNull(actual);
    }

    @Test
    public void testRestoreNull() {
        SerialNumber actual = SerialNumberStringConverter.getInstance().restore(null);
        Assert.assertNull(actual);
    }
}
