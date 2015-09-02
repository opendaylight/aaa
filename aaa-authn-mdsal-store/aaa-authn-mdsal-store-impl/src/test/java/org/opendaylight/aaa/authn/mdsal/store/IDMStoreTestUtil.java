package org.opendaylight.aaa.authn.mdsal.store;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Domain;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.DomainKey;
import org.opendaylight.aaa.authn.mdsal.store.IDMStore;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Role;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.RoleBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.RoleKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.User;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.UserBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.UserKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Grant;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.GrantBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.GrantKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.AAA;

import java.lang.String;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.base.Optional;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;

import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;
public class IDMStoreTestUtil {
    /* DataBroker mocked with Mokito */
    protected static DataBroker dataBroker = mock(DataBroker.class);
    protected static WriteTransaction wrt = mock(WriteTransaction.class);
    protected static ReadOnlyTransaction rot = null;

    static {
         rot = (ReadOnlyTransaction)DataBrokerReadMocker.addMock(ReadOnlyTransaction.class);
         when(dataBroker.newReadOnlyTransaction()).thenReturn(rot);
         when(dataBroker.newWriteOnlyTransaction()).thenReturn(wrt);
    }

    /* Domain Data Object Instance */
    public Domain domain = createdomain();
    /* Domain create Method */
    public Domain createdomain(){
        /* Start of Domain builder  */
        DomainBuilder domainbuilder = new DomainBuilder();
        domainbuilder.setName("SETNAME");
        domainbuilder.setKey(new DomainKey("SETDOMAINID"));
        domainbuilder.setDomainid("SETDOMAINID");
        domainbuilder.setDescription("SETDESCRIPTION");
        domainbuilder.setEnabled(true);
        /* End of Domain builder  */
        return domainbuilder.build();
    }
    /* Role Data Object Instance */
    public Role role = createrole();
    /* Role create Method */
    public Role createrole(){
        /* Start of Role builder  */
        RoleBuilder rolebuilder = new RoleBuilder();
        rolebuilder.setName("SETNAME");
        rolebuilder.setKey(new RoleKey("SETROLEID"));
        rolebuilder.setDomainid("SETDOMAINID");
        rolebuilder.setRoleid("SETROLEID");
        rolebuilder.setDescription("SETDESCRIPTION");
        /* End of Role builder  */
        return rolebuilder.build();
    }
    /* User Data Object Instance */
    public User user = createuser();
    /* User create Method */
    public User createuser(){
        /* Start of User builder  */
        UserBuilder userbuilder = new UserBuilder();
        userbuilder.setName("SETNAME");
        userbuilder.setKey(new UserKey("SETUSERID"));
        userbuilder.setDomainid("SETDOMAINID");
        userbuilder.setUserid("SETUSERID");
        userbuilder.setEmail("SETEMAIL");
        userbuilder.setPassword("SETPASSWORD");
        userbuilder.setSalt("SETSALT");
        userbuilder.setEnable(true);
        userbuilder.setDescription("SETDESCRIPTION");
        /* End of User builder  */
        return userbuilder.build();
    }
    /* Grant Data Object Instance */
    public Grant grant = creategrant();
    /* Grant create Method */
    public Grant creategrant(){
        /* Start of Grant builder  */
        GrantBuilder grantbuilder = new GrantBuilder();
        grantbuilder.setKey(new GrantKey("Test"));
        grantbuilder.setDomainid("SETDOMAINID");
        grantbuilder.setRoleid("SETROLEID");
        grantbuilder.setUserid("SETUSERID");
        grantbuilder.setGrantid("SETGRANTID");
        /* End of Grant builder  */
        return grantbuilder.build();
    }
    /* InstanceIdentifier for Grant instance grant */
    public InstanceIdentifier<Grant> grantID = InstanceIdentifier
        .create(AAA.class)
        .child(Grant.class, new GrantKey(new String("Test")));

    /* Mokito DataBroker method for grant Data Object */
    public void addMokitoForgrant() {
        CheckedFuture<Optional<Grant>, ReadFailedException> read = mock(CheckedFuture.class);
        try{
            DataBrokerReadMocker.getMoker(rot).addWhen("read", new Object[]{LogicalDatastoreType.CONFIGURATION, grantID}, read);
        }catch(Exception err){
            err.printStackTrace();
        }
        Optional<Grant> optional = mock(Optional.class);
        try {when(read.get()).thenReturn(optional);} catch (InterruptedException | ExecutionException e) {}
        when(optional.get()).thenReturn(grant);
        when(optional.isPresent()).thenReturn(true);
    }
    /* InstanceIdentifier for Domain instance domain */
    public InstanceIdentifier<Domain> domainID = InstanceIdentifier
        .create(AAA.class)
        .child(Domain.class, new DomainKey(new String("SETDOMAINID")));

    /* Mokito DataBroker method for domain Data Object */
    public void addMokitoFordomain() {
        CheckedFuture<Optional<Domain>, ReadFailedException> read = mock(CheckedFuture.class);
        try{
            DataBrokerReadMocker.getMoker(rot).addWhen("read", new Object[]{LogicalDatastoreType.CONFIGURATION, domainID}, read);
        }catch(Exception err){
            err.printStackTrace();
        }
        Optional<Domain> optional = mock(Optional.class);
        try {when(read.get()).thenReturn(optional);} catch (InterruptedException | ExecutionException e) {}
        when(optional.get()).thenReturn(domain);
        when(optional.isPresent()).thenReturn(true);
    }
    /* InstanceIdentifier for Role instance role */
    public InstanceIdentifier<Role> roleID = InstanceIdentifier
        .create(AAA.class)
        .child(Role.class, new RoleKey(new String("SETROLEID")));

    /* Mokito DataBroker method for role Data Object */
    public void addMokitoForrole() {
        CheckedFuture<Optional<Role>, ReadFailedException> read = mock(CheckedFuture.class);
        try{
            DataBrokerReadMocker.getMoker(rot).addWhen("read", new Object[]{LogicalDatastoreType.CONFIGURATION, roleID}, read);
        }catch(Exception err){
            err.printStackTrace();
        }
        Optional<Role> optional = mock(Optional.class);
        try {when(read.get()).thenReturn(optional);} catch (InterruptedException | ExecutionException e) {}
        when(optional.get()).thenReturn(role);
        when(optional.isPresent()).thenReturn(true);
    }
    /* InstanceIdentifier for User instance user */
    public InstanceIdentifier<User> userID = InstanceIdentifier
        .create(AAA.class)
        .child(User.class, new UserKey(new String("SETUSERID")));

    /* Mokito DataBroker method for user Data Object */
    public void addMokitoForuser() {
        CheckedFuture<Optional<User>, ReadFailedException> read = mock(CheckedFuture.class);
        try{
            DataBrokerReadMocker.getMoker(rot).addWhen("read", new Object[]{LogicalDatastoreType.CONFIGURATION, userID}, read);
        }catch(Exception err){
            err.printStackTrace();
        }
        Optional<User> optional = mock(Optional.class);
        try {when(read.get()).thenReturn(optional);} catch (InterruptedException | ExecutionException e) {}
        when(optional.get()).thenReturn(user);
        when(optional.isPresent()).thenReturn(true);
    }
}
