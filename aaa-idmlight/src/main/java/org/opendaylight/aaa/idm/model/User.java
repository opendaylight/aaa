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

@XmlRootElement(name = "user")
public class User {
   private Integer userid;
   private String name;
   private String description;
   private Boolean enabled;
   private String email;
   private String password;

   public Integer getUserid() {
      return userid;
   }

   public void setUserid(Integer id) {
      this.userid = id;
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

   public Boolean getEnabled() {
      return enabled;
   }

   public void setEnabled(Boolean enabled) {
      this.enabled = enabled;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getEmail() {
      return email;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getPassword() {
      return password;
   }

}

