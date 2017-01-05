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
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class ServiceProxyTest {

    @Test
    public void testGetInstance() {
        // ensures that singleton pattern is working
        assertNotNull(ServiceProxy.getInstance());
    }

    @Test
    public void testGetSetEnabled() {
        // combines set and get tests. These are important in this instance,
        // because getEnabled allows an optional callback Filter.
        ServiceProxy.getInstance().setEnabled(true);
        assertTrue(ServiceProxy.getInstance().getEnabled(null));

        AAAFilter testFilter = new AAAFilter();
        // register the filter
        ServiceProxy.getInstance().getEnabled(testFilter);
        assertTrue(testFilter.isEnabled());

        ServiceProxy.getInstance().setEnabled(false);
        assertFalse(ServiceProxy.getInstance().getEnabled(testFilter));
        assertFalse(testFilter.isEnabled());
    }
}
