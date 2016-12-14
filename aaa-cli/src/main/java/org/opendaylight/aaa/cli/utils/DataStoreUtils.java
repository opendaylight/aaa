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
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

public class DataStoreUtils {

    public static final String getDomainId(IIDMStore identityStore, String domainName) throws IDMStoreException {
         List<Domain> domains = identityStore.getDomains().getDomains();
         for (Domain domain : domains) {
            if (domain.getName().equals(domainName)) {
                return domain.getDomainid();
            }
         }
         return null;
    }

    public static final String getRoleId(IIDMStore identityStore, String roleName) throws IDMStoreException {
        List<Role> roles = identityStore.getRoles().getRoles();
        for (Role role : roles) {
           if (role.getName().equals(roleName)) {
               return role.getRoleid();
           }
        }
        return null;
   }

    public static final String getUserId(IIDMStore identityStore, String userName) throws IDMStoreException {
        List<User> users = identityStore.getUsers().getUsers();
        for (User usr : users) {
           if (usr.getName().equals(userName)) {
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
        List<Grant> grants = identityStore.getGrants(domainId, usrId).getGrants();
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

    public static boolean login(IIDMStore identityStore, String userName, String password) throws IDMStoreException {
        final Users users = identityStore.getUsers();
        for (User usr : users.getUsers()) {
            final String realPwd = SHA256Calculator.getSHA256(password, usr.getSalt());
            if (usr.getName().equals(userName) && usr.getPassword().equals(realPwd)) {
                return true;
            }
        }
        return false;
    }
}
