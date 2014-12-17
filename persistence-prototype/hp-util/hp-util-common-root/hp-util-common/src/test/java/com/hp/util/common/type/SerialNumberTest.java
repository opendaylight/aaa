/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class SerialNumberTest {

    @Test
    public void testValueOf() {
        String expected = "ASD123";
        SerialNumber serialNumber = SerialNumber.valueOf(expected);
        Assert.assertEquals(expected, serialNumber.getValue());
    }
}
