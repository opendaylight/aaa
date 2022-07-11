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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "org.opendaylight.aaa.h2", property = "type=default")
@Designate(ocd = H2Store.Configuration.class)
public class H2Store implements IIDMStore {
    @ObjectClassDefinition
    public @interface Configuration {
        @AttributeDefinition(name = "dbUserName")
        String username() default "foo";
        @AttributeDefinition(name = "dbPassword")
        String password() default "bar";
    }

    private static final Logger LOG = LoggerFactory.getLogger(H2Store.class);

    private final DomainStore domainStore;
    private final UserStore userStore;
    private final RoleStore roleStore;
    private final GrantStore grantStore;

    @Activate
    public H2Store(@Reference final PasswordHashService passwordService, final Configuration config) {
        this(config.username(), config.password(), passwordService);
        LOG.info("H2 IDMStore activated");
    }

    public H2Store(final String dbUsername, final String dbPassword, final PasswordHashService passwordService) {
        this(new IdmLightSimpleConnectionProvider(
                new IdmLightConfigBuilder().dbUser(dbUsername).dbPwd(dbPassword).build()), passwordService);
    }

    public H2Store(final ConnectionProvider connectionFactory, final PasswordHashService passwordService) {
        domainStore = new DomainStore(connectionFactory);
        userStore = new UserStore(connectionFactory, passwordService);
        roleStore = new RoleStore(connectionFactory);
        grantStore = new GrantStore(connectionFactory);
    }

    @Deactivate
    void deactivate() {
        LOG.info("H2 IDMStore deactivated");
    }

    @Override
    public Domain writeDomain(final Domain domain) throws IDMStoreException {
        try {
            return domainStore.createDomain(domain);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain readDomain(final String domainid) throws IDMStoreException {
        try {
            return domainStore.getDomain(domainid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain deleteDomain(final String domainid) throws IDMStoreException {
        try {
            return domainStore.deleteDomain(domainid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain updateDomain(final Domain domain) throws IDMStoreException {
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
    public Role writeRole(final Role role) throws IDMStoreException {
        try {
            return roleStore.createRole(role);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role readRole(final String roleid) throws IDMStoreException {
        try {
            return roleStore.getRole(roleid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role deleteRole(final String roleid) throws IDMStoreException {
        try {
            return roleStore.deleteRole(roleid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role updateRole(final Role role) throws IDMStoreException {
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
    public User writeUser(final User user) throws IDMStoreException {
        try {
            return userStore.createUser(user);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User readUser(final String userid) throws IDMStoreException {
        try {
            return userStore.getUser(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User deleteUser(final String userid) throws IDMStoreException {
        try {
            return userStore.deleteUser(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User updateUser(final User user) throws IDMStoreException {
        try {
            return userStore.putUser(user);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while updating user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Users getUsers(final String username, final String domain) throws IDMStoreException {
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
    public Grant writeGrant(final Grant grant) throws IDMStoreException {
        try {
            return grantStore.createGrant(grant);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant readGrant(final String grantid) throws IDMStoreException {
        try {
            return grantStore.getGrant(grantid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant readGrant(final String domainid, final String userid, final String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }

    @Override
    public Grant deleteGrant(final String grantid) throws IDMStoreException {
        try {
            return grantStore.deleteGrant(grantid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grants getGrants(final String domainid, final String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(domainid, userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting grants", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grants getGrants(final String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting grants", e);
            throw new IDMStoreException(e);
        }
    }

    public Domain createDomain(final String domainName, final boolean enable) throws StoreException {
        Domain domain = new Domain();
        domain.setName(domainName);
        domain.setEnabled(enable);
        return domainStore.createDomain(domain);
    }

    public User createUser(final String name, final String password, final String domain, final String description,
            final String email, final boolean enabled, final String salt) throws StoreException {
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

    public Role createRole(final String name, final String domain, final String description)
            throws StoreException {
        Role role = new Role();
        role.setDescription(description);
        role.setName(name);
        role.setDomainid(domain);
        return roleStore.createRole(role);
    }

    public Grant createGrant(final String domain, final String user, final String role) throws StoreException {
        Grant grant = new Grant();
        grant.setDomainid(domain);
        grant.setRoleid(role);
        grant.setUserid(user);
        return grantStore.createGrant(grant);
    }

}
