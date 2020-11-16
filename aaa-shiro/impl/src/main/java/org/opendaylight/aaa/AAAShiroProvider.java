/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.datastore.h2.H2TokenStore;
import org.opendaylight.aaa.tokenauthrealm.auth.HttpBasicAuth;
import org.opendaylight.aaa.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for AAA shiro implementation.
 */
public final class AAAShiroProvider implements TokenProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    private final TokenStore tokenStore;
    private final TokenAuthenticators tokenAuthenticators;

    /**
     * Constructor.
     */
    public AAAShiroProvider(final PasswordCredentialAuth credentialAuth,
                            final DatastoreConfig datastoreConfig,
                            final IIDMStore iidmStore) {
        if (datastoreConfig == null || !datastoreConfig.getStore().equals(DatastoreConfig.Store.H2DataStore)) {
            LOG.info("AAA Datastore has not been initialized");
            tokenStore = null;
            tokenAuthenticators = new TokenAuthenticators();
            return;
        }

        tokenStore = new H2TokenStore(datastoreConfig.getTimeToLive().longValue(),
                datastoreConfig.getTimeToWait().longValue());

        initializeIIDMStore(iidmStore);

        tokenAuthenticators = new TokenAuthenticators(new HttpBasicAuth(credentialAuth));
    }

    private static void initializeIIDMStore(final IIDMStore iidmStore) {
        try {
            new StoreBuilder(iidmStore).initWithDefaultUsers(IIDMStore.DEFAULT_DOMAIN);
        } catch (final IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("AAAShiroProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("AAAShiroProvider Closed");
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public TokenAuthenticators getTokenAuthenticators() {
        return tokenAuthenticators;
    }
}
