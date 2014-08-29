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
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadWriteTransaction;
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
    return domDataReadWriteTransaction.cancel();
  }

  @Override
  public void delete(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    //TODO: Do AuthZ check here.
    domDataReadWriteTransaction.delete(logicalDatastoreType, yangInstanceIdentifier);
  }

  @Override
  public CheckedFuture<Void, TransactionCommitFailedException> submit() {
    //TODO: Do AuthZ check here.
    return domDataReadWriteTransaction.submit();
  }

  @Override
  public ListenableFuture<RpcResult<TransactionStatus>> commit() {
    //TODO: Do AuthZ check here.
    return domDataReadWriteTransaction.commit();
  }

  @Override
  public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    //TODO: Do AuthZ check here.
    return domDataReadWriteTransaction.read(logicalDatastoreType, yangInstanceIdentifier);
  }

  @Override
  public CheckedFuture<Boolean, ReadFailedException> exists(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    //TODO: Do AuthZ check here.
    return domDataReadWriteTransaction.exists(logicalDatastoreType, yangInstanceIdentifier);
  }

  @Override
  public void put(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier, NormalizedNode<?, ?> normalizedNode) {
    //TODO: Do AuthZ check here?
    domDataReadWriteTransaction.put(logicalDatastoreType, yangInstanceIdentifier, normalizedNode);
  }

  @Override
  public void merge(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier, NormalizedNode<?, ?> normalizedNode) {
    //TODO: Do AuthZ check here?
    domDataReadWriteTransaction.merge(logicalDatastoreType, yangInstanceIdentifier, normalizedNode);
  }

  @Override
  public Object getIdentifier() {
    //TODO: Do AuthZ check here.
    return domDataReadWriteTransaction.getIdentifier();
  }
}
