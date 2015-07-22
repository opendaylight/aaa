/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.model;

/**
 *
 * @author peter.mellquist@hp.com 
 *
 */


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.aaa.idm.persistence.JDBCObjectStore;
import org.opendaylight.aaa.idm.persistence.StoreException;

@XmlRootElement(name = "users")
public class Users {
   private List<User> users = new ArrayList<User>();
   
   public Users(){
       JDBCObjectStore store = null;
       try {
           store = new JDBCObjectStore();
           List<Object> allusers = store.getAllObjects(new User());
           for(Object u:allusers){
               users.add((User)u);
           }
       } catch (StoreException e) {
           e.printStackTrace();
       }finally {
           if(store!=null) store.closeConnection();
       }
   }
   
   public void setUsers(List<User> users) {
      this.users = users;
   } 

   public List<User> getUsers() {
      return users;
   }

}

