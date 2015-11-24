/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.persistence;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.idm.IdmLightProxy;

/*
 * @Author - Sharon Aicler (saichler@cisco.com)
*/
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
        proxy.authenticate(new Creds());
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
        @Override
        public String domain() {
            return "sdn";
        }
    }
}
