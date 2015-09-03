/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import org.apache.shiro.web.servlet.ShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default AAA JAX-RS 1.X Web Filter.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <code>javax.servlet.Filter</code>
 * @see <code>org.apache.shiro.web.servlet.ShiroFilter</code>
 */
public class AAAFilter extends ShiroFilter {

  private static Logger LOG = LoggerFactory.getLogger(AAAFilter.class);

  public AAAFilter() {
    super();
    final String DEBUG_MESSAGE = "Creating the AAAFilter";
    LOG.debug(DEBUG_MESSAGE);
  }

  @Override
  public void init() throws Exception {
    super.init();
    final String DEBUG_MESSAGE = "Initializing the AAAFilter";
    LOG.debug(DEBUG_MESSAGE);
  }
}
