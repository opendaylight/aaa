/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the generic <code>ActiveDirectoryRealm</code> provided by Shiro.  This
 * allows for enhanced logging as well as isolation of all realms in a single
 * package, <code>org.opendaylightaaa.shiro.realm</code>, which enables easier
 * import by consuming servlets.
 *
 * To enable the <code>ODLActiveDirectoryRealm</code>, modify the realms
 * declaration in <code>etc/shiro.ini</code> as follows:
 * <code>adRealm = org.opendaylight.aaa.shiro.realm.ODLActiveDirectoryRealm
 * adRealm.searchBase = "CN=Users,DC=example,DC=com"
 * adRealm.systemUsername = aduser@example.com
 * adRealm.systemPassword = adpassword
 * adRealm.url = ldaps://adserver:636
 * adRealm.groupRolesMap = "CN=sysadmin,CN=Users,DC=example,DC=com":"sysadmin",\
 *                         "CN=unprivileged,CN=Users,DC=example,DC=com":"unprivileged"
 * ...
 * securityManager.realms = $tokenAuthRealm, $adRealm</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class ODLActiveDirectoryRealm extends ActiveDirectoryRealm {

    private static final Logger LOG = LoggerFactory.getLogger(ODLActiveDirectoryRealm.class);

    public ODLActiveDirectoryRealm() {
        LOG.debug("Creating an instance of ODLActiveDirectoryRealm to use with AAA");
    }
}
