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
 * @author saichler@cisco.com
 *
 */


import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.KeyMethod;

@XmlRootElement(name = "user")
public interface User extends IStorable{

   public Integer getUserid();
      
   @KeyMethod
   public void setUserid(Integer id);

   public String getName();

   public void setName(String name);

   public String getDescription();
   
   public void setDescription(String description);

   public Boolean getEnabled();

   public void setEnabled(Boolean enabled);

   public void setEmail(String email);
   
   public String getEmail();
   
   public void setPassword(String password);
   
   public String getPassword();
}

