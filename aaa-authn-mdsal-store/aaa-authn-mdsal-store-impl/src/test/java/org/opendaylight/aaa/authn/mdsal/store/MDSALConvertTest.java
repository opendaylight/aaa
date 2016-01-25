/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;

public class MDSALConvertTest {
    @Test
    public void testConvertDomain() {
        Domain d = new Domain();
        d.setDescription("hello");
        d.setDomainid("hello");
        d.setEnabled(true);
        d.setName("Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain mdsalDomain = IDMObject2MDSAL.toMDSALDomain(d);
        Assert.assertNotNull(mdsalDomain);
        Domain d2 = IDMObject2MDSAL.toIDMDomain(mdsalDomain);
        Assert.assertNotNull(d2);
        Assert.assertEquals(d, d2);
    }

    @Test
    public void testConvertRole() {
        Role r = new Role();
        r.setDescription("hello");
        r.setRoleid("Hello@hello");
        r.setName("Hello");
        r.setDomainid("hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role mdsalRole = IDMObject2MDSAL.toMDSALRole(r);
        Assert.assertNotNull(mdsalRole);
        Role r2 = IDMObject2MDSAL.toIDMRole(mdsalRole);
        Assert.assertNotNull(r2);
        Assert.assertEquals(r, r2);
    }

    @Test
    public void testConvertUser() {
        User u = new User();
        u.setDescription("hello");
        u.setDomainid("hello");
        u.setUserid("hello@hello");
        u.setName("Hello");
        u.setEmail("email");
        u.setEnabled(true);
        u.setPassword("pass");
        u.setSalt("salt");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User mdsalUser = IDMObject2MDSAL.toMDSALUser(u);
        Assert.assertNotNull(mdsalUser);
        User u2 = IDMObject2MDSAL.toIDMUser(mdsalUser);
        Assert.assertNotNull(u2);
        Assert.assertEquals(u, u2);
    }

    @Test
    public void testConvertGrant() {
        Grant g = new Grant();
        g.setDomainid("hello");
        g.setUserid("hello@hello");
        g.setRoleid("hello@hello");
        g.setGrantid("hello@hello@Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant mdsalGrant = IDMObject2MDSAL.toMDSALGrant(g);
        Assert.assertNotNull(mdsalGrant);
        Grant g2 = IDMObject2MDSAL.toIDMGrant(mdsalGrant);
        Assert.assertNotNull(g2);
        Assert.assertEquals(g, g2);
    }
}
