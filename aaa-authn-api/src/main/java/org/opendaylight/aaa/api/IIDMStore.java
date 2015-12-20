/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public interface IIDMStore {
    public String DEFAULT_DOMAIN = "sdn";

    // Domain methods
    public Domain writeDomain(Domain domain) throws IDMStoreException;

    public Domain readDomain(String domainid) throws IDMStoreException;

    public Domain deleteDomain(String domainid) throws IDMStoreException;

    public Domain updateDomain(Domain domain) throws IDMStoreException;

    public Domains getDomains() throws IDMStoreException;

    // Role methods
    public Role writeRole(Role role) throws IDMStoreException;

    public Role readRole(String roleid) throws IDMStoreException;

    public Role deleteRole(String roleid) throws IDMStoreException;

    public Role updateRole(Role role) throws IDMStoreException;

    public Roles getRoles() throws IDMStoreException;

    // User methods
    public User writeUser(User user) throws IDMStoreException;

    public User readUser(String userid) throws IDMStoreException;

    public User deleteUser(String userid) throws IDMStoreException;

    public User updateUser(User user) throws IDMStoreException;

    public Users getUsers() throws IDMStoreException;

    public Users getUsers(String username, String domain) throws IDMStoreException;

    // Grant methods
    public Grant writeGrant(Grant grant) throws IDMStoreException;

    public Grant readGrant(String grantid) throws IDMStoreException;

    public Grant deleteGrant(String grantid) throws IDMStoreException;

    public Grants getGrants(String domainid, String userid) throws IDMStoreException;

    public Grants getGrants(String userid) throws IDMStoreException;

    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException;
}
