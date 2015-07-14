package org.opendaylight.aaa.idm.persistence;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.idm.IdmLightProxy;

public class PasswordHashTest {

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
    }

    @After
    public void after(){
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
    public void testPasswordHash(){
        StoreBuilder b = new StoreBuilder();
        b.init();
        IdmLightProxy proxy = new IdmLightProxy();
        proxy.authenticate(new Creds(), "sdn");
    }
    
    private static class Creds implements PasswordCredentials {
        @Override
        public String username() {
            return "admin";
        }
        @Override
        public String password() {
            return "admin";
        }
    }
}
