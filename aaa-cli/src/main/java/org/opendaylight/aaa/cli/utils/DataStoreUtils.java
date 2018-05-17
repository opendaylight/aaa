/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.utils;

import java.util.List;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataStoreUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DataStoreUtils.class);
    private static final String ADMIN_ROLE = "admin";

    private DataStoreUtils() {

    }

    public static String getDomainId(final IIDMStore identityStore, final String domainName) throws IDMStoreException {
        Domains domains = identityStore.getDomains();
        if (domains != null) {
            for (Domain domain : domains.getDomains()) {
                if (domain.getName().equalsIgnoreCase(domainName)) {
                    return domain.getDomainid();
                }
            }
        }
        return null;
    }

    public static String getRoleId(final IIDMStore identityStore, final String roleName) throws IDMStoreException {
        List<Role> roles = identityStore.getRoles().getRoles();
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return role.getRoleid();
            }
        }
        return null;
    }

    public static String getUserId(final IIDMStore identityStore, final String userName) throws IDMStoreException {
        List<User> users = identityStore.getUsers().getUsers();
        for (User usr : users) {
            if (usr.getName().equalsIgnoreCase(userName)) {
                return usr.getUserid();
            }
        }
        return null;
    }

    public static String getGrantId(final IIDMStore identityStore, final String domainName, final String roleName,
                                    final String userName)
            throws IDMStoreException {
        final String domainId = getDomainId(identityStore, domainName);
        if (domainId == null) {
            return null;
        }
        final String usrId = getUserId(identityStore, userName);
        if (usrId == null) {
            return null;
        }
        List<Grant> grants = identityStore.getGrants(usrId).getGrants();
        if (grants == null || grants.isEmpty()) {
            return null;
        }
        final String roleId = getRoleId(identityStore, roleName);
        if (roleId == null) {
            return null;
        }
        for (Grant grant : grants) {
            if (grant.getRoleid().equals(roleId)) {
                return grant.getGrantid();
            }
        }
        return null;
    }

    public static User isAdminUser(final IIDMStore identityStore, final PasswordHashService passwordService,
                                   final String userName, final String password) throws IDMStoreException {

        final Users users = identityStore.getUsers();
        for (User usr : users.getUsers()) {
            if (usr.getName().equals(userName)
                    && passwordService.passwordsMatch(password, usr.getPassword(), usr.getSalt())) {
                List<Grant> grants = identityStore.getGrants(usr.getUserid()).getGrants();
                if (grants != null && !grants.isEmpty()) {
                    final String adminRoleId = getRoleId(identityStore, ADMIN_ROLE);
                    for (Grant grant : grants) {
                        if (grant.getRoleid().equals(adminRoleId)) {
                            return usr;
                        }
                    }
                    LOG.debug("user is not authorized for admin grant");
                }
            }
        }
        return null;
    }
}
