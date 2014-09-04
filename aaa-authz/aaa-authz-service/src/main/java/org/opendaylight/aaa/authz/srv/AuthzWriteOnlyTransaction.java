/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Created by wdec on 02/09/2014.
 */
public class AuthzWriteOnlyTransaction implements DOMDataWriteTransaction {

  private final DOMDataWriteTransaction domDataWriteTransaction;

  public AuthzWriteOnlyTransaction(DOMDataWriteTransaction wo) {
    this.domDataWriteTransaction = wo;
  }

  @Override
  public void put(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier, NormalizedNode<?, ?> normalizedNode) {
    //TODO: Do AuthZ check here.
    domDataWriteTransaction.put(logicalDatastoreType, yangInstanceIdentifier, normalizedNode);
  }

  @Override
  public void merge(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier, NormalizedNode<?, ?> normalizedNode) {
    //TODO: Do AuthZ check here.
    domDataWriteTransaction.merge(logicalDatastoreType, yangInstanceIdentifier, normalizedNode);
  }

  @Override
  public boolean cancel() {
    return domDataWriteTransaction.cancel();
  }

  @Override
  public void delete(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    //TODO: Do AuthZ check here.
    domDataWriteTransaction.delete(logicalDatastoreType, yangInstanceIdentifier);

  }

  @Override
  public CheckedFuture<Void, TransactionCommitFailedException> submit() {
    //TODO: Do AuthZ check here.
    return domDataWriteTransaction.submit();
  }

  @Override
  public ListenableFuture<RpcResult<TransactionStatus>> commit() {
    //TODO: Do AuthZ check here.
    return domDataWriteTransaction.commit();
  }

  @Override
  public Object getIdentifier() {
    //TODO: Do AuthZ check here.
    return domDataWriteTransaction.getIdentifier();
  }
}
