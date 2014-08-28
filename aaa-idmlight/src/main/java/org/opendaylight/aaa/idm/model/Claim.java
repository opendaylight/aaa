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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

@XmlRootElement(name = "Claim")
public class Claim {
   private Integer domainid;
   private Integer userid; 
   private String username;
   private List<Role> roles;

   public Integer getDomainid() {
      return domainid;
   }

   public void setDomainid(Integer id) {
      this.domainid = id;
   }

   public Integer getUserid() {
      return userid;
   }

   public void setUserid(Integer id) {
      this.userid = id;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String name) {
      this.username = name;
   }  

   public List<Role> getRoles() {
      return roles;
   }

   public void setRoles(List<Role> roles) {
      this.roles = roles;
   }

}

