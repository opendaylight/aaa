/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An activator for the secure token server to inject in a
 * {@link CredentialAuth} implementation.
 *
 * @author liemmn
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class Activator extends DependencyActivatorBase {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    // Definition of several methods called in the ServiceLocator through
    // Reflection
    private static final String AUTHENTICATION_SERVICE_REMOVED = "authenticationServiceRemoved";
    private static final String AUTHENTICATION_SERVICE_ADDED = "authenticationServiceAdded";
    private static final String TOKEN_STORE_REMOVED = "tokenStoreRemoved";
    private static final String TOKEN_STORE_ADDED = "tokenStoreAdded";
    private static final String TOKEN_AUTH_REMOVED = "tokenAuthRemoved";
    private static final String TOKEN_AUTH_ADDED = "tokenAuthAdded";
    private static final String CLAIM_AUTH_REMOVED = "claimAuthRemoved";
    private static final String CLAIM_AUTH_ADDED = "claimAuthAdded";
    private static final String CREDENTIAL_AUTH_REMOVED = "credentialAuthRemoved";
    private static final String CREDENTIAL_AUTH_ADDED = "credentialAuthAdded";

    // A collection of all services, which is used for closing ServiceTrackers
    private ImmutableList<ServiceTracker<?, ?>> services;

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {

        LOG.info("STS Activator initializing");
        manager.add(createComponent().setImplementation(ServiceLocator.getInstance())
                                     .add(createServiceDependency().setService(CredentialAuth.class)
                                                                   .setRequired(true)
                                                                   .setCallbacks(
                                                                           CREDENTIAL_AUTH_ADDED,
                                                                           CREDENTIAL_AUTH_REMOVED))
                                     .add(createServiceDependency().setService(ClaimAuth.class)
                                                                   .setRequired(false)
                                                                   .setCallbacks(CLAIM_AUTH_ADDED,
                                                                           CLAIM_AUTH_REMOVED))
                                     .add(createServiceDependency().setService(TokenAuth.class)
                                                                   .setRequired(false)
                                                                   .setCallbacks(TOKEN_AUTH_ADDED,
                                                                           TOKEN_AUTH_REMOVED))
                                     .add(createServiceDependency().setService(TokenStore.class)
                                                                   .setRequired(true)
                                                                   .setCallbacks(TOKEN_STORE_ADDED,
                                                                           TOKEN_STORE_REMOVED))
                                     .add(createServiceDependency().setService(TokenStore.class)
                                                                   .setRequired(true))
                                     .add(createServiceDependency().setService(
                                             AuthenticationService.class)
                                                                   .setRequired(true)
                                                                   .setCallbacks(
                                                                           AUTHENTICATION_SERVICE_ADDED,
                                                                           AUTHENTICATION_SERVICE_REMOVED))
                                     .add(createServiceDependency().setService(IdMService.class)
                                                                   .setRequired(true))
                                     .add(createServiceDependency().setService(ClientService.class)
                                                                   .setRequired(true)));

        final Builder<ServiceTracker<?, ?>> servicesBuilder = new ImmutableList.Builder<ServiceTracker<?, ?>>();

        // Async ServiceTrackers to track and load AAA STS bundles
        final ServiceTracker<AuthenticationService, AuthenticationService> authenticationService = new ServiceTracker<>(
                context, AuthenticationService.class,
                new AAAServiceTrackerCustomizer<AuthenticationService>(
                        new Function<AuthenticationService, Void>() {
                        @Override
                        public Void apply(AuthenticationService authenticationService) {
                            ServiceLocator.getInstance().setAuthenticationService(
                                    authenticationService);
                            return null;
                        }
                    }));
        servicesBuilder.add(authenticationService);
        authenticationService.open();

        final ServiceTracker<IdMService, IdMService> idmService = new ServiceTracker<>(context,
                IdMService.class, new AAAServiceTrackerCustomizer<IdMService>(
                        new Function<IdMService, Void>() {
                            @Override
                            public Void apply(IdMService idmService) {
                                ServiceLocator.getInstance().setIdmService(idmService);
                                return null;
                            }
                        }));
        servicesBuilder.add(idmService);
        idmService.open();

        final ServiceTracker<TokenAuth, TokenAuth> tokenAuthService = new ServiceTracker<>(context,
                TokenAuth.class, new AAAServiceTrackerCustomizer<TokenAuth>(
                        new Function<TokenAuth, Void>() {
                            @Override
                            public Void apply(TokenAuth tokenAuth) {
                                final List<TokenAuth> tokenAuthCollection = (List<TokenAuth>) Lists
                                        .newArrayList(tokenAuth);
                                ServiceLocator.getInstance().setTokenAuthCollection(
                                        tokenAuthCollection);
                                return null;
                            }
                        }));
        servicesBuilder.add(tokenAuthService);
        tokenAuthService.open();

        final ServiceTracker<TokenStore, TokenStore> tokenStoreService = new ServiceTracker<>(
                context, TokenStore.class, new AAAServiceTrackerCustomizer<TokenStore>(
                        new Function<TokenStore, Void>() {
                            @Override
                            public Void apply(TokenStore tokenStore) {
                                ServiceLocator.getInstance().setTokenStore(tokenStore);
                                return null;
                            }
                        }));
        servicesBuilder.add(tokenStoreService);
        tokenStoreService.open();

        final ServiceTracker<ClientService, ClientService> clientService = new ServiceTracker<>(
                context, ClientService.class, new AAAServiceTrackerCustomizer<ClientService>(
                        new Function<ClientService, Void>() {
                            @Override
                            public Void apply(ClientService clientService) {
                                ServiceLocator.getInstance().setClientService(clientService);
                                return null;
                            }
                        }));
        servicesBuilder.add(clientService);
        clientService.open();

        services = servicesBuilder.build();

        LOG.info("STS Activator initialized; ServiceTracker may still be processing");
    }

    /**
     * Wrapper for AAA generic service loading.
     *
     * @param <S> service
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    static final class AAAServiceTrackerCustomizer<S> implements ServiceTrackerCustomizer<S, S> {

        private Function<S, Void> callback;

        AAAServiceTrackerCustomizer(final Function<S, Void> callback) {
            this.callback = callback;
        }

        @Override
        public S addingService(ServiceReference<S> reference) {
            S service = reference.getBundle().getBundleContext().getService(reference);
            LOG.info("Attempting to resolve {} through AAAServiceTrackerCustomizer", service.getClass());
            try {
                callback.apply(service);
            } catch (Exception e) {
                LOG.error("Unable to resolve {}", service.getClass(), e);
            }
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<S> reference, S service) {
        }

        @Override
        public void removedService(ServiceReference<S> reference, S service) {
        }
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {

        for (ServiceTracker<?, ?> serviceTracker : services) {
            serviceTracker.close();
        }
    }
}
