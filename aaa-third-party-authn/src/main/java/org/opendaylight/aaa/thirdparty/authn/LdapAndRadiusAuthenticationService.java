/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.thirdparty.authn;

import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.ThirdPartyAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public class LdapAndRadiusAuthenticationService implements ThirdPartyAuthenticationService{

    private static final Logger LOG = LoggerFactory.getLogger(LdapAndRadiusAuthenticationService.class);

    @Override
    public boolean authenticate(PasswordCredentials passwordCredentials) {
        if(LDAPConfiguration.getInstance().isEnabled()){
            try {
                return LDAPIntegration.authenticateSSL(passwordCredentials);
            } catch (Exception e) {
                LOG.error("Failed to authenticate using LDAP",e);
                return false;
            }
        }else if(RadiusConfiguration.getInstance().isEnabled()){
            try{
                return RadiusIntegration.authenticate(passwordCredentials);
            }catch(Exception e) {
                LOG.error("Failed to authenticate using Radius",e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean shouldCreateLocalUser() {
        return true;
    }

    @Override
    public boolean allowLocalAuthentication() {
        return !LDAPConfiguration.getInstance().isEnabled() && !RadiusConfiguration.getInstance().isEnabled();
    }
}
