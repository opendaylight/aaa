/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;
import java.util.Collection;
import org.junit.Test;

/**
 * A few basic test cases for the DefualtRBACRules singleton container.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class DefaultRBACRulesTest {

    @Test
    public void testGetInstance() {
        assertNotNull(DefaultRBACRules.getInstance());
        assertEquals(DefaultRBACRules.getInstance(), DefaultRBACRules.getInstance());
    }

    @Test
    public void testGetRBACRules() {
        Collection<RBACRule> rbacRules = DefaultRBACRules.getInstance().getRBACRules();
        assertNotNull(rbacRules);

        // check that a copy was returned
        int originalSize = rbacRules.size();
        rbacRules.add(RBACRule.createAuthorizationRule("fakeurl/*", Sets.newHashSet("admin")));
        assertEquals(originalSize, DefaultRBACRules.getInstance().getRBACRules().size());
    }

}
