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

@XmlRootElement(name = "domains")
public class Domains {
   private List<Domain> domains = new ArrayList<Domain>();
   
   public Domains() {
	   Domain d = (Domain)OStore.newStorable(Domain.class);
	   List<IStorable> lst = d.find();
	   for(IStorable s:lst){
		   domains.add((Domain)s);
	   }	   
   }

   public Domains(String name) {
	   Domain d = (Domain)OStore.newStorable(Domain.class);
	   d.setName(name);
	   List<IStorable> lst = d.find();
	   for(IStorable s:lst){
		   domains.add((Domain)s);
	   }	   
   }

   public void setDomains(List<Domain> domains) {
      this.domains = domains;
   } 

   public List<Domain> getDomains() {
      return domains;
   }

}

