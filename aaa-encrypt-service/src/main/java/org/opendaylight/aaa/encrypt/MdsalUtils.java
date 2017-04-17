/*
 * Copyright (c) 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.encrypt;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MdsalUtils manages all the mdsal data operation.
 *
 * @author mserngawy
 *
 */
public class MdsalUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MdsalUtils.class);

    public static InstanceIdentifier<AaaEncryptServiceConfig> getEncryptionSrvConfigIid() {
        return InstanceIdentifier.builder(AaaEncryptServiceConfig.class).build();
    }

    /**
     * initialize the data tree for the given InstanceIdentifier type
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
        Futures.addCallback(transaction.submit(), new FutureCallback<Void>() {
            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("initDatastore: transaction failed");
            }

            @Override
            public void onSuccess(final Void result) {
                LOG.debug("initDatastore: transaction succeeded");
            }
        });
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
        D result = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        Optional<D> optionalDataObject;
        final CheckedFuture<Optional<D>, ReadFailedException> future = transaction.read(store, path);
        try {
            optionalDataObject = future.checkedGet();
            if (optionalDataObject.isPresent()) {
                result = optionalDataObject.get();
            } else {
                LOG.debug("{}: Failed to read {}",
                        Thread.currentThread().getStackTrace()[1], path);
            }
        } catch (final ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        transaction.close();
        return result;
    }
}
