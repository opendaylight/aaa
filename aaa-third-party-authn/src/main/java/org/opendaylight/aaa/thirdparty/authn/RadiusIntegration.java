/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.thirdparty.authn;

import com.google.common.base.Preconditions;

import org.opendaylight.aaa.api.PasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public class RadiusIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(RadiusIntegration.class);

    public static boolean authenticate(PasswordCredentials creds) throws Exception {
        LOG.info("Authenticating with Radius");
        Preconditions.checkNotNull(creds);
        RadiusConfiguration configuration = RadiusConfiguration.getInstance();
        /* Un-mark below for tiny Radius Supoport, and remove the exception throwing
        RadiusClient rc = new RadiusClient(configuration.getHost(),configuration.getSECRET());
        return rc.authenticate(creds.username(), creds.password());
        */
        throw new UnsupportedOperationException("Radius support is not supported out of the box in ODL, just via proprietary distribution");
    }
}
