/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadWriteTransaction;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Created by wdec on 26/08/2014.
 */
public class AuthzDataReadWriteTransaction implements DOMDataReadWriteTransaction {

    private final DOMDataReadWriteTransaction domDataReadWriteTransaction;

    public AuthzDataReadWriteTransaction(DOMDataReadWriteTransaction domDataReadWriteTransaction) {
        this.domDataReadWriteTransaction = domDataReadWriteTransaction;
    }

    @Override
    public boolean cancel() {
        if (AuthzServiceImpl.isAuthorized(ActionType.Cancel)) {
            return domDataReadWriteTransaction.cancel();
        }
        return false;
    }

    @Override
    public void delete(LogicalDatastoreType logicalDatastoreType,
            YangInstanceIdentifier yangInstanceIdentifier) {

        if (AuthzServiceImpl.isAuthorized(logicalDatastoreType, yangInstanceIdentifier,
                ActionType.Delete)) {
            domDataReadWriteTransaction.delete(logicalDatastoreType, yangInstanceIdentifier);
        }
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> submit() {
        if (AuthzServiceImpl.isAuthorized(ActionType.Submit)) {
            return domDataReadWriteTransaction.submit();
        }
        TransactionCommitFailedException e = new TransactionCommitFailedException(
                "Unauthorized User");
        return Futures.immediateFailedCheckedFuture(e);
    }

    @Deprecated
    @Override
    public ListenableFuture<RpcResult<TransactionStatus>> commit() {
        if (AuthzServiceImpl.isAuthorized(ActionType.Commit)) {
            return domDataReadWriteTransaction.commit();
        }
        TransactionCommitFailedException e = new TransactionCommitFailedException(
                "Unauthorized User");
        return Futures.immediateFailedCheckedFuture(e);
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(
            LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {

        if (AuthzServiceImpl.isAuthorized(logicalDatastoreType, yangInstanceIdentifier,
                ActionType.Read)) {
            return domDataReadWriteTransaction.read(logicalDatastoreType, yangInstanceIdentifier);
        }
        ReadFailedException e = new ReadFailedException("Authorization Failed");
        return Futures.immediateFailedCheckedFuture(e);
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(
            LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {

        if (AuthzServiceImpl.isAuthorized(logicalDatastoreType, yangInstanceIdentifier,
                ActionType.Exists)) {
            return domDataReadWriteTransaction.exists(logicalDatastoreType, yangInstanceIdentifier);
        }
        ReadFailedException e = new ReadFailedException("Authorization Failed");
        return Futures.immediateFailedCheckedFuture(e);
    }

    @Override
    public void put(LogicalDatastoreType logicalDatastoreType,
            YangInstanceIdentifier yangInstanceIdentifier, NormalizedNode<?, ?> normalizedNode) {

        if (AuthzServiceImpl.isAuthorized(logicalDatastoreType, yangInstanceIdentifier,
                ActionType.Put)) {
            domDataReadWriteTransaction.put(logicalDatastoreType, yangInstanceIdentifier,
                    normalizedNode);
        }
    }

    @Override
    public void merge(LogicalDatastoreType logicalDatastoreType,
            YangInstanceIdentifier yangInstanceIdentifier, NormalizedNode<?, ?> normalizedNode) {

        if (AuthzServiceImpl.isAuthorized(logicalDatastoreType, yangInstanceIdentifier,
                ActionType.Merge)) {
            domDataReadWriteTransaction.merge(logicalDatastoreType, yangInstanceIdentifier,
                    normalizedNode);
        }
    }

    @Override
    public Object getIdentifier() {
        if (AuthzServiceImpl.isAuthorized(ActionType.GetIdentifier)) {
            return domDataReadWriteTransaction.getIdentifier();
        }
        return null;
    }
}
