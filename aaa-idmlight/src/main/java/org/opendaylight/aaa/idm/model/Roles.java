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

@XmlRootElement(name = "roles")
public class Roles {
   private List<JSRole> roles = new ArrayList<JSRole>();

   public Roles(){
	   Role r = (Role)OStore.newStorable(Role.class);
	   List<IStorable> lst = r.find();
	   for(IStorable s:lst){
		   roles.add(JSRole.create((Role)s));
	   }
   }
   
   public void setRoles(List<JSRole> roles) {
      this.roles = roles;
   } 

   public List<JSRole> getRoles() {
      return roles;
   }

}

