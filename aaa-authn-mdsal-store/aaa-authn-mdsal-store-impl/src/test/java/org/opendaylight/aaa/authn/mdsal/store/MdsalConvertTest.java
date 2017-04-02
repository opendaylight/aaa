/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
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

public class MdsalConvertTest {
    @Test
    public void testConvertDomain() {
        Domain domain = new Domain();
        domain.setDescription("hello");
        domain.setDomainid("hello");
        domain.setEnabled(true);
        domain.setName("Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain mdsalDomain =
                IDMObject2MDSAL.toMDSALDomain(domain);
        Assert.assertNotNull(mdsalDomain);
        Domain d2 = IDMObject2MDSAL.toIDMDomain(mdsalDomain);
        Assert.assertNotNull(d2);
        Assert.assertEquals(domain, d2);
    }

    @Test
    public void testConvertRole() {
        Role role = new Role();
        role.setDescription("hello");
        role.setRoleid("Hello@hello");
        role.setName("Hello");
        role.setDomainid("hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role mdsalRole = IDMObject2MDSAL
                .toMDSALRole(role);
        Assert.assertNotNull(mdsalRole);
        Role r2 = IDMObject2MDSAL.toIDMRole(mdsalRole);
        Assert.assertNotNull(r2);
        Assert.assertEquals(role, r2);
    }

    @Test
    public void testConvertUser() {
        User user = new User();
        user.setDescription("hello");
        user.setDomainid("hello");
        user.setUserid("hello@hello");
        user.setName("Hello");
        user.setEmail("email");
        user.setEnabled(true);
        user.setPassword("pass");
        user.setSalt("salt");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User mdsalUser = IDMObject2MDSAL
                .toMDSALUser(user);
        Assert.assertNotNull(mdsalUser);
        User u2 = IDMObject2MDSAL.toIDMUser(mdsalUser);
        Assert.assertNotNull(u2);
        Assert.assertEquals(user, u2);
    }

    @Test
    public void testConvertGrant() {
        Grant grant = new Grant();
        grant.setDomainid("hello");
        grant.setUserid("hello@hello");
        grant.setRoleid("hello@hello");
        grant.setGrantid("hello@hello@Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims
            .rev141029.authentication.Grant mdsalGrant = IDMObject2MDSAL.toMDSALGrant(grant);
        Assert.assertNotNull(mdsalGrant);
        Grant g2 = IDMObject2MDSAL.toIDMGrant(mdsalGrant);
        Assert.assertNotNull(g2);
        Assert.assertEquals(grant, g2);
    }
}
