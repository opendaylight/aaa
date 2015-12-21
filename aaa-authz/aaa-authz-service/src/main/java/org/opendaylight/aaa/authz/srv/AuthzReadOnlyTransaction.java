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

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Created by wdec on 28/08/2014.
 */

public class AuthzReadOnlyTransaction implements DOMDataReadOnlyTransaction {

    private final DOMDataReadOnlyTransaction ro;

    public AuthzReadOnlyTransaction(DOMDataReadOnlyTransaction ro) {
        this.ro = ro;
    }

    @Override
    public void close() {
        ro.close();
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(
            LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {

        if (AuthzServiceImpl.isAuthorized(logicalDatastoreType, yangInstanceIdentifier,
                ActionType.Read)) {
            return ro.read(logicalDatastoreType, yangInstanceIdentifier);
        }
        ReadFailedException e = new ReadFailedException("Authorization Failed");
        return Futures.immediateFailedCheckedFuture(e);
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(
            LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {

        if (AuthzServiceImpl.isAuthorized(ActionType.Exists)) {
            return ro.exists(logicalDatastoreType, yangInstanceIdentifier);
        }
        ReadFailedException e = new ReadFailedException("Authorization Failed");
        return Futures.immediateFailedCheckedFuture(e);
    }

    @Override
    public Object getIdentifier() {
        if (AuthzServiceImpl.isAuthorized(ActionType.GetIdentifier)) {
            return ro.getIdentifier();
        }
        return null;
    }
}
