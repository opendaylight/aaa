/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import java.util.Map;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.controller.md.sal.dom.api.DOMDataChangeListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadWriteTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMTransactionChain;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.BrokerService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Created by wdec on 26/08/2014.
 */
public class AuthzDomDataBroker implements BrokerService, DOMDataBroker {

    private DOMDataBroker domDataBroker;
    private Broker.ProviderSession providerSession;

    private volatile AuthenticationService authService;

    final static AuthzDomDataBroker INSTANCE = new AuthzDomDataBroker();

    public static AuthzDomDataBroker getInstance() {
        return INSTANCE;
    }

    public void setDomDataBroker(DOMDataBroker domDataBroker) {
        this.domDataBroker = domDataBroker;
    }

    public void setProviderSession(Broker.ProviderSession providerSession) {
        this.providerSession = providerSession;
    }

    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }

    public AuthenticationService getAuthService() {
        return this.authService;
    }

    @Override
    public DOMDataReadOnlyTransaction newReadOnlyTransaction() {
        // new Authz transaction + inject real DOM Transaction
        DOMDataReadOnlyTransaction ro = domDataBroker.newReadOnlyTransaction();

        // return domDataBroker.newReadOnlyTransaction(); //Return original
        return new AuthzReadOnlyTransaction(ro);
    }

    @Override
    public Map<Class<? extends DOMDataBrokerExtension>, DOMDataBrokerExtension> getSupportedExtensions() {
        return domDataBroker.getSupportedExtensions();
    }

    @Override
    public DOMDataReadWriteTransaction newReadWriteTransaction() {
        // return new Authz transaction + inject real DOM Transaction
        DOMDataReadWriteTransaction rw = domDataBroker.newReadWriteTransaction();
        return new AuthzDataReadWriteTransaction(rw);
    }

    @Override
    public DOMDataWriteTransaction newWriteOnlyTransaction() {
        DOMDataWriteTransaction wo = domDataBroker.newWriteOnlyTransaction();
        return new AuthzWriteOnlyTransaction(wo);
    }

    @Override
    public ListenerRegistration<DOMDataChangeListener> registerDataChangeListener(
            LogicalDatastoreType logicalDatastoreType,
            YangInstanceIdentifier yangInstanceIdentifier,
            DOMDataChangeListener domDataChangeListener, DataChangeScope dataChangeScope) {
        return domDataBroker.registerDataChangeListener(logicalDatastoreType,
                yangInstanceIdentifier, domDataChangeListener, dataChangeScope);
    }

    @Override
    public DOMTransactionChain createTransactionChain(
            TransactionChainListener transactionChainListener) {
        return domDataBroker.createTransactionChain(transactionChainListener);
    }
}
