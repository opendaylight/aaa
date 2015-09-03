/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realms.ldap;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends DependencyActivatorBase {

  private static Logger LOG = LoggerFactory.getLogger(Activator.class);

  @Override
  public void destroy(BundleContext arg0, DependencyManager arg1)
      throws Exception {
    LOG.info("Destroy aaa-shiro bundle");
  }

  @Override
  public void init(BundleContext arg0, DependencyManager arg1) throws Exception {
    LOG.info("Init aaa-shiro bundle");
  }

}
