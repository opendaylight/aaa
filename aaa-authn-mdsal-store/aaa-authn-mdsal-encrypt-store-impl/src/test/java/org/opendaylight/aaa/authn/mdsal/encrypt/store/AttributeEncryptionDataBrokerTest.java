/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.encrypt.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Authentication;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author - Sunil Kulkarni(sunikulk@cisco.com)
 **/
public class AttributeEncryptionDataBrokerTest {

    private DataBroker dataBroker = Mockito.mock(DataBroker.class);

    private WriteTransaction writeTransaction = Mockito.mock(WriteTransaction.class);

    private ReadOnlyTransaction readOnlyTransaction = Mockito.mock(ReadOnlyTransaction.class);

    private ReadWriteTransaction readWriteTransaction = Mockito.mock(ReadWriteTransaction.class);

    private AttributeEncryptionDataBroker encryptionDataBroker;

    private CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = Mockito.mock(CheckedFuture.class);

    private CheckedFuture<Optional<Domain>, ReadFailedException> readCheckedFuture = Mockito.mock(CheckedFuture.class);

    @Before
    public void setup() throws NoSuchMethodException, ClassNotFoundException {
        encryptionDataBroker = new AttributeEncryptionDataBroker(dataBroker, "foo_key_test");
        encryptionDataBroker.addAttribute(Domain.class, "Name");
    }

    @Test
    public void testWriteTransactionPut() throws TransactionCommitFailedException {
        Mockito.when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        Mockito.doNothing().when(writeTransaction).put(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), Mockito.any(Domain.class), Mockito.anyBoolean());
        Mockito.when(writeTransaction.submit()).thenReturn(checkedFuture);

