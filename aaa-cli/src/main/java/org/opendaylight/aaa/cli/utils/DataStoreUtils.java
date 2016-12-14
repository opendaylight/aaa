/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.utils;

import java.util.List;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStoreUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DataStoreUtils.class);
    private static final String ADMIN_ROLE = "admin";

    public static final String getDomainId(IIDMStore identityStore, String domainName) throws IDMStoreException {
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

    public static final String getRoleId(IIDMStore identityStore, String roleName) throws IDMStoreException {
        List<Role> roles = identityStore.getRoles().getRoles();
        for (Role role : roles) {
           if (role.getName().equalsIgnoreCase(roleName)) {
               return role.getRoleid();
           }
        }
        return null;
   }

    public static final String getUserId(IIDMStore identityStore, String userName) throws IDMStoreException {
        List<User> users = identityStore.getUsers().getUsers();
        for (User usr : users) {
           if (usr.getName().equalsIgnoreCase(userName)) {
               return usr.getUserid();
           }
        }
        return null;
   }

    public static final String getGrantId(IIDMStore identityStore, String domainName, String roleName,
                                        String userName) throws IDMStoreException {
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

    public static User isAdminUser(IIDMStore identityStore, String userName, String password) throws IDMStoreException {
        final Users users = identityStore.getUsers();
        for (User usr : users.getUsers()) {
            final String realPwd = SHA256Calculator.getSHA256(password, usr.getSalt());
            if (usr.getName().equals(userName) && usr.getPassword().equals(realPwd)) {
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
