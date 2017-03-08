/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

/**
 * Interface for the IDMStore.
 *
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public interface IIDMStore {
    String DEFAULT_DOMAIN = "sdn";

    // Domain methods
    Domain writeDomain(Domain domain) throws IDMStoreException;

    Domain readDomain(String domainid) throws IDMStoreException;

    Domain deleteDomain(String domainid) throws IDMStoreException;

    Domain updateDomain(Domain domain) throws IDMStoreException;

    Domains getDomains() throws IDMStoreException;

    // Role methods
    Role writeRole(Role role) throws IDMStoreException;

    Role readRole(String roleid) throws IDMStoreException;

    Role deleteRole(String roleid) throws IDMStoreException;

    Role updateRole(Role role) throws IDMStoreException;

    Roles getRoles() throws IDMStoreException;

    // User methods
    User writeUser(User user) throws IDMStoreException;

    User readUser(String userid) throws IDMStoreException;

    User deleteUser(String userid) throws IDMStoreException;

    User updateUser(User user) throws IDMStoreException;

    Users getUsers() throws IDMStoreException;

    Users getUsers(String username, String domain) throws IDMStoreException;

    // Grant methods
    Grant writeGrant(Grant grant) throws IDMStoreException;

    Grant readGrant(String grantid) throws IDMStoreException;

    Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException;

    Grant deleteGrant(String grantid) throws IDMStoreException;

    Grants getGrants(String domainid, String userid) throws IDMStoreException;

    Grants getGrants(String userid) throws IDMStoreException;

    boolean isMainNodeInCluster();
}