        final WriteTransaction encryptedWriteTransaction = encryptionDataBroker.newWriteOnlyTransaction();
        Domain domain = getDomain();
        ArgumentCaptor<DataObject> putObjects = ArgumentCaptor.forClass(DataObject.class);
        InstanceIdentifier<Domain> domainInstanceIdentifier = InstanceIdentifier.create(Authentication.class).child(
                Domain.class, new DomainKey(domain.getDomainid()));
        encryptedWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, domainInstanceIdentifier, domain, true);
        encryptedWriteTransaction.submit().checkedGet();

        Mockito.verify(writeTransaction, Mockito.times(1)).put(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), putObjects.capture(), Mockito.anyBoolean());
        assertTrue(putObjects.getAllValues().size() == 1);
        assertTrue(putObjects.getAllValues().get(0) instanceof Domain);
        String encryptedDomainName = ((Domain) putObjects.getAllValues().get(0)).getName();
        assertEquals("Encrypted:a0sez3BNMgbonDxxPehR6A==", encryptedDomainName);
    }

    @Test
    public void testWriteTransactionMerge() throws TransactionCommitFailedException {
        Mockito.when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        Mockito.doNothing().when(writeTransaction).merge(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), Mockito.any(Domain.class), Mockito.anyBoolean());
        Mockito.when(writeTransaction.submit()).thenReturn(checkedFuture);

        final WriteTransaction encryptedWriteTransaction = encryptionDataBroker.newWriteOnlyTransaction();
        Domain domain = getDomain();
        ArgumentCaptor<DataObject> mergeObjects = ArgumentCaptor.forClass(DataObject.class);
        InstanceIdentifier<Domain> domainInstanceIdentifier = InstanceIdentifier.create(Authentication.class).child(
                Domain.class, new DomainKey(domain.getDomainid()));
        encryptedWriteTransaction.merge(LogicalDatastoreType.CONFIGURATION, domainInstanceIdentifier, domain, true);
        encryptedWriteTransaction.submit().checkedGet();

        Mockito.verify(writeTransaction, Mockito.times(1)).merge(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), mergeObjects.capture(), Mockito.anyBoolean());
        assertTrue(mergeObjects.getAllValues().size() == 1);
        assertTrue(mergeObjects.getAllValues().get(0) instanceof Domain);
        String encryptedDomainName = ((Domain) mergeObjects.getAllValues().get(0)).getName();
        assertEquals("Encrypted:a0sez3BNMgbonDxxPehR6A==", encryptedDomainName);
    }

    @Test
    public void testReadTransaction() throws ReadFailedException {
        Domain domain = getDomain();
        InstanceIdentifier<Domain> domainInstanceIdentifier = InstanceIdentifier.create(Authentication.class).child(
                Domain.class, new DomainKey(domain.getDomainid()));
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        Mockito.when(readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, domainInstanceIdentifier)).thenReturn(readCheckedFuture);
        Mockito.when(readCheckedFuture.checkedGet()).thenReturn(Optional.of(getEncryptedDomain()));
        final ReadTransaction encryptedReadTransaction = encryptionDataBroker.newReadOnlyTransaction();
        final Domain readDomain = encryptedReadTransaction.read(LogicalDatastoreType.OPERATIONAL, domainInstanceIdentifier).checkedGet().get();
        assertNotNull(readDomain);
        assertNotNull(readDomain.getName());
        assertEquals("Test", readDomain.getName());
    }

    @Test
    public void testReadWriteTransactionPut() throws TransactionCommitFailedException {
        Mockito.when(dataBroker.newReadWriteTransaction()).thenReturn(readWriteTransaction);
        Mockito.doNothing().when(readWriteTransaction).put(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), Mockito.any(Domain.class), Mockito.anyBoolean());
        Mockito.when(readWriteTransaction.submit()).thenReturn(checkedFuture);

        final WriteTransaction encryptedReadWriteTransaction = encryptionDataBroker.newReadWriteTransaction();
        Domain domain = getDomain();
        ArgumentCaptor<DataObject> putObjects = ArgumentCaptor.forClass(DataObject.class);
        InstanceIdentifier<Domain> domainInstanceIdentifier = InstanceIdentifier.create(Authentication.class).child(
                Domain.class, new DomainKey(domain.getDomainid()));
        encryptedReadWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, domainInstanceIdentifier, domain, true);
        encryptedReadWriteTransaction.submit().checkedGet();

        Mockito.verify(readWriteTransaction, Mockito.times(1)).put(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), putObjects.capture(), Mockito.anyBoolean());
        assertTrue(putObjects.getAllValues().size() == 1);
        assertTrue(putObjects.getAllValues().get(0) instanceof Domain);
        String encryptedDomainName = ((Domain) putObjects.getAllValues().get(0)).getName();
        assertEquals("Encrypted:a0sez3BNMgbonDxxPehR6A==", encryptedDomainName);
    }

    @Test
    public void testReadWriteTransactionMerge() throws TransactionCommitFailedException {
        Mockito.when(dataBroker.newReadWriteTransaction()).thenReturn(readWriteTransaction);
        Mockito.doNothing().when(readWriteTransaction).merge(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), Mockito.any(Domain.class), Mockito.anyBoolean());
        Mockito.when(readWriteTransaction.submit()).thenReturn(checkedFuture);

        final WriteTransaction encryptedReadWriteTransaction = encryptionDataBroker.newReadWriteTransaction();
        Domain domain = getDomain();
        ArgumentCaptor<DataObject> mergeObjects = ArgumentCaptor.forClass(DataObject.class);
        InstanceIdentifier<Domain> domainInstanceIdentifier = InstanceIdentifier.create(Authentication.class).child(
                Domain.class, new DomainKey(domain.getDomainid()));
        encryptedReadWriteTransaction.merge(LogicalDatastoreType.CONFIGURATION, domainInstanceIdentifier, domain, true);
        encryptedReadWriteTransaction.submit().checkedGet();

        Mockito.verify(readWriteTransaction, Mockito.times(1)).merge(Mockito.any(LogicalDatastoreType.class), Mockito.any(InstanceIdentifier.class), mergeObjects.capture(), Mockito.anyBoolean());
        assertTrue(mergeObjects.getAllValues().size() == 1);
        assertTrue(mergeObjects.getAllValues().get(0) instanceof Domain);
        String encryptedDomainName = ((Domain) mergeObjects.getAllValues().get(0)).getName();
        assertEquals("Encrypted:a0sez3BNMgbonDxxPehR6A==", encryptedDomainName);
    }

    @Test
    public void testReadWriteTransactionRead() throws ReadFailedException {
        Domain domain = getDomain();
        InstanceIdentifier<Domain> domainInstanceIdentifier = InstanceIdentifier.create(Authentication.class).child(
                Domain.class, new DomainKey(domain.getDomainid()));
        Mockito.when(dataBroker.newReadWriteTransaction()).thenReturn(readWriteTransaction);
        Mockito.when(readWriteTransaction.read(LogicalDatastoreType.OPERATIONAL, domainInstanceIdentifier)).thenReturn(readCheckedFuture);
        Mockito.when(readCheckedFuture.checkedGet()).thenReturn(Optional.of(getEncryptedDomain()));
        final ReadTransaction encryptedReadWReadTransaction = encryptionDataBroker.newReadWriteTransaction();
        final Domain readDomain = encryptedReadWReadTransaction.read(LogicalDatastoreType.OPERATIONAL, domainInstanceIdentifier).checkedGet().get();
        assertNotNull(readDomain);
        assertNotNull(readDomain.getName());
        assertEquals("Test", readDomain.getName());
    }

    private Domain getDomain() {
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDescription("Test Description");
        domainBuilder.setDomainid("Test");
        domainBuilder.setEnabled(true);
        domainBuilder.setKey(new DomainKey(domainBuilder.getDomainid()));
        domainBuilder.setName("Test");
        return domainBuilder.build();
    }

    private Domain getEncryptedDomain() {
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDescription("Test Description");
        domainBuilder.setDomainid("Test");
        domainBuilder.setEnabled(true);
        domainBuilder.setKey(new DomainKey(domainBuilder.getDomainid()));
        domainBuilder.setName("Encrypted:a0sez3BNMgbonDxxPehR6A==");
        return domainBuilder.build();
    }

}
