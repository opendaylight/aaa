/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.aaa.idm.store.h2.H2Store;
import org.opendaylight.aaa.idm.store.h2.H2TokenStore;
import org.opendaylight.aaa.idm.store.mdsal.IdManagementMdsaLStore;
import org.opendaylight.aaa.idm.store.mdsal.MdsalStore;
import org.opendaylight.aaa.idm.store.mdsal.MdsalTokenStore;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.config.aaa.idm.store.config.rev170517.IdmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaaIdmLight {

    private static final Logger LOG = LoggerFactory.getLogger(AaaIdmLight.class);
    private static IIDMStore store;
    private static TokenStore tokenStore;

    public AaaIdmLight(IdmConfig idmConfig, DataBroker dataBroker, AAAEncryptionService dataEncrypter) {
        if (idmConfig.getDefaultStore().equals(IdmConfig.DefaultStore.H2DataStore)) {
            store = new H2Store();
            tokenStore = new H2TokenStore(idmConfig.getTimeToLive().longValue(), idmConfig.getTimeToWait().longValue());
        } else if (idmConfig.getDefaultStore().equals(IdmConfig.DefaultStore.MdsalDataStore)) {
            store = new MdsalStore(new IdManagementMdsaLStore(dataBroker));
            tokenStore = new MdsalTokenStore(dataBroker, dataEncrypter, idmConfig.getTimeToLive().longValue());
        } else {
            store = null;
            tokenStore = null;
            LOG.info("AAAIDMLight has not been initialized, Default store type is {}", idmConfig.getDefaultStore().getName());
            return;
        }
        LOG.info("AAAIDMLight has been initialized, Default store type is {}", idmConfig.getDefaultStore().getName());
    }

    public void initializeStore() {
        try {
            if (store != null) {
                new StoreBuilder(store).init();
            }
        } catch (IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }
    }

    public static final TokenStore getTokenStore(){
        return tokenStore;
    }

    public static final IIDMStore getStore(){
        return store;
    }

    public static final void setStore(IIDMStore dataStore) {
        store = dataStore;
    }
}
