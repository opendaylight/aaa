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
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MdsalUtils manages all the mdsal data operation delete, merger, put and read.
 *
 * @author mserngawy
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
     * @param path {@link DataObjectIdentifier} to read from
     * @param <D> the data object type
     * @return the result of the request
     */
    public static <D extends DataObject> boolean delete(final DataBroker dataBroker, final LogicalDatastoreType store,
            final DataObjectIdentifier<D> path)  {
        final var transaction = dataBroker.newWriteOnlyTransaction();
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
     * initialize the data tree for the given DataObjectIdentifier type.
     *
     * @param type data store type
     * @param dataBroker Mdsal data Broker
     * @param iid DataObjectIdentifier type
     * @param object data object
     */
    public static <T extends DataObject> void initalizeDatastore(final LogicalDatastoreType type,
            final DataBroker dataBroker, final DataObjectIdentifier<T> iid, final T object) {
        // Put data to MD-SAL data store
        final var transaction = dataBroker.newWriteOnlyTransaction();
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
     * @param path {@link DataObjectIdentifier} for path to read
     * @param <D> the data object type
     * @return the result of the request
     */
    public static <D extends DataObject> boolean merge(final DataBroker dataBroker,
            final LogicalDatastoreType logicalDatastoreType, final DataObjectIdentifier<D> path, final D data) {
        final var transaction = dataBroker.newWriteOnlyTransaction();
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
     * @param path {@link DataObjectIdentifier} for path to read
     * @param <D> the data object type
     * @return the result of the request
     */
    public static <D extends DataObject> boolean put(final DataBroker dataBroker,
            final LogicalDatastoreType logicalDatastoreType, final DataObjectIdentifier<D> path, final D data) {
        final var transaction = dataBroker.newWriteOnlyTransaction();
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
     * @param path {@link DataObjectIdentifier} for path to read
     * @param <D> the data object type
     * @return the result as the data object requested
     */
    public static <D extends DataObject> D read(final DataBroker dataBroker, final LogicalDatastoreType store,
            final DataObjectIdentifier<D> path)  {
        try (var transaction = dataBroker.newReadOnlyTransaction()) {
            return transaction.read(store, path).get().orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to read {} ", path, e);
            return null;
        }
    }
}
