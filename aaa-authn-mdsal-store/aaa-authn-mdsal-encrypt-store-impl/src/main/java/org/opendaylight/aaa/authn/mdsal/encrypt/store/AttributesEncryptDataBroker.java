/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 */

package org.opendaylight.aaa.authn.mdsal.encrypt.store;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sunikulk on 4/11/2016.
 */
public abstract class AttributesEncryptDataBroker implements DataBroker {

    private static final Logger LOG = LoggerFactory.getLogger(AttributesEncryptDataBroker.class);

    private final DataBroker dataBroker;
    private final AttributeEncryptionMap attributeEncryptionMap;

    public AttributesEncryptDataBroker(final DataBroker dataBroker, final AttributeEncryptionMap attributeEncryptionMap) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.attributeEncryptionMap = Preconditions.checkNotNull(attributeEncryptionMap);
    }

    @Override
    public ReadOnlyTransaction newReadOnlyTransaction() {
        return new AttributesEncryptReadOnlyTransaction(dataBroker.newReadOnlyTransaction());
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return new AttributesEncryptReadWriteTransaction(dataBroker.newReadWriteTransaction());
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new AttributesEncryptWriteTransactionImpl(this.dataBroker.newWriteOnlyTransaction());
    }

    @Override
    public ListenerRegistration<DataChangeListener> registerDataChangeListener(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier, DataChangeListener dataChangeListener, DataChangeScope dataChangeScope) {
        return dataBroker.registerDataChangeListener(logicalDatastoreType, instanceIdentifier, dataChangeListener, dataChangeScope);
    }

    @Override
    public BindingTransactionChain createTransactionChain(TransactionChainListener transactionChainListener) {
        return dataBroker.createTransactionChain(transactionChainListener);
    }

    @Nonnull
    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(DataTreeIdentifier<T> dataTreeIdentifier, L l) {
        return dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, l);
    }

    private class AttributesEncryptWriteTransactionImpl implements WriteTransaction {

        private final WriteTransaction writeTransaction;


        public AttributesEncryptWriteTransactionImpl(final WriteTransaction writeTransaction) {
            this.writeTransaction = Preconditions.checkNotNull(writeTransaction);
        }

        @Override
        public <T extends DataObject> void put(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t) {
            T instance = getEncryptedData(instanceIdentifier, t);
            writeTransaction.put(logicalDatastoreType, instanceIdentifier, instance);
        }

        @Override
        public <T extends DataObject> void put(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, boolean b) {
            T instance = getEncryptedData(instanceIdentifier, t);
            writeTransaction.put(logicalDatastoreType, instanceIdentifier, instance, b);
        }


        @Override
        public <T extends DataObject> void merge(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t) {
            T instance = getEncryptedData(instanceIdentifier, t);
            writeTransaction.merge(logicalDatastoreType, instanceIdentifier, instance);
        }

        @Override
        public <T extends DataObject> void merge(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, boolean b) {
            T instance = getEncryptedData(instanceIdentifier, t);
            writeTransaction.merge(logicalDatastoreType, instanceIdentifier, instance, b);
        }

        @Override
        public boolean cancel() {
            return writeTransaction.cancel();
        }

        @Override
        public void delete(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier) {
            writeTransaction.delete(logicalDatastoreType, instanceIdentifier);
        }

        @Override
        public CheckedFuture<Void, TransactionCommitFailedException> submit() {
            return writeTransaction.submit();
        }

        @Override
        @Deprecated
        public ListenableFuture<RpcResult<TransactionStatus>> commit() {
            return writeTransaction.commit();
        }

        @Override
        public Object getIdentifier() {
            return writeTransaction.getIdentifier();
        }
    }

    private class AttributesEncryptReadOnlyTransaction implements ReadOnlyTransaction {

        private final ReadOnlyTransaction readOnlyTransaction;

        public AttributesEncryptReadOnlyTransaction(final ReadOnlyTransaction readOnlyTransaction) {
            this.readOnlyTransaction = Preconditions.checkNotNull(readOnlyTransaction);
        }

        @Override
        public void close() {
            readOnlyTransaction.close();
        }

        @Override
        public <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> read(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier) {
            final CheckedFuture<Optional<T>, ReadFailedException> data = readOnlyTransaction.read(logicalDatastoreType, instanceIdentifier);
            try {
                final Optional<T> optional = data.checkedGet();
                final T instance = getDecryptedData(instanceIdentifier, optional.get());
                return Futures.immediateCheckedFuture(Optional.of(instance));
            } catch (ReadFailedException e) {
                LOG.warn("Error while decrypting attributes", e);
                return Futures.immediateFailedCheckedFuture(e);
            }
        }

        @Override
        public Object getIdentifier() {
            return readOnlyTransaction.getIdentifier();
        }


    }

    private class AttributesEncryptReadWriteTransaction implements ReadWriteTransaction {

        private final ReadWriteTransaction readWriteTransaction;

        public AttributesEncryptReadWriteTransaction(final ReadWriteTransaction readWriteTransaction) {
            this.readWriteTransaction = Preconditions.checkNotNull(readWriteTransaction);
        }

        @Override
        public <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> read(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier) {
            final CheckedFuture<Optional<T>, ReadFailedException> data = readWriteTransaction.read(logicalDatastoreType, instanceIdentifier);
            try {
                final Optional<T> optional = data.checkedGet();
                final T instance = getDecryptedData(instanceIdentifier, optional.get());
                return Futures.immediateCheckedFuture(Optional.of(instance));
            } catch (ReadFailedException e) {
                LOG.warn("Error while decrypting attributes", e);
                return Futures.immediateFailedCheckedFuture(e);
            }
        }

        @Override
        public <T extends DataObject> void put(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t) {
            T instance = getEncryptedData(instanceIdentifier, t);
            this.readWriteTransaction.put(logicalDatastoreType, instanceIdentifier, instance);
        }

        @Override
        public <T extends DataObject> void put(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, boolean b) {
            T instance = getEncryptedData(instanceIdentifier, t);
            this.readWriteTransaction.put(logicalDatastoreType, instanceIdentifier, instance, b);
        }

        @Override
        public <T extends DataObject> void merge(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t) {
            T instance = getEncryptedData(instanceIdentifier, t);
            this.readWriteTransaction.merge(logicalDatastoreType, instanceIdentifier, instance);
        }

        @Override
        public <T extends DataObject> void merge(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, boolean b) {
            T instance = getEncryptedData(instanceIdentifier, t);
            this.readWriteTransaction.merge(logicalDatastoreType, instanceIdentifier, instance, b);
        }

        @Override
        public boolean cancel() {
            return this.readWriteTransaction.cancel();
        }

        @Override
        public void delete(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier) {
            this.readWriteTransaction.delete(logicalDatastoreType, instanceIdentifier);
        }

        @Override
        public CheckedFuture<Void, TransactionCommitFailedException> submit() {
            return this.readWriteTransaction.submit();
        }

        @Override
        @Deprecated
        public ListenableFuture<RpcResult<TransactionStatus>> commit() {
            return this.readWriteTransaction.commit();
        }

        @Override
        public Object getIdentifier() {
            return this.readWriteTransaction.getIdentifier();
        }
    }

    private <T> T getEncryptedData(InstanceIdentifier instanceIdentifier, T t) {
        Object instance = t;
        try {
            final Class targetType = instanceIdentifier.getTargetType();
            final AttributeEncryptionMap.ClassData classData = this.attributeEncryptionMap.getClassData(targetType);
            if (classData != null) {
                final Object newInstance = classData.getBuilder().getConstructor(targetType).newInstance(t);
                final List<AttributeEncryptionMap.AttributeData> attributes = classData.getAttributes();
                for (AttributeEncryptionMap.AttributeData data : attributes) {
                    String value = data.invokeGetMethod(newInstance);
                    String encryptedValue = data.encrypt(value);
                    data.invokeSetMethod(newInstance, encryptedValue);
                }
                instance = classData.build(newInstance);
            }
        } catch (Exception e) {
            LOG.warn("Error while encryption of data", e);
        }
        return (T) instance;
    }

    private <T> T getDecryptedData(InstanceIdentifier instanceIdentifier, T t) {
        Object instance = t;
        try {
            final Class targetType = instanceIdentifier.getTargetType();
            final AttributeEncryptionMap.ClassData classData = this.attributeEncryptionMap.getClassData(targetType);
            if (classData != null) {
                final Object newInstance = classData.getBuilder().getConstructor(targetType).newInstance(t);
                final List<AttributeEncryptionMap.AttributeData> attributes = classData.getAttributes();
                for (AttributeEncryptionMap.AttributeData data : attributes) {
                    final String encryptedValue = data.invokeGetMethod(newInstance);
                    final String value = data.decrypt(encryptedValue);
                    data.invokeSetMethod(newInstance, value);
                }
                instance = classData.build(newInstance);
            }
        } catch (Exception e) {
            LOG.warn("Error while decryption of data", e);
        }
        return (T) instance;
    }
}
