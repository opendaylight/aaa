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

@XmlRootElement(name = "roles")
public class Roles {
   private List<Role> roles = new ArrayList<Role>();

   public Roles(){
       JDBCObjectStore store = null;
       try {
           store = new JDBCObjectStore();
           List<Object> allroles = store.getAllObjects(new Role());
           for(Object r:allroles){
               roles.add((Role)r);
           }
       } catch (StoreException e) {
           e.printStackTrace();
       }finally {
           if(store!=null) store.closeConnection();
       }
   }

   public void setRoles(List<Role> roles) {
      this.roles = roles;
   } 

   public List<Role> getRoles() {
      return roles;
   }

}

