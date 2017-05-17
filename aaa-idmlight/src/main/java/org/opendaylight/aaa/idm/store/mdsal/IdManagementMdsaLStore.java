/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.store.mdsal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
/*import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Authentication;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.GrantBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.GrantKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.RoleBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.RoleKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.UserBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.UserKey;*/
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identity Management MDSAL Store.
 *
 * @author Sharon Aicler - saichler@cisco.com
 *
 */
public class IdManagementMdsaLStore {

    private static final Logger LOG = LoggerFactory.getLogger(IdManagementMdsaLStore.class);
    private final DataBroker dataBroker;

    public IdManagementMdsaLStore(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public static final String getString(String stringValueA, String stringValueB) {
        if (stringValueA != null) {
            return stringValueA;
        }
        return stringValueB;
    }

    public static final Boolean getBoolean(Boolean booleanValueA, Boolean booleanValueB) {
        if (booleanValueA != null) {
            return booleanValueA;
        }
        return booleanValueB;
    }

    public static boolean waitForSubmit(CheckedFuture<Void, TransactionCommitFailedException> submit) {
        // This can happen only when testing
        if (submit == null) {
            return false;
        }
        while (!submit.isDone() && !submit.isCancelled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
            }
        }
        return submit.isCancelled();
    }

    // Domain methods
    /*public Domain writeDomain(Domain domain) {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getName());
        Preconditions.checkNotNull(domain.isEnabled());
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDescription(domain.getDescription());
        domainBuilder.setDomainid(domain.getName());
        domainBuilder.setEnabled(domain.isEnabled());
        domainBuilder.setName(domain.getName());
        domainBuilder.setKey(new DomainKey(domainBuilder.getName()));
        domain = domainBuilder.build();
        InstanceIdentifier<Domain> domainId = InstanceIdentifier.create(Authentication.class).child(Domain.class,
                new DomainKey(domain.getDomainid()));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.put(LogicalDatastoreType.CONFIGURATION, domainId, domain, true);
        CheckedFuture<Void, TransactionCommitFailedException> submit = wrt.submit();
        if (!waitForSubmit(submit)) {
            return domain;
        } else {
            return null;
        }
    }

    public Domain readDomain(String domainid) {
        Preconditions.checkNotNull(domainid);
        InstanceIdentifier<Domain> domainId = InstanceIdentifier.create(Authentication.class).child(Domain.class,
                new DomainKey(domainid));
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Domain>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION,
                domainId);
        if (read == null) {
            LOG.error("Failed to read domain from data store");
            return null;
        }
        Optional<Domain> optional = null;
        try {
            optional = read.get();
        } catch (InterruptedException | ExecutionException e1) {
            LOG.error("Failed to read domain from data store", e1);
            return null;
        }

        if (optional == null) {
            return null;
        }

        if (!optional.isPresent()) {
            return null;
        }

        return optional.get();
    }

    public Domain deleteDomain(String domainid) {
        Preconditions.checkNotNull(domainid);
        Domain domain = readDomain(domainid);
        if (domain == null) {
            LOG.error("Failed to delete domain from data store, unknown domain");
            return null;
        }
        InstanceIdentifier<Domain> domainId = InstanceIdentifier.create(Authentication.class).child(Domain.class,
                new DomainKey(domainid));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.delete(LogicalDatastoreType.CONFIGURATION, domainId);
        wrt.submit();
        return domain;
    }

    public Domain updateDomain(Domain domain) throws IDMStoreException {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getDomainid());
        Domain existing = readDomain(domain.getDomainid());
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDescription(getString(domain.getDescription(), existing.getDescription()));
        domainBuilder.setName(existing.getName());
        domainBuilder.setEnabled(getBoolean(domain.isEnabled(), existing.isEnabled()));
        return writeDomain(domainBuilder.build());
    }

    public List<Domain> getAllDomains() {
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION,
                id);
        if (read == null) {
            return null;
        }

        try {
            if (read.get() == null) {
                return null;
            }
            if (read.get().isPresent()) {
                Authentication auth = read.get().get();
                return auth.getDomain();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Failed to read domains", e);
        }
        return null;
    }

    public List<Role> getAllRoles() {
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION,
                id);
        if (read == null) {
            return null;
        }

        try {
            if (read.get() == null) {
                return null;
            }
            if (read.get().isPresent()) {
                Authentication auth = read.get().get();
                return auth.getRole();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Failed to read domains", e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION,
                id);
        if (read == null) {
            return null;
        }

        try {
            if (read.get() == null) {
                return null;
            }
            if (read.get().isPresent()) {
                Authentication auth = read.get().get();
                return auth.getUser();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Failed to read domains", e);
        }
        return null;
    }

    public List<Grant> getAllGrants() {
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION,
                id);
        if (read == null) {
            return null;
        }

        try {
            if (read.get() == null) {
                return null;
            }
            if (read.get().isPresent()) {
                Authentication auth = read.get().get();
                return auth.getGrant();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Failed to read domains", e);
        }
        return null;
    }

    // Role methods
    public Role writeRole(Role role) {
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getName());
        Preconditions.checkNotNull(role.getDomainid());
        Preconditions.checkNotNull(readDomain(role.getDomainid()));
        RoleBuilder roleBuilder = new RoleBuilder();
        roleBuilder.setDescription(role.getDescription());
        roleBuilder.setRoleid(IDMStoreUtil.createRoleid(role.getName(), role.getDomainid()));
        roleBuilder.setKey(new RoleKey(roleBuilder.getRoleid()));
        roleBuilder.setName(role.getName());
        roleBuilder.setDomainid(role.getDomainid());
        role = roleBuilder.build();
        InstanceIdentifier<Role> roleId = InstanceIdentifier.create(Authentication.class).child(Role.class,
                new RoleKey(role.getRoleid()));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.put(LogicalDatastoreType.CONFIGURATION, roleId, role, true);
        CheckedFuture<Void, TransactionCommitFailedException> submit = wrt.submit();
        if (!waitForSubmit(submit)) {
            return role;
        } else {
            return null;
        }
    }

    public Role readRole(String roleid) {
        Preconditions.checkNotNull(roleid);
        InstanceIdentifier<Role> roleId = InstanceIdentifier.create(Authentication.class).child(Role.class,
                new RoleKey(roleid));
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Role>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION, roleId);
        if (read == null) {
            LOG.error("Failed to read role from data store");
            return null;
        }
        Optional<Role> optional = null;
        try {
            optional = read.get();
        } catch (InterruptedException | ExecutionException e1) {
            LOG.error("Failed to read role from data store", e1);
            return null;
        }

        if (optional == null) {
            return null;
        }

        if (!optional.isPresent()) {
            return null;
        }

        return optional.get();
    }

    public Role deleteRole(String roleid) {
        Preconditions.checkNotNull(roleid);
        Role role = readRole(roleid);
        if (role == null) {
            LOG.error("Failed to delete role from data store, unknown role");
            return null;
        }
        InstanceIdentifier<Role> roleId = InstanceIdentifier.create(Authentication.class).child(Role.class,
                new RoleKey(roleid));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.delete(LogicalDatastoreType.CONFIGURATION, roleId);
        wrt.submit();
        return role;
    }

    public Role updateRole(Role role) {
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getRoleid());
        Role existing = readRole(role.getRoleid());
        RoleBuilder roleBuilder = new RoleBuilder();
        roleBuilder.setDescription(getString(role.getDescription(), existing.getDescription()));
        roleBuilder.setName(existing.getName());
        roleBuilder.setDomainid(existing.getDomainid());
        return writeRole(roleBuilder.build());
    }

    // User methods
    public User writeUser(User user) throws IDMStoreException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getName());
        Preconditions.checkNotNull(user.getDomainid());
        Preconditions.checkNotNull(readDomain(user.getDomainid()));
        UserBuilder userBuilder = new UserBuilder();
        if (user.getSalt() == null) {
            userBuilder.setSalt(SHA256Calculator.generateSALT());
        } else {
            userBuilder.setSalt(user.getSalt());
        }
        userBuilder.setUserid(IDMStoreUtil.createUserid(user.getName(), user.getDomainid()));
        userBuilder.setDescription(user.getDescription());
        userBuilder.setDomainid(user.getDomainid());
        userBuilder.setEmail(user.getEmail());
        userBuilder.setEnabled(user.isEnabled());
        userBuilder.setKey(new UserKey(userBuilder.getUserid()));
        userBuilder.setName(user.getName());
        userBuilder.setPassword(SHA256Calculator.getSHA256(user.getPassword(), userBuilder.getSalt()));
        user = userBuilder.build();
        InstanceIdentifier<User> userId = InstanceIdentifier.create(Authentication.class).child(User.class,
                new UserKey(user.getUserid()));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.put(LogicalDatastoreType.CONFIGURATION, userId, user, true);
        CheckedFuture<Void, TransactionCommitFailedException> submit = wrt.submit();
        if (!waitForSubmit(submit)) {
            return user;
        } else {
            return null;
        }
    }

    public User readUser(String userid) {
        Preconditions.checkNotNull(userid);
        InstanceIdentifier<User> userId = InstanceIdentifier.create(Authentication.class).child(User.class,
                new UserKey(userid));
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<User>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION, userId);
        if (read == null) {
            LOG.error("Failed to read user from data store");
            return null;
        }
        Optional<User> optional = null;
        try {
            optional = read.get();
        } catch (InterruptedException | ExecutionException e1) {
            LOG.error("Failed to read domain from data store", e1);
            return null;
        }

        if (optional == null) {
            return null;
        }

        if (!optional.isPresent()) {
            return null;
        }

        return optional.get();
    }

    public User deleteUser(String userid) {
        Preconditions.checkNotNull(userid);
        User user = readUser(userid);
        if (user == null) {
            LOG.error("Failed to delete user from data store, unknown user");
            return null;
        }
        InstanceIdentifier<User> userId = InstanceIdentifier.create(Authentication.class).child(User.class,
                new UserKey(userid));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.delete(LogicalDatastoreType.CONFIGURATION, userId);
        wrt.submit();
        return user;
    }

    public User updateUser(User user) throws IDMStoreException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getUserid());
        User existing = readUser(user.getUserid());
        UserBuilder userBuilder = new UserBuilder();
        userBuilder.setName(existing.getName());
        userBuilder.setDomainid(existing.getDomainid());
        userBuilder.setDescription(getString(user.getDescription(), existing.getDescription()));
        userBuilder.setEmail(getString(user.getEmail(), existing.getEmail()));
        userBuilder.setEnabled(getBoolean(user.isEnabled(), existing.isEnabled()));
        userBuilder.setPassword(getString(user.getPassword(), existing.getPassword()));
        userBuilder.setSalt(getString(user.getSalt(), existing.getSalt()));
        return writeUser(userBuilder.build());
    }

    // Grant methods
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        Preconditions.checkNotNull(grant);
        Preconditions.checkNotNull(grant.getDomainid());
        Preconditions.checkNotNull(grant.getUserid());
        Preconditions.checkNotNull(grant.getRoleid());
        Preconditions.checkNotNull(readDomain(grant.getDomainid()));
        Preconditions.checkNotNull(readUser(grant.getUserid()));
        Preconditions.checkNotNull(readRole(grant.getRoleid()));
        GrantBuilder grantBuilder = new GrantBuilder();
        grantBuilder.setDomainid(grant.getDomainid());
        grantBuilder.setRoleid(grant.getRoleid());
        grantBuilder.setUserid(grant.getUserid());
        grantBuilder.setGrantid(IDMStoreUtil.createGrantid(grant.getUserid(), grant.getDomainid(), grant.getRoleid()));
        grantBuilder.setKey(new GrantKey(grantBuilder.getGrantid()));
        grant = grantBuilder.build();
        InstanceIdentifier<Grant> grantId = InstanceIdentifier.create(Authentication.class).child(Grant.class,
                new GrantKey(grant.getGrantid()));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.put(LogicalDatastoreType.CONFIGURATION, grantId, grant, true);
        CheckedFuture<Void, TransactionCommitFailedException> submit = wrt.submit();
        if (!waitForSubmit(submit)) {
            return grant;
        } else {
            return null;
        }
    }

    public Grant readGrant(String grantid) {
        Preconditions.checkNotNull(grantid);
        InstanceIdentifier<Grant> grantId = InstanceIdentifier.create(Authentication.class).child(Grant.class,
                new GrantKey(grantid));
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Grant>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION,
                grantId);
        if (read == null) {
            LOG.error("Failed to read grant from data store");
            return null;
        }
        Optional<Grant> optional = null;
        try {
            optional = read.get();
        } catch (InterruptedException | ExecutionException e1) {
            LOG.error("Failed to read domain from data store", e1);
            return null;
        }

        if (optional == null) {
            return null;
        }

        if (!optional.isPresent()) {
            return null;
        }

        return optional.get();
    }

    public Grant deleteGrant(String grantid) {
        Preconditions.checkNotNull(grantid);
        Grant grant = readGrant(grantid);
        if (grant == null) {
            LOG.error("Failed to delete grant from data store, unknown grant");
            return null;
        }
        InstanceIdentifier<Grant> grantId = InstanceIdentifier.create(Authentication.class).child(Grant.class,
                new GrantKey(grantid));
        WriteTransaction wrt = dataBroker.newWriteOnlyTransaction();
        wrt.delete(LogicalDatastoreType.CONFIGURATION, grantId);
        wrt.submit();
        return grant;
    }*/
}
