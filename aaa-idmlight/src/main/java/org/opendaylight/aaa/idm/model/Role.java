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
 */

import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.KeyMethod;

@XmlRootElement(name = "role")
public interface Role extends IStorable{
   public Integer getRoleid();
   @KeyMethod
   public void setRoleid(Integer id);

   public String getName();

   public void setName(String name);

   public String getDescription();

   public void setDescription(String description);
}

