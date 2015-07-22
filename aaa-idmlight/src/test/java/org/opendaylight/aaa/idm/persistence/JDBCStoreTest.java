package org.opendaylight.aaa.idm.persistence;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.Users;

public class JDBCStoreTest {
    private JDBCObjectStore store = null;
    @Before
    public void before(){
        File f = new File("idmlight.db.mv.db");
        if(f.exists()){
                        f.delete();
        }
        f = new File("idmlight.db.trace.db");
        if(f.exists()){
            f.delete();
        }
        JDBCObjectStore.clearCache();
        store = new JDBCObjectStore();
    }
    @After
    public void after(){
        store.closeConnection();
        File f = new File("idmlight.db.mv.db");
        if(f.exists()){
                        f.delete();
        }
        f = new File("idmlight.db.trace.db");
        if(f.exists()){
            f.delete();
        }
    }
    @Test
    public void testStoreInitialize() throws StoreException{
        StoreBuilder sb = new StoreBuilder();
        sb.init();
        User user = new User();
        user.setName("admin");
        user = (User) store.getPOJO(user, true);
        Assert.assertEquals(true, user!=null);
        Assert.assertEquals(true, user.getUserid()!=null);
        
        Role role = new Role();
        role.setName("admin");
        role = (Role) store.getPOJO(role, true);
        Assert.assertEquals(true, role!=null);
        Assert.assertEquals(true, role.getRoleid()!=null);

        Domain domain = new Domain();
        domain.setName("sdn");
        domain = (Domain) store.getPOJO(domain, true);
        Assert.assertEquals(true, domain!=null);
        Assert.assertEquals(true, domain.getDomainid()!=null);

        Grant grant = new Grant();
        grant.setRoleid(role.getRoleid());
        grant.setDomainid(domain.getDomainid());
        grant.setUserid(user.getUserid());

        grant = (Grant) store.getPOJO(grant, false);
        Assert.assertEquals(true, user!=null);
        Assert.assertEquals(true, user.getUserid()!=null);
    }
    @Test
    public void testCreateTables() throws StoreException{
        store.dbConnect(new User());
        store.dbConnect(new Role());
        store.dbConnect(new Domain());
        store.dbConnect(new Grant());
    }
    @Test
    public void testCreateObject() throws StoreException{
        User u = new User();
        u.setDescription("Test");
        u.setEmail("test@test.com");
        u.setEnabled(true);
        u.setName("test");
        u.setPassword("test");
        u = (User)store.createPOJO(u);
        Assert.assertEquals(true, u!=null);
        Assert.assertEquals(true, u.getUserid()!=null);
    }
    @Test
    public void testUpdateObject() throws StoreException{
        User u = new User();
        u.setDescription("Test");
        u.setEmail("test@test.com");
        u.setEnabled(true);
        u.setName("test");
        u.setPassword("test");
        u = (User)store.createPOJO(u);
        Assert.assertEquals(true, u!=null);
        Assert.assertEquals(true, u.getUserid()!=null);
        
        User update = new User();
        update.setName(u.getName());
        update.setEmail("test2@test.com");
        store.updatePOJO(update);
        
        User updated = (User)store.getPOJO(update, true);
        Assert.assertEquals(true, updated!=null);
        Assert.assertEquals("test2@test.com",updated.getEmail());
    }
    @Test
    public void testGetObject() throws StoreException{
        User u = new User();
        u.setDescription("Test");
        u.setEmail("test@test.com");
        u.setEnabled(true);
        u.setName("test");
        u.setPassword("test");
        u = (User)store.createPOJO(u);
        Assert.assertEquals(true, u!=null);
        Assert.assertEquals(true, u.getUserid()!=null);

        User stored = (User)store.getPOJO(u, true);
        Assert.assertEquals(true, stored!=null);
        Assert.assertEquals("test@test.com",stored.getEmail());
    }
    @Test
    public void testDeleteObject() throws StoreException{
        User u = new User();
        u.setDescription("Test");
        u.setEmail("test@test.com");
        u.setEnabled(true);
        u.setName("test");
        u.setPassword("test");
        u = (User)store.createPOJO(u);
        Assert.assertEquals(true, u!=null);
        Assert.assertEquals(true, u.getUserid()!=null);

        User deleted = (User)store.deletePOJO(u, true);
        Assert.assertEquals(true, deleted!=null);
        Assert.assertEquals("test@test.com",deleted.getEmail());

        deleted = (User)store.deletePOJO(u, true);
        Assert.assertEquals(true, deleted==null);
    }

    @Test
    public void testGetObjects() throws StoreException{
        StoreBuilder sb = new StoreBuilder();
        sb.init();
        Users users = new Users();
        Assert.assertEquals(2, users.getUsers().size());
    }    
}
