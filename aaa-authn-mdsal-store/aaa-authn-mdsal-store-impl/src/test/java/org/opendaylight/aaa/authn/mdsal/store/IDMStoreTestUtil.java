/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.concurrent.ExecutionException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Authentication;
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
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.UserKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IDMStoreTestUtil {
    /* DataBroker mocked with Mokito */
    protected static DataBroker dataBroker = mock(DataBroker.class);
    protected static WriteTransaction wrt = mock(WriteTransaction.class);
    protected static ReadOnlyTransaction rot = null;

    static {
        rot = (ReadOnlyTransaction) DataBrokerReadMocker.addMock(ReadOnlyTransaction.class);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(rot);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(wrt);
    }

    /* Domain Data Object Instance */
    public Domain domain = createdomain();

    /* Domain create Method */
    public Domain createdomain() {
        /* Start of Domain builder */
        DomainBuilder domainbuilder = new DomainBuilder();
        domainbuilder.setName("SETNAME");
        domainbuilder.setDomainid("SETNAME");
        domainbuilder.setKey(new DomainKey("SETNAME"));
        domainbuilder.setDescription("SETDESCRIPTION");
        domainbuilder.setEnabled(true);
        /* End of Domain builder */
        return domainbuilder.build();
    }

    /* Role Data Object Instance */
    public Role role = createrole();

    /* Role create Method */
    public Role createrole() {
        /* Start of Role builder */
        RoleBuilder rolebuilder = new RoleBuilder();
        rolebuilder.setRoleid("SETNAME@SETNAME");
        rolebuilder.setName("SETNAME");
        rolebuilder.setKey(new RoleKey(rolebuilder.getRoleid()));
        rolebuilder.setDomainid(createdomain().getDomainid());
        rolebuilder.setDescription("SETDESCRIPTION");
        /* End of Role builder */
        return rolebuilder.build();
    }

    /* User Data Object Instance */
    public User user = createuser();

    /* User create Method */
    public User createuser() {
        /* Start of User builder */
        UserBuilder userbuilder = new UserBuilder();
        userbuilder.setUserid("SETNAME@SETNAME");
        userbuilder.setName("SETNAME");
        userbuilder.setKey(new UserKey(userbuilder.getUserid()));
        userbuilder.setDomainid(createdomain().getDomainid());
        userbuilder.setEmail("SETEMAIL");
        userbuilder.setPassword("SETPASSWORD");
        userbuilder.setSalt("SETSALT");
        userbuilder.setEnabled(true);
        userbuilder.setDescription("SETDESCRIPTION");
        /* End of User builder */
        return userbuilder.build();
    }

    /* Grant Data Object Instance */
    public Grant grant = creategrant();

    /* Grant create Method */
    public Grant creategrant() {
        /* Start of Grant builder */
        GrantBuilder grantbuilder = new GrantBuilder();
        grantbuilder.setDomainid(createdomain().getDomainid());
        grantbuilder.setRoleid(createrole().getRoleid());
        grantbuilder.setUserid(createuser().getUserid());
        grantbuilder.setGrantid(IDMStoreUtil.createGrantid(grantbuilder.getUserid(),
                grantbuilder.getDomainid(), grantbuilder.getRoleid()));
        grantbuilder.setKey(new GrantKey(grantbuilder.getGrantid()));
        /* End of Grant builder */
        return grantbuilder.build();
    }

    /* InstanceIdentifier for Grant instance grant */
    public InstanceIdentifier<Grant> grantID = InstanceIdentifier.create(Authentication.class)
                                                                 .child(Grant.class,
                                                                         creategrant().getKey());

    /* Mokito DataBroker method for grant Data Object */
    public void addMokitoForgrant() throws NoSuchMethodException, SecurityException, InterruptedException, ExecutionException {
        CheckedFuture<Optional<Grant>, ReadFailedException> read = mock(CheckedFuture.class);
        DataBrokerReadMocker.getMocker(rot).addWhen("read",
                new Object[] { LogicalDatastoreType.CONFIGURATION, grantID }, read);
        Optional<Grant> optional = mock(Optional.class);
        when(read.get()).thenReturn(optional);
        when(optional.get()).thenReturn(grant);
        when(optional.isPresent()).thenReturn(true);
    }

    /* InstanceIdentifier for Domain instance domain */
    public InstanceIdentifier<Domain> domainID = InstanceIdentifier.create(Authentication.class)
                                                                   .child(Domain.class,
                                                                           new DomainKey(
                                                                                   new String(
                                                                                           "SETNAME")));

    /* Mokito DataBroker method for domain Data Object */
    public void addMokitoFordomain() throws NoSuchMethodException, SecurityException, InterruptedException, ExecutionException {
        CheckedFuture<Optional<Domain>, ReadFailedException> read = mock(CheckedFuture.class);
        DataBrokerReadMocker.getMocker(rot).addWhen("read",
                new Object[] { LogicalDatastoreType.CONFIGURATION, domainID }, read);
        Optional<Domain> optional = mock(Optional.class);
        when(read.get()).thenReturn(optional);
        when(optional.get()).thenReturn(domain);
        when(optional.isPresent()).thenReturn(true);
    }

    /* InstanceIdentifier for Role instance role */
    public InstanceIdentifier<Role> roleID = InstanceIdentifier.create(Authentication.class).child(
            Role.class, createrole().getKey());

    /* Mokito DataBroker method for role Data Object */
    public void addMokitoForrole() throws NoSuchMethodException, SecurityException, InterruptedException, ExecutionException {
        CheckedFuture<Optional<Role>, ReadFailedException> read = mock(CheckedFuture.class);
        DataBrokerReadMocker.getMocker(rot).addWhen("read",
                new Object[] { LogicalDatastoreType.CONFIGURATION, roleID }, read);
        Optional<Role> optional = mock(Optional.class);
        when(read.get()).thenReturn(optional);
        when(optional.get()).thenReturn(role);
        when(optional.isPresent()).thenReturn(true);
    }

    /* InstanceIdentifier for User instance user */
    public InstanceIdentifier<User> userID = InstanceIdentifier.create(Authentication.class).child(
            User.class, createuser().getKey());

    /* Mokito DataBroker method for user Data Object */
    public void addMokitoForuser() throws NoSuchMethodException, SecurityException, InterruptedException, ExecutionException {
        CheckedFuture<Optional<User>, ReadFailedException> read = mock(CheckedFuture.class);
        DataBrokerReadMocker.getMocker(rot).addWhen("read",
                new Object[] { LogicalDatastoreType.CONFIGURATION, userID }, read);
        Optional<User> optional = mock(Optional.class);
        when(read.get()).thenReturn(optional);
        when(optional.get()).thenReturn(user);
        when(optional.isPresent()).thenReturn(true);
    }
}
