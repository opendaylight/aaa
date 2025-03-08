/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.tokenauthrealm.auth.HttpBasicAuth;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RealmAuthProvider} using {@link IIDMStore} and {@link HttpBasicAuth}.
 */
@NonNullByDefault
@Component(factory = BasicRealmAuthProvider.FACTORY_NAME)
public final class BasicRealmAuthProvider implements RealmAuthProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BasicRealmAuthProvider.class);

    /**
     * OSGi DS Component Factory name.
     */
    public static final String FACTORY_NAME = "org.opendaylight.aaa.shiro.realm.BasicRealmAuthProvider";

    private final List<TokenAuth> tokenAuthenticators;

    @Activate
    public BasicRealmAuthProvider(@Reference final PasswordCredentialAuth credentialAuth,
            @Reference final IIDMStore iidmStore) {
        try {
            new StoreBuilder(iidmStore).initWithDefaultUsers(IIDMStore.DEFAULT_DOMAIN);
        } catch (final IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }
        tokenAuthenticators = List.of(new HttpBasicAuth(credentialAuth));
    }

    @Override
    public List<TokenAuth> tokenAuthenticators() {
        return tokenAuthenticators;
    }
}
