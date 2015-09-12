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

@XmlRootElement(name = "grant")
public class Grant {
   private String grantid;
   private String domainid;
   private String userid;
   private String roleid;

   public String getGrantid(){
       return this.grantid;
   }

   public void setGrantid(String id){
       this.grantid = id;
   }

   public String getDomainid() {
      return domainid;
   }

   public void setDomainid(String id) {
      this.domainid = id;
   }

   public String getUserid() {
      return userid;
   }

   public void setUserid(String id) {
      this.userid = id;
   }

   public String getRoleid() {
      return roleid;
   }

   public void setRoleid(String id) {
      this.roleid = id;
   }
}

