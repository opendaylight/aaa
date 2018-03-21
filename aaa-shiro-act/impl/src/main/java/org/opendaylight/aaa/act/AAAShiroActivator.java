/*
 * Copyright © 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.act;

import org.opendaylight.aaa.shiro.AAAShiroActivation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AAAShiroActivator provides enablement of the AAAFilter for RESTCONF.  odl-restconf-noauth
 * demands AAA is turned off.  Since odl-restconf just installs AAA along with odl-restconf-noauth,
 * AAAFilter is disabled by default.  The filter is only enabled upon activation of this bundle.
 */
public class AAAShiroActivator {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroActivator.class);

    public AAAShiroActivator(AAAShiroActivation aaaActivation) {
        aaaActivation.activate();
        LOG.info("AAA shiro is enabled");
    }
}
