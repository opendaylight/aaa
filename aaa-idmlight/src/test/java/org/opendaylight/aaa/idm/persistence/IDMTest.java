package org.opendaylight.aaa.idm.persistence;

import java.io.File;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.rest.UserHandler;

public class IDMTest {
    @BeforeClass
    public static void start() {
        File f = new File("idmlight.db.mv.db");
        if(f.exists()){
            f.delete();
        }
        f = new File("idmlight.db.trace.db");
        if(f.exists()){
            f.delete();
        }
    }

    @AfterClass
    public static void end() {
        File f = new File("idmlight.db.mv.db");
        if(f.exists()){
            f.delete();
        }
        f = new File("idmlight.db.trace.db");
        if(f.exists()){
            f.delete();
        }
    }

    @Before
    public void before() throws StoreException, SQLException{
    	UserStore us = new UserStore();
    	us.dbClean();
    	DomainStore ds = new DomainStore();
    	ds.dbClean();
    	RoleStore rs = new RoleStore();
    	rs.dbClean();
    	GrantStore gs = new GrantStore();
    	gs.dbClean();
    }

    @Test
    public void testCreateDefaultDomain() {
        Domain d = new Domain();
        Assert.assertEquals(true, d != null);
        DomainStore ds = new DomainStore();
        d.setName(StoreBuilder.DEFAULT_DOMAIN);
        d.setEnabled(true);
        try {
			d = ds.createDomain(d);
		} catch (StoreException e) {
			e.printStackTrace();
		}
        Assert.assertEquals(true, d != null);
    }

    @Test
    public void testCreateTempRole() throws StoreException {
        Role role = StoreBuilder.createRole("temp","temp domain","Temp Testing role");
        Assert.assertEquals(true, role != null);
    }

    @Test
    public void testCreateUser() throws StoreException {
        User user = StoreBuilder.createUser("test","pass","domain","desc","email",true,"SALT");
        Assert.assertEquals(true, user != null);
    }

    @Test
    public void testCreateGrant() throws StoreException {
        Domain d = StoreBuilder.createDomain("sdn",true);
        Role role = StoreBuilder.createRole("temp","temp domain","Temp Testing role");
        User user = StoreBuilder.createUser("test","pass","domain","desc","email",true,"SALT");
        Grant g = StoreBuilder.createGrant(d.getDomainid(), user.getUserid(), role.getRoleid());
        Assert.assertEquals(true, g != null);
    }

    @Test
    public void testUpdatingUserEmail() throws StoreException {
    	UserStore us = new UserStore();
        Domain d = StoreBuilder.createDomain("sdn",true);
        Role role = StoreBuilder.createRole("temp",d.getDomainid(),"Temp Testing role");
        User user = StoreBuilder.createUser("test","pass",d.getDomainid(),"desc","email",true,"SALT");
        Grant g = StoreBuilder.createGrant(d.getDomainid(), user.getUserid(), role.getRoleid());

        user.setName("test");
        user = us.putUser(user);
        Assert.assertEquals(true, user != null);

        user.setEmail("Test@Test.com");
        user = us.putUser(user);

        user = new User();
        user.setName("test");
        user.setDomainID(d.getDomainid());
        user = us.getUser(user.getName()+"@"+user.getDomainID());

        Assert.assertEquals("Test@Test.com", user.getEmail());
    }

    @Test
    public void testCreateUserViaAPI() throws StoreException {
        Domain d = StoreBuilder.createDomain("sdn",true);

        User user = new User();
        user.setName("Hello");
        user.setPassword("Hello");
        user.setDomainID(d.getDomainid());
        UserHandler h = new UserHandler();
        h.createUser(null, user);

        User u = new User();
        u.setName("Hello");
        u.setDomainID(d.getDomainid());
        UserStore us = new UserStore();
        u = us.getUser(u.getName()+"@"+u.getDomainID());

        Assert.assertEquals(true, u != null);
    }

    @Test
    public void testUpdateUserViaAPI() throws StoreException {
        Domain d = StoreBuilder.createDomain("sdn",true);

        User user = new User();
        user.setName("Hello");
        user.setPassword("Hello");
        user.setDomainID(d.getDomainid());
        UserHandler h = new UserHandler();
        h.createUser(null, user);

        user.setEmail("Hello@Hello.com");
        user.setPassword("Test123");
        h.putUser(null, user, "" + user.getUserid());

        UserStore us = new UserStore();

        User u = new User();
        u.setName("Hello");
        u.setDomainID(d.getDomainid());
        u = us.getUser(u.getName()+"@"+u.getDomainID());

        Assert.assertEquals("Hello@Hello.com", u.getEmail());

        String hash = SHA256Calculator.getSHA256("Test123", u.getSalt());
        Assert.assertEquals(u.getPassword(), hash);
    }

    @Test
    public void testUpdateUserRoleViaAPI() throws StoreException {
    	Domain d = StoreBuilder.createDomain("sdn",true);
    	Role role1 = StoreBuilder.createRole("temp1",d.getDomainid(),"Temp Testing role");
    	Role role2 = StoreBuilder.createRole("temp2",d.getDomainid(),"Temp Testing role");

        User user = new User();
        user.setName("Hello");
        user.setPassword("Hello");
        user.setDomainID(d.getDomainid());

        UserHandler h = new UserHandler();
        h.createUser(null, user);

        user.setEmail("Hello@Hello.com");
        user.setPassword("Test123");
        h.putUser(null, user, user.getUserid());

        Grant g = new Grant();
        g.setUserid(user.getUserid());
        g.setDomainid(d.getDomainid());
        g.setRoleid(role1.getRoleid());
        GrantStore gs = new GrantStore();
        g = gs.createGrant(g);

        Assert.assertEquals(true, g != null);
        Assert.assertEquals(g.getRoleid(), role1.getRoleid());

        g = new Grant();
        g.setUserid(user.getUserid());
        g.setDomainid(d.getDomainid());
        g.setRoleid(role1.getRoleid());
        gs.deleteGrant(g);
        g.setRoleid(role2.getRoleid());
        g = gs.createGrant(g);

        Assert.assertEquals(true, g != null);
        Assert.assertEquals(g.getRoleid(), role2.getRoleid());

        User u = new User();
        u.setName("Hello");
        u.setDomainID(d.getDomainid());
        UserStore us = new UserStore();
        u = us.getUser(u.getName()+"@"+u.getDomainID());

        Assert.assertEquals("Hello@Hello.com", u.getEmail());

        String hash = SHA256Calculator.getSHA256("Test123", u.getSalt());
        Assert.assertEquals(true, hash.equals(u.getPassword()));
    }
}
