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

@XmlRootElement(name = "grant")
public interface Grant extends IStorable{

   public Integer getGrantid();
   @KeyMethod
   public void setGrantid(Integer id);

   public String getDescription();

   public void setDescription(String description);

   public Integer getDomainid();

   public void setDomainid(Integer id);
 
   public Integer getUserid();

   public void setUserid(Integer id);

   public Integer getRoleid();

   public void setRoleid(Integer id);
}

