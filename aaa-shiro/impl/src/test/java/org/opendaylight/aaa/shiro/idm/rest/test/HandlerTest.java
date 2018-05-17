/*
 * Copyright (c) 2016, 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.idm.rest.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;
import org.opendaylight.aaa.provider.GsonProvider;
import org.opendaylight.aaa.shiro.idm.IdmLightApplication;
import org.opendaylight.aaa.shiro.idm.IdmLightProxy;

public abstract class HandlerTest extends JerseyTest {

    protected IDMTestStore testStore;

    @Override
    protected Application configure() {
        testStore = new IDMTestStore();
        return ResourceConfig.forApplication(new IdmLightApplication(testStore,
                new IdmLightProxy(testStore, new DefaultPasswordHashService())));
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(new GsonProvider<>());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        new StoreBuilder(testStore).initWithDefaultUsers(IIDMStore.DEFAULT_DOMAIN);
    }

    static <T> Entity<T> entity(T obj) {
        return Entity.entity(obj, MediaType.APPLICATION_JSON);
    }
}
