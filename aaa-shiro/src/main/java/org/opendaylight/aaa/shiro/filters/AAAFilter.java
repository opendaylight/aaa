/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAFilter extends ShiroFilter {

  private static Logger LOG = LoggerFactory.getLogger(AAAFilter.class);
  public AAAFilter() {
    super();
    
    Ini ini = new Ini();
    ini.loadFromPath("etc/shiro.ini");
    LOG.error("INI file contents are: " + ini.toString());
    
    IniSecurityManagerFactory factory = new IniSecurityManagerFactory(ini);
    SecurityManager securityManager = factory.getInstance();
    SecurityUtils.setSecurityManager(securityManager);
  }
  
  @Override
  public void init() throws Exception {
    super.init();
  }
}
