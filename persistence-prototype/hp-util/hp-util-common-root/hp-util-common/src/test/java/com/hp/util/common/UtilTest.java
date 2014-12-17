/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class UtilTest {

    @Test
    public void testEquals() {
        Integer obj1 = Integer.valueOf(1);
        Integer obj2 = Integer.valueOf(1);
        Assert.assertTrue(Util.equals(obj1, obj2));

        obj1 = null;
        obj2 = null;
        Assert.assertTrue(Util.equals(obj1, obj2));

        obj1 = Integer.valueOf(1);
        obj2 = Integer.valueOf(2);
        Assert.assertFalse(Util.equals(obj1, obj2));

        obj1 = null;
        obj2 = Integer.valueOf(1);
        Assert.assertFalse(Util.equals(obj1, obj2));

        obj1 = Integer.valueOf(1);
        obj2 = null;
        Assert.assertFalse(Util.equals(obj1, obj2));
    }
}
