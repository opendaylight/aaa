/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activates h2 based data store for AAA through Aries Blueprint.
 */
public class AAAIDMLightModule {

    private static final Logger LOG = LoggerFactory.getLogger(AAAIDMLightModule.class);

    private static volatile IIDMStore store = null;

    public AAAIDMLightModule(final IIDMStore iidmStore) {
        store = iidmStore;
        LOG.info("AAAIDMLight initialized with class of type {}", store.getClass().getName());
    }

    /**
     * Called by blueprint to for store initialization (i.e., add initial IDM data).
     */
    public void initializeStore() {
        try {
            if (store != null) {
                LOG.info("IIDMStore service {} was found", store.getClass().getName());
                new StoreBuilder(store).init();
            }
        } catch (final IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }
    }

    public static final IIDMStore getStore(){
        return store;
    }

    public static final void setStore(final IIDMStore s){
        store = s;
    }
}
