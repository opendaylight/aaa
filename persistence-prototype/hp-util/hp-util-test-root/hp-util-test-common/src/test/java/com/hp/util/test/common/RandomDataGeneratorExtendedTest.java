/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test.common;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class RandomDataGeneratorExtendedTest {

    @Test
    public void testGetMacAddress() {
        RandomDataGeneratorExtended randomDataGenerator = new RandomDataGeneratorExtended();
        Assert.assertNotNull(randomDataGenerator.getMacAddress());
    }

    @Test
    public void testGetIpAddress() {
        RandomDataGeneratorExtended randomDataGenerator = new RandomDataGeneratorExtended();
        Assert.assertNotNull(randomDataGenerator.getIpAddress());
    }
}
