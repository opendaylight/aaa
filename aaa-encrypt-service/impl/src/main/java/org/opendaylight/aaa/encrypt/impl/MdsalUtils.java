/*
 * Copyright (c) 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

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
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MdsalUtils manages all the mdsal data operation.
 *
 * @author mserngawy
 */
public final class MdsalUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MdsalUtils.class);

    private MdsalUtils() {

    }

    public static InstanceIdentifier<AaaEncryptServiceConfig> getEncryptionSrvConfigIid() {
        return InstanceIdentifier.builder(AaaEncryptServiceConfig.class).build();
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

        // Perform the transaction.submit asynchronously
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
     * Executes read as a blocking transaction.
     *
     * @param store {@link LogicalDatastoreType} to read
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result as the data object requested
     */
    public static <D extends org.opendaylight.yangtools.yang.binding.DataObject> D read(
            final DataBroker dataBroker, final LogicalDatastoreType store, final InstanceIdentifier<D> path)  {
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
