/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.encrypt.store;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.aaa.api.DataEncrypter;
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
 * This is a extension of data broker which is used to encrypt the attributes of the models.
 * <p>
 * Following is usage of adding model class and its attributes to be encrypted.
 * <p>
 * AttributeEncryptionDataBroker broker = new DataBroker();
 * broker.add(Domain.class, "Name");
 * Note : attribute name is case sensitive.
 *
 * @author - Sunil Kulkarni(sunikulk@cisco.com)
 **/
public class AttributeEncryptionDataBroker implements DataBroker {
    private static final Logger LOG = LoggerFactory.getLogger(AttributeEncryptionDataBroker.class);

    private final DataBroker dataBroker;
    private final List<ClassData> classDataList;
    private final DataEncrypter dataEncrypter;

    public AttributeEncryptionDataBroker(final DataBroker dataBroker, String configKey) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.dataEncrypter = new DataEncrypter(Preconditions.checkNotNull(configKey));
        classDataList = new ArrayList<>();
    }

    /**
     * Add the class and its attributes to be encrypted
     *
     * @param key
     * @param name
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public void addAttribute(Class<?> key, String name) throws ClassNotFoundException, NoSuchMethodException {
        ClassData classData = getClassData(key);
        if (classData == null) {
            classData = new ClassData(key);
            this.classDataList.add(classData);
        }
        classData.getAttributes().add(new AttributeData(name, classData.getBuilder()));
    }

    private ClassData getClassData(Class<?> cls) {
        for (ClassData data : this.classDataList) {
            if (data.getCanonicalName().equals(cls.getCanonicalName())) {
                return data;
            }
        }
        return null;
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
            final ClassData classData = getClassData(targetType);
            if (classData != null) {
                final Object newInstance = classData.getBuilder().getConstructor(targetType).newInstance(t);
                final Set<AttributeData> attributes = classData.getAttributes();
                for (AttributeData data : attributes) {
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
            final ClassData classData = getClassData(targetType);
            if (classData != null) {
                final Object newInstance = classData.getBuilder().getConstructor(targetType).newInstance(t);
                final Set<AttributeData> attributes = classData.getAttributes();
                for (AttributeData data : attributes) {
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

    private class ClassData {
        private final Class<?> cls;
        private final Class<?> builderClass;
        private final Set<AttributeData> attributes;
        private final Method buildMethod;

        public ClassData(final Class<?> cls) throws ClassNotFoundException, NoSuchMethodException {
            this.cls = Preconditions.checkNotNull(cls);
            this.builderClass = cls.getClassLoader().loadClass(cls.getCanonicalName() + "Builder");
            this.buildMethod = this.builderClass.getMethod("build");
            attributes = new HashSet<>();
        }

        public Object build(Object builderClassInstance) throws InvocationTargetException, IllegalAccessException {
            return this.buildMethod.invoke(builderClassInstance);
        }

        public void addAttribute(String attribute) throws NoSuchMethodException {
            this.attributes.add(new AttributeData(attribute, this.builderClass));
        }

        public String getCanonicalName() {
            return this.cls.getCanonicalName();
        }

        public Set<AttributeData> getAttributes() {
            return attributes;
        }

        public Class<?> getBuilder() {
            return this.builderClass;
        }
    }

    private class AttributeData {
        private final Method getMethod;
        private final Method setMethod;

        public AttributeData(final String attributeName, final Class<?> builderClassInstance) throws NoSuchMethodException {
            Preconditions.checkNotNull(attributeName);
            Preconditions.checkNotNull(builderClassInstance);
            this.getMethod = builderClassInstance.getMethod("get" + attributeName);
            this.setMethod = builderClassInstance.getMethod("set" + attributeName, String.class);
        }

        public String encrypt(String attributeName) throws InvocationTargetException, IllegalAccessException {
            return dataEncrypter.encrypt(attributeName);
        }

        public String decrypt(String encryptedValue) {
            return dataEncrypter.decrypt(encryptedValue);
        }

        public String invokeGetMethod(Object builderClassInstance) throws InvocationTargetException, IllegalAccessException {
            return (String) this.getMethod.invoke(builderClassInstance);
        }

        public void invokeSetMethod(Object builderClassInstance, String value) throws InvocationTargetException, IllegalAccessException {
            this.setMethod.invoke(builderClassInstance, value);
        }
    }
}
