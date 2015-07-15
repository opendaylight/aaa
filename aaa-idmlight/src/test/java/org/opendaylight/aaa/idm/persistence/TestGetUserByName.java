/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.aaa.idm.persistence;
import java.io.File;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.rest.UserHandler;

/*
 * @Author - Sharon Aicler (saichler@cisco.com)
*/
public class TestGetUserByName {

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
    public void testGetUserByName(){
        StoreBuilder b = new StoreBuilder();
        b.init();
        UserStore us = new UserStore();
        try {
            User user = us.findUserByName("admin");
            Assert.assertEquals(true, user!=null);
        } catch (StoreException e) {
            e.printStackTrace();
            Assert.assertEquals(true, false);
        }
    }

    @Test
    public void testGetUserByNameViaAPI(){
        StoreBuilder b = new StoreBuilder();
        b.init();
        UserHandler uh = new UserHandler();
        Response user = uh.getUser("=admin");
        Assert.assertEquals(200, user.getStatus());
    }    
}