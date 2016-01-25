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
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User;

public class IDMStoreTest {

    @Test
    public void testWriteDomain() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoFordomain();
        Domain domain = testedObject.writeDomain(util.domain);
        Assert.assertNotNull(domain);
        Assert.assertEquals(domain.getDomainid(), util.domain.getName());
    }

    @Test
    public void testReadDomain() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoFordomain();
        Domain domain = testedObject.readDomain(util.domain.getDomainid());
        Assert.assertNotNull(domain);
        Assert.assertEquals(domain, util.domain);
    }

    @Test
    public void testDeleteDomain() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoFordomain();
        Domain domain = testedObject.deleteDomain(util.domain.getDomainid());
        Assert.assertEquals(domain, util.domain);
    }

    @Test
    public void testUpdateDomain() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoFordomain();
        Domain domain = testedObject.updateDomain(util.domain);
        Assert.assertEquals(domain, util.domain);
    }

    @Test
    public void testWriteRole() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForrole();
        util.addMokitoFordomain();
        Role role = testedObject.writeRole(util.role);
        Assert.assertNotNull(role);
        Assert.assertEquals(role.getRoleid(),
                IDMStoreUtil.createRoleid(role.getName(), role.getDomainid()));
    }

    @Test
    public void testReadRole() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForrole();
        Role role = testedObject.readRole(util.role.getRoleid());
        Assert.assertNotNull(role);
        Assert.assertEquals(role, util.role);
    }

    @Test
    public void testDeleteRole() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForrole();
        Role role = testedObject.deleteRole(util.role.getRoleid());
        Assert.assertNotNull(role);
        Assert.assertEquals(role, util.role);
    }

    @Test
    public void testUpdateRole() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForrole();
        Role role = testedObject.updateRole(util.role);
        Assert.assertNotNull(role);
        Assert.assertEquals(role, util.role);
    }

    @Test
    public void testWriteUser() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForuser();
        User user = testedObject.writeUser(util.user);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUserid(),
                IDMStoreUtil.createUserid(user.getName(), util.user.getDomainid()));
    }

    @Test
    public void testReadUser() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForuser();
        User user = testedObject.readUser(util.user.getUserid());
        Assert.assertNotNull(user);
        Assert.assertEquals(user, util.user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForuser();
        User user = testedObject.deleteUser(util.user.getUserid());
        Assert.assertNotNull(user);
        Assert.assertEquals(user, util.user);
    }

    @Test
    public void testUpdateUser() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForuser();
        User user = testedObject.updateUser(util.user);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getPassword(),
                SHA256Calculator.getSHA256(util.user.getPassword(), util.user.getSalt()));
    }

    @Test
    public void testWriteGrant() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoFordomain();
        util.addMokitoForrole();
        util.addMokitoForuser();
        util.addMokitoForgrant();
        Grant grant = testedObject.writeGrant(util.grant);
        Assert.assertNotNull(grant);
    }

    @Test
    public void testReadGrant() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForgrant();
        Grant grant = testedObject.readGrant(util.grant.getGrantid());
        Assert.assertNotNull(grant);
        Assert.assertEquals(grant, util.grant);
    }

    @Test
    public void testDeleteGrant() throws Exception {
        IDMStoreTestUtil util = new IDMStoreTestUtil();
        IDMMDSALStore testedObject = new IDMMDSALStore(util.dataBroker);
        util.addMokitoForgrant();
        Grant grant = testedObject.deleteGrant(util.grant.getGrantid());
        Assert.assertNotNull(grant);
        Assert.assertEquals(grant, util.grant);
    }
}
