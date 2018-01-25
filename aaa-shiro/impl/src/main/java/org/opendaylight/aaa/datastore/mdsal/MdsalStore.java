/*
 * Copyright (c) 2017 Kontron Canada and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.datastore.mdsal;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

/**
 * Implementation of the mdsal AAA data store
 * FIXME the implementation will be done after updating the aaa.yang model.
 */
public class MdsalStore implements IIDMStore {

    private final DataBroker dataBroker;

    public MdsalStore(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        // TODO Auto-generated method stub
        return null;
    }

}
