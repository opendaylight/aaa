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

@XmlRootElement(name = "user")
public class User {
   private String userid;
   private String name;
   private String description;
   private Boolean enabled;
   private String email;
   private String password;
   private String salt;
   private String domainid;

   public String getUserid() {
      return userid;
   }

   public void setUserid(String id) {
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

   public void setSalt(String s){
       this.salt = s;
   }

   public String getSalt(){
       return this.salt;
   }

   public String getDomainID(){
       return domainid;
   }

   public void setDomainID(String domainid){
       this.domainid = domainid;
   }
}

