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

import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.OStore;

@XmlRootElement(name = "grants")
public class Grants {
   private List<JSGrant> grants = new ArrayList<JSGrant>();

   public Grants(int domainID,int userID){
	   Grant g = (Grant)OStore.newStorable(Grant.class);
	   g.setDomainid(domainID);
	   g.setUserid(userID);
	   List<IStorable> lst = g.find();
	   for(IStorable s:lst){
		   grants.add(JSGrant.create((Grant)s));
	   } 
   }

   public Grants(int userID){
	   Grant g = (Grant)OStore.newStorable(Grant.class);
	   g.setUserid(userID);
	   List<IStorable> lst = g.find();
	   for(IStorable s:lst){
		   grants.add(JSGrant.create((Grant)s));
	   } 
   }
      
   public void setGrants(List<JSGrant> grants) {
      this.grants = grants;
   } 

   public List<JSGrant> getGrants() {
      return grants;
   }

}

