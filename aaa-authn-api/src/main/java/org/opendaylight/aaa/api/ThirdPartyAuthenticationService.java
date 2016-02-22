/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

/**
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public interface ThirdPartyAuthenticationService {
    public boolean authenticate(PasswordCredentials passwordCredentials);
    public boolean shouldCreateLocalUser();
    public boolean allowLocalAuthentication();
}
