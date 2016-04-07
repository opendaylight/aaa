/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the generic <code>JdbcRealm</code> provided by Shiro.  This allows for
 * enhanced logging as well as isolation of all realms in a single package,
 * <code>org.opendaylight.aaa.shiro.realm</code>, which enables easier import
 * by consuming servlets.  JdbcRealm allows integration of AAA with a generic
 * JDBC-supporting data source.  This can ease deployment with existing OSS
 * systems.
 *
 * To enabled the <code>ODLJdbcRealm</code>, modify the realms declaration in
 * <code>etc/shiro.ini</code> as follows:
 * <code>
 * ds = com.mysql.jdbc.Driver
 * ds.serverName = localhost
 * ds.user = user
 * ds.password = password
 * ds.databaseName = db_name
 * jdbcRealm = org.opendaylight.aaa.shiro.realm.ODLJdbcRealm
 * jdbcRealm.dataSource = $ds
 * jdbcRealm.authenticationQuery = "SELECT password FROM users WHERE user_name = ?"
 * jdbcRealm.userRolesQuery = "SELECT role_name FROM user_rolesWHERE user_name = ?"
 * ...
 * securityManager.realms = $tokenAuthRealm, $jdbcRealm
 * </code>
 * Note that the values you use for these fields will likely differ from the
 * ones provided above based on your particular deployment scenario.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class ODLJdbcRealm extends JdbcRealm {

    private static final Logger LOG = LoggerFactory.getLogger(ODLJdbcRealm.class);

    public ODLJdbcRealm() {
        LOG.debug("Creating an instance of ODLJdbcRealm to use with AAA");
    }
}
