/*
 * Copyright (c) 2015 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.datastore.h2;

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
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2Store implements IIDMStore {

    private static final Logger LOG = LoggerFactory.getLogger(H2Store.class);

    private final DomainStore domainStore;
    private final UserStore userStore;
    private final RoleStore roleStore;
    private final GrantStore grantStore;

    public H2Store(final String dbUsername, final String dbPassword, final PasswordHashService passwordService) {
        this(new IdmLightSimpleConnectionProvider(
                new IdmLightConfigBuilder().dbUser(dbUsername).dbPwd(dbPassword).build()), passwordService);
    }

    public H2Store(ConnectionProvider connectionFactory, final PasswordHashService passwordService) {
        this.domainStore = new DomainStore(connectionFactory);
        this.userStore = new UserStore(connectionFactory, passwordService);
        this.roleStore = new RoleStore(connectionFactory);
        this.grantStore = new GrantStore(connectionFactory);
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        try {
            return domainStore.createDomain(domain);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.getDomain(domainid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.deleteDomain(domainid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        try {
            return domainStore.putDomain(domain);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while updating domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        try {
            return domainStore.getDomains();
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading domains", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        try {
            return roleStore.createRole(role);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        try {
            return roleStore.getRole(roleid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        try {
            return roleStore.deleteRole(roleid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        try {
            return roleStore.putRole(role);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while updating role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        try {
            return roleStore.getRoles();
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting roles", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        try {
            return userStore.createUser(user);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        try {
            return userStore.getUser(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        try {
            return userStore.deleteUser(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        try {
            return userStore.putUser(user);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while updating user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        try {
            return userStore.getUsers(username, domain);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading users", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        try {
            return userStore.getUsers();
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading users", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        try {
            return grantStore.createGrant(grant);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.getGrant(grantid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.deleteGrant(grantid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(domainid, userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting grants", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting grants", e);
            throw new IDMStoreException(e);
        }
    }

    public Domain createDomain(String domainName, boolean enable) throws StoreException {
        Domain domain = new Domain();
        domain.setName(domainName);
        domain.setEnabled(enable);
        return domainStore.createDomain(domain);
    }

    public User createUser(String name, String password, String domain, String description,
            String email, boolean enabled, String salt) throws StoreException {
        User user = new User();
        user.setName(name);
        user.setDomainid(domain);
        user.setDescription(description);
        user.setEmail(email);
        user.setEnabled(enabled);
        user.setPassword(password);
        user.setSalt(salt);
        return userStore.createUser(user);
    }

    public Role createRole(String name, String domain, String description)
            throws StoreException {
        Role role = new Role();
        role.setDescription(description);
        role.setName(name);
        role.setDomainid(domain);
        return roleStore.createRole(role);
    }

    public Grant createGrant(String domain, String user, String role) throws StoreException {
        Grant grant = new Grant();
        grant.setDomainid(domain);
        grant.setRoleid(role);
        grant.setUserid(user);
        return grantStore.createGrant(grant);
    }

}
