/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.authn.mdsal.store;

import java.util.List;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

/**
 * @author Sharon Aicler - saichler@cisco.com
 *
 */
public class IDMStore implements IIDMStore {
    private final IDMMDSALStore mdsalStore;

    public IDMStore(IDMMDSALStore mdsalStore) {
        this.mdsalStore = mdsalStore;
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.writeDomain(IDMObject2MDSAL.toMDSALDomain(domain)));
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.readDomain(domainid));
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.deleteDomain(domainid));
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.updateDomain(IDMObject2MDSAL.toMDSALDomain(domain)));
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        Domains domains = new Domains();
        List<org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain> mdSalDomains = mdsalStore.getAllDomains();
        for (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain d : mdSalDomains) {
            domains.getDomains().add(IDMObject2MDSAL.toIDMDomain(d));
        }
        return domains;
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.writeRole(IDMObject2MDSAL.toMDSALRole(role)));
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.readRole(roleid));
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.deleteRole(roleid));
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.writeRole(IDMObject2MDSAL.toMDSALRole(role)));
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.writeUser(IDMObject2MDSAL.toMDSALUser(user)));
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.readUser(userid));
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.deleteUser(userid));
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.writeUser(IDMObject2MDSAL.toMDSALUser(user)));
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMGrant(mdsalStore.writeGrant(IDMObject2MDSAL.toMDSALGrant(grant)));
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMGrant(mdsalStore.readGrant(grantid));
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMGrant(mdsalStore.readGrant(grantid));
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        Roles roles = new Roles();
        List<org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role> mdSalRoles = mdsalStore.getAllRoles();
        for (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role r : mdSalRoles) {
            roles.getRoles().add(IDMObject2MDSAL.toIDMRole(r));
        }
        return roles;
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        Users users = new Users();
        List<org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User> mdSalUsers = mdsalStore.getAllUsers();
        for (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User u : mdSalUsers) {
            users.getUsers().add(IDMObject2MDSAL.toIDMUser(u));
        }
        return users;
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        Users users = new Users();
        List<org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User> mdSalUsers = mdsalStore.getAllUsers();
        for (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User u : mdSalUsers) {
            if (u.getDomainid().equals(domain) && u.getName().equals(username)) {
                users.getUsers().add(IDMObject2MDSAL.toIDMUser(u));
            }
        }
        return users;
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        Grants grants = new Grants();
        List<org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant> mdSalGrants = mdsalStore.getAllGrants();
        String currentGrantUserId, currentGrantDomainId;
        for (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant g : mdSalGrants) {
            currentGrantUserId = g.getUserid();
            currentGrantDomainId = g.getDomainid();
            if (currentGrantUserId.equals(userid) && currentGrantDomainId.equals(domainid)) {
                grants.getGrants().add(IDMObject2MDSAL.toIDMGrant(g));
            }
        }
        return grants;
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        Grants grants = new Grants();
        List<org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant> mdSalGrants = mdsalStore.getAllGrants();
        for (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant g : mdSalGrants) {
            if (g.getUserid().equals(userid)) {
                grants.getGrants().add(IDMObject2MDSAL.toIDMGrant(g));
            }
        }
        return grants;
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }

    @Override
    public boolean isMainNodeInCluster() {
        return true;
    }
}
