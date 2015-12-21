/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.Provider;

public class AuthzConsumerContextImplTest {

    private Broker.ConsumerSession realconsumercontext;
    private Provider realprovidercontext;
    private AuthzBrokerImpl authzBroker;
    private Broker realbroker;

    @Before
    public void beforeTest() {
        realconsumercontext = Mockito.mock(Broker.ConsumerSession.class);
        realprovidercontext = Mockito.mock(Provider.class);
        realbroker = Mockito.mock(Broker.class);
        realbroker.registerProvider(realprovidercontext);
        authzBroker = Mockito.mock(AuthzBrokerImpl.class);
    }

    @org.junit.Test
    public void testGetService() throws Exception {
        AuthzConsumerContextImpl authzConsumerContext = new AuthzConsumerContextImpl(
                realconsumercontext, authzBroker);

        Assert.assertEquals("Expected Authz session context",
                authzConsumerContext.getService(DOMDataBroker.class).getClass(),
                AuthzDomDataBroker.class);
        // Assert.assertEquals("Expected Authz session context",
        // authzConsumerContext.getService(SchemaService.class).getClass(),
        // SchemaService.class);
    }
}