/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.shiro.realm.BasicRealmAuthProvider;
import org.opendaylight.aaa.shiro.realm.EmptyRealmAuthProvider;
import org.opendaylight.aaa.shiro.realm.RealmAuthProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for AAA shiro implementation.
 */
public final class AAAShiroProvider implements RealmAuthProvider, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    private final @NonNull RealmAuthProvider delegate;

    /**
     * Constructor.
     */
    public AAAShiroProvider(final PasswordCredentialAuth credentialAuth, final DatastoreConfig datastoreConfig,
            final IIDMStore iidmStore) {
        if (datastoreConfig != null && datastoreConfig.getStore() == DatastoreConfig.Store.H2DataStore) {
            delegate = new BasicRealmAuthProvider(credentialAuth, iidmStore);
            LOG.info("AAAShiroProvider Session Initiated");
        } else {
            delegate = new EmptyRealmAuthProvider();
            LOG.info("AAA Datastore has not been initialized");
        }
    }

    @Override
    public List<TokenAuth> tokenAuthenticators() {
        return delegate.tokenAuthenticators();
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    @Override
    public void close() {
        LOG.info("AAAShiroProvider Closed");
    }
}
