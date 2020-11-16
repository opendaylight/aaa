/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.Objects;
import javax.inject.Inject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfigBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = TokenProvider.class)
@Designate(ocd = OSGiAAAShiroProvider.DatastoreConfiguration.class)
public class OSGiAAAShiroProvider implements TokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OSGiAAAShiroProvider.class);

    @ObjectClassDefinition(description = "DatastoreConfiguration for aaa-shiro.")
    public @interface DatastoreConfiguration {
        @AttributeDefinition(description = "Store type")
        String storeType() default "h2-data-store";

        @AttributeDefinition(description = "Time to live for tokens in seconds")
        long timeToLive() default 36000;

        @AttributeDefinition(description = "Time to wait for tokens in seconds")
        long timeToWait() default 3600;
    }

    private AAAShiroProvider delegate;

    @Inject
    @Activate
    public OSGiAAAShiroProvider(final @NonNull DatastoreConfiguration datastoreConfiguration,
            final @Reference PasswordCredentialAuth credentialAuth, final @Reference IIDMStore iidmStore) {
        LOG.warn("Activate");

        delegate = new AAAShiroProvider(credentialAuth, processDatastoreConfiguration(datastoreConfiguration),
                iidmStore);
        delegate.init();
    }

    @Override
    public TokenAuthenticators getTokenAuthenticators() {
        return delegate.getTokenAuthenticators();
    }

    @Override
    public TokenStore getTokenStore() {
        return delegate.getTokenStore();
    }

    private static DatastoreConfig processDatastoreConfiguration(final DatastoreConfiguration datastoreConfiguration) {
        Objects.requireNonNull(datastoreConfiguration);

        return new DatastoreConfigBuilder()
                .setStore(DatastoreConfig.Store.forName(datastoreConfiguration.storeType()))
                .setTimeToLive(Uint64.valueOf(datastoreConfiguration.timeToLive()))
                .setTimeToWait(Uint64.valueOf(datastoreConfiguration.timeToWait()))
                .build();
    }
}
