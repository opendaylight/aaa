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

@XmlRootElement(name = "grants")
public class Grants {
   private List<Grant> grants = new ArrayList<Grant>();

   public Grants(){
       JDBCObjectStore store = null;
       try {
           store = new JDBCObjectStore();
           List<Object> allgrants = store.getAllObjects(new Grant());
           for(Object g:allgrants){
               grants.add((Grant)g);
           }
       } catch (StoreException e) {
           e.printStackTrace();
       }finally {
           if(store!=null) store.closeConnection();
       }
   }

   public void setGrants(List<Grant> grants) {
      this.grants = grants;
   } 

   public List<Grant> getGrants() {
      return grants;
   }

}

