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

@XmlRootElement(name = "domains")
public class Domains {
   private List<Domain> domains = new ArrayList<Domain>();

   public Domains(){
       JDBCObjectStore store = null;
       try {
           store = new JDBCObjectStore();
           List<Object> alldomains = store.getAllObjects(new Domain());
           for(Object d:alldomains){
               domains.add((Domain)d);
           }
       } catch (StoreException e) {
           e.printStackTrace();
       }finally {
           if(store!=null) store.closeConnection();
       }
   }

   public void setDomains(List<Domain> domains) {
      this.domains = domains;
   } 

   public List<Domain> getDomains() {
      return domains;
   }

}

