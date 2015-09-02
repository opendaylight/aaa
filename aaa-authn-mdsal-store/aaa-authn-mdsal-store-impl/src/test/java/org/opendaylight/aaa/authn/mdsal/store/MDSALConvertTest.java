package org.opendaylight.aaa.authn.mdsal.store;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.aaa.authn.mdsal.store.IDMObject2MDSAL;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;

public class MDSALConvertTest {
    @Test
    public void testConvertDomain(){
        Domain d = new Domain();
        d.setDescription("hello");
        //d.setDomainid(54545);
        d.setEnabled(true);
        d.setName("Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain mdsalDomain = IDMObject2MDSAL.toMDSALDomain(d);
        Assert.assertNotNull(mdsalDomain);
        Domain d2 = IDMObject2MDSAL.toIDMDomain(mdsalDomain);
        Assert.assertNotNull(d2);
        Assert.assertEquals(d, d2);
    }
    @Test
    public void testConvertRole(){
        Role r = new Role();
        r.setDescription("hello");
        //r.setRoleid(54545);
        r.setName("Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role mdsalRole = IDMObject2MDSAL.toMDSALRole(r);
        Assert.assertNotNull(mdsalRole);
        Role r2 = IDMObject2MDSAL.toIDMRole(mdsalRole);
        Assert.assertNotNull(r2);
        Assert.assertEquals(r, r2);
    }
    @Test
    public void testConvertUser(){
        User u = new User();
        u.setDescription("hello");
        //u.setUserid(54545);
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
    /*
    @Test
    public void testConvertGrant(){
        Grant g = new Grant();
        g.setDescription("hello");
        //d.setRoleid(54545);
        r.setName("Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role mdsalRole = IDMObject2MDSAL.toMDSALRole(r);
        Assert.assertNotNull(mdsalRole);
        Role r2 = IDMObject2MDSAL.toIDMRole(mdsalRole);
        Assert.assertNotNull(r2);
        Assert.assertEquals(r, r2);
    }*/
}
