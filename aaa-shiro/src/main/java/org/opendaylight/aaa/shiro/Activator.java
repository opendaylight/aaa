/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Offers contextual <code>DEBUG</code> level clues concerning the activation
 * of the <code>aaa-shiro</code> bundle.
 * 
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class Activator extends DependencyActivatorBase {

  private static Logger LOG = LoggerFactory.getLogger(Activator.class);

  @Override
  public void destroy(BundleContext bc, DependencyManager dm)
      throws Exception {
    final String DEBUG_MESSAGE = "Destroying the aaa-shiro bundle";
    LOG.debug(DEBUG_MESSAGE);
  }

  @Override
  public void init(BundleContext bc, DependencyManager dm) throws Exception {
    final String DEBUG_MESSAGE = "Initializing the aaa-shiro bundle";
    LOG.debug(DEBUG_MESSAGE);
  }

}
