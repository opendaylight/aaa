/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MdsalUtils manages all the mdsal data operation delete, merger, put and read.
 *
 * @author mserngawy
 *
 */
public final class MdsalUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MdsalUtils.class);

    private MdsalUtils() {

    }

    /**
     * Executes delete as a blocking transaction.
     *
     * @param dataBroker Mdsal data Broker
     * @param store {@link LogicalDatastoreType} which should be modified
     * @param path {@link InstanceIdentifier} to read from
     * @param <D> the data object type
     * @return the result of the request
     */
    public static <D extends DataObject> boolean delete(final DataBroker dataBroker, final LogicalDatastoreType store,
            final InstanceIdentifier<D> path)  {
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.delete(store, path);
        try {
            transaction.commit().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to delete {} ", path, e);
        }
        return false;
    }

    /**
     * initialize the data tree for the given InstanceIdentifier type.
     *
     * @param type data store type
     * @param dataBroker Mdsal data Broker
     * @param iid InstanceIdentifier type
     * @param object data object
     */
    public static <T extends DataObject> void initalizeDatastore(final LogicalDatastoreType type,
            final DataBroker dataBroker, final InstanceIdentifier<T> iid, final T object) {
        // Put data to MD-SAL data store
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.put(type, iid, object);

        // Perform the transaction.commit asynchronously
        Futures.addCallback(transaction.commit(), new FutureCallback<CommitInfo>() {
            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("initDatastore: transaction failed");
            }

            @Override
            public void onSuccess(final CommitInfo result) {
                LOG.debug("initDatastore: transaction succeeded");
            }
        }, MoreExecutors.directExecutor());
        LOG.info("initDatastore: data populated: {}, {}, {}", type, iid, object);
    }

    /**
     * Executes merge as a blocking transaction.
     *
     * @param dataBroker Mdsal data Broker
     * @param logicalDatastoreType {@link LogicalDatastoreType} which should be modified
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result of the request
     */
    public static <D extends DataObject> boolean merge(final DataBroker dataBroker,
            final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<D> path, final D data) {
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.mergeParentStructureMerge(logicalDatastoreType, path, data);
        try {
            transaction.commit().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to merge {} ", path, e);
        }
        return false;
    }

    /**
     * Executes put as a blocking transaction.
     *
     * @param dataBroker Mdsal data Broker
     * @param logicalDatastoreType {@link LogicalDatastoreType} which should be modified
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result of the request
     */
    public static <D extends DataObject> boolean put(final DataBroker dataBroker,
            final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<D> path, final D data) {
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.mergeParentStructurePut(logicalDatastoreType, path, data);
        try {
            transaction.commit().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to put {} ", path, e);
        }
        return false;
    }

    /**
     * Executes read as a blocking transaction.
     *
     * @param store {@link LogicalDatastoreType} to read
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result as the data object requested
     */
    public static <D extends DataObject> D read(final DataBroker dataBroker, final LogicalDatastoreType store,
            final InstanceIdentifier<D> path)  {
        try (ReadTransaction transaction = dataBroker.newReadOnlyTransaction()) {
            Optional<D> optionalDataObject = transaction.read(store, path).get();
            if (optionalDataObject.isPresent()) {
                return optionalDataObject.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to read {} ", path, e);
        }

        return null;
    }
}
