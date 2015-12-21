/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.Broker.ProviderSession;
import org.opendaylight.controller.sal.core.api.BrokerService;
import org.opendaylight.controller.sal.core.spi.ForwardingProviderSession;

/**
 * Created by wdec on 28/08/2014.
 */
public class AuthzProviderContextImpl extends ForwardingProviderSession {

    private final Broker.ProviderSession realSession;

    public AuthzProviderContextImpl(Broker.ProviderSession providerSession,
            AuthzBrokerImpl authzBroker) {
        this.realSession = providerSession;
    }

    @Override
    protected ProviderSession delegate() {
        // TODO Auto-generated method stub
        return realSession;
    }

    @Override
    public <T extends BrokerService> T getService(Class<T> tClass) {
        T t;
        // Check for class and return Authz broker only for DOMBroker
        if (tClass == DOMDataBroker.class) {
            t = (T) AuthzDomDataBroker.getInstance();
        } else {
            t = realSession.getService(tClass);
        }
        // AuthzDomDataBroker.getInstance().setDomDataBroker((DOMDataBroker)t);
        return t;
    }
}
