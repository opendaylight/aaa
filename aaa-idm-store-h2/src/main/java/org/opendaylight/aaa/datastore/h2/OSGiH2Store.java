/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
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

@Beta
@Component(immediate = true, configurationPid = "org.opendaylight.aaa.h2", property = "type=default")
@Designate(ocd = OSGiH2Store.Configuration.class)
// FIXME: merge this with H2Store when we have constructor injection
public final class OSGiH2Store implements IIDMStore {
    @ObjectClassDefinition
    public @interface Configuration {
        @AttributeDefinition(name = "dbUserName")
        String username() default "foo";
        @AttributeDefinition(name = "dbPassword")
        String password() default "bar";
    }

    private static final Logger LOG = LoggerFactory.getLogger(OSGiH2Store.class);

    @Reference
    PasswordHashService passwordService;

    private H2Store delegate;

    @Override
    public Domain writeDomain(final Domain domain) throws IDMStoreException {
        return delegate().writeDomain(domain);
    }

    @Override
    public Domain readDomain(final String domainid) throws IDMStoreException {
        return delegate().readDomain(domainid);
    }

    @Override
    public Domain deleteDomain(final String domainid) throws IDMStoreException {
        return delegate().deleteDomain(domainid);
    }

    @Override
    public Domain updateDomain(final Domain domain) throws IDMStoreException {
        return delegate().updateDomain(domain);
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        return delegate().getDomains();
    }

    @Override
    public Role writeRole(final Role role) throws IDMStoreException {
        return delegate().writeRole(role);
    }

    @Override
    public Role readRole(final String roleid) throws IDMStoreException {
        return delegate().readRole(roleid);
    }

    @Override
    public Role deleteRole(final String roleid) throws IDMStoreException {
        return delegate().deleteRole(roleid);
    }

    @Override
    public Role updateRole(final Role role) throws IDMStoreException {
        return delegate().updateRole(role);
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        return delegate().getRoles();
    }

    @Override
    public User writeUser(final User user) throws IDMStoreException {
        return delegate().writeUser(user);
    }

    @Override
    public User readUser(final String userid) throws IDMStoreException {
        return delegate().readUser(userid);
    }

    @Override
    public User deleteUser(final String userid) throws IDMStoreException {
        return delegate().deleteUser(userid);
    }

    @Override
    public User updateUser(final User user) throws IDMStoreException {
        return delegate().updateUser(user);
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        return delegate().getUsers();
    }

    @Override
    public Users getUsers(final String username, final String domain) throws IDMStoreException {
        return delegate().getUsers(username, domain);
    }

    @Override
    public Grant writeGrant(final Grant grant) throws IDMStoreException {
        return delegate().writeGrant(grant);
    }

    @Override
    public Grant readGrant(final String grantid) throws IDMStoreException {
        return delegate().readGrant(grantid);
    }

    @Override
    public Grant readGrant(final String domainid, final String userid, final String roleid) throws IDMStoreException {
        return delegate().readGrant(domainid, userid, roleid);
    }

    @Override
    public Grant deleteGrant(final String grantid) throws IDMStoreException {
        return delegate().deleteGrant(grantid);
    }

    @Override
    public Grants getGrants(final String domainid, final String userid) throws IDMStoreException {
        return delegate().getGrants(domainid, userid);
    }

    @Override
    public Grants getGrants(final String userid) throws IDMStoreException {
        return delegate().getGrants(userid);
    }

    @Activate
    void activate(final Configuration config) {
        delegate = new H2Store(config.username(), config.password(), passwordService);
        LOG.info("H2 IDMStore activated");
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("H2 IDMStore deactivated");
    }

    private H2Store delegate() {
        return verifyNotNull(delegate);
    }
}
