/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.aaa.idm.radius;

import org.opendaylight.aaa.api.PasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.util.RadiusClient;

import com.google.common.base.Preconditions;
/*
 * @Author - Sharon Aicler (saichler@cisco.com)
 */
public class RadiusIntegration {
    private static Logger logger = LoggerFactory.getLogger(RadiusIntegration.class);

    public static boolean authenticate(PasswordCredentials creds,String domain) throws Exception {
        logger.info("Authenticating with Radius");
        Preconditions.checkNotNull(creds);
        Preconditions.checkNotNull(domain);
        RadiusConfiguration configuration = RadiusConfiguration.getInstance();
        RadiusClient rc = new RadiusClient(configuration.getHost(),configuration.getSECRET());
        return rc.authenticate(creds.username(), creds.password());
    }
}
