/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.model;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "role")
public class Role {
   private String roleid;
   private String name;
   private String description;
   private String domainid;

   public String getRoleid() {
      return roleid;
   }

   public void setRoleid(String id) {
      this.roleid = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public void setDomainID(String _domainid){
       this.domainid = _domainid;
   }

   public String getDomainID(){
       return this.domainid;
   }
}

