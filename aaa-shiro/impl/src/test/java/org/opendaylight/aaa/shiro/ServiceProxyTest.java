/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.aaa.shiro.filters.AAAFilter;

/**
 * Test for the ServiceProxy.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class ServiceProxyTest {

    @Test
    public void testGetInstance() {
        // ensures that singleton pattern is working
        assertNotNull(org.opendaylight.aaa.shiro.ServiceProxy.getInstance());
    }

    @Test
    public void testGetSetEnabled() {
        // combines set and get tests. These are important in this instance,
        // because getEnabled allows an optional callback Filter.
        org.opendaylight.aaa.shiro.ServiceProxy.getInstance().setEnabled(true);
        assertTrue(org.opendaylight.aaa.shiro.ServiceProxy.getInstance().getEnabled(null));

        AAAFilter testFilter = new AAAFilter();
        // register the filter
        org.opendaylight.aaa.shiro.ServiceProxy.getInstance().getEnabled(testFilter);
        assertTrue(testFilter.isEnabled());

        org.opendaylight.aaa.shiro.ServiceProxy.getInstance().setEnabled(false);
        assertFalse(org.opendaylight.aaa.shiro.ServiceProxy.getInstance().getEnabled(testFilter));
        assertFalse(testFilter.isEnabled());
    }
}
