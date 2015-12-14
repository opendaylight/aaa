/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import java.util.List;
import java.util.concurrent.Callable;

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

import com.google.common.collect.Lists;

/**
 * An activator for the secure token server to inject in a
 * {@link CredentialAuth} implementation.
 *
 * @author liemmn
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class Activator extends DependencyActivatorBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    // Info level messages surrounding AAA STS activation
    private static final String STS_ACTIVATOR_INITIALIZED = "STS Activator initialized; ServiceTracker may still be processing";
    private static final String STS_ACTIVATOR_INITIALIZING = "STS Activator initializing";

    // Definition of several methods called in the ServiceLocator through Reflection
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

    static ServiceTracker<AuthenticationService, AuthenticationService> authenticationService;
    static ServiceTracker<IdMService, IdMService> idmService;
    static ServiceTracker<TokenAuth, TokenAuth> tokenAuthService;
    static ServiceTracker<TokenStore, TokenStore> tokenStoreService;
    static ServiceTracker<ClientService, ClientService> clientService;
    @Override
    public void init(BundleContext context, DependencyManager manager)
            throws Exception {

        LOGGER.info(STS_ACTIVATOR_INITIALIZING);
        manager.add(createComponent()
                .setImplementation(ServiceLocator.getInstance())
                .add(createServiceDependency().setService(CredentialAuth.class)
                    .setRequired(true)
                    .setCallbacks(CREDENTIAL_AUTH_ADDED, CREDENTIAL_AUTH_REMOVED))
                .add(createServiceDependency().setService(ClaimAuth.class)
                .setRequired(false)
                .setCallbacks(CLAIM_AUTH_ADDED, CLAIM_AUTH_REMOVED))
                .add(createServiceDependency().setService(TokenAuth.class)
                    .setRequired(false)
                        .setCallbacks(TOKEN_AUTH_ADDED, TOKEN_AUTH_REMOVED))
                .add(createServiceDependency().setService(TokenStore.class)
                        .setRequired(true)
                        .setCallbacks(TOKEN_STORE_ADDED, TOKEN_STORE_REMOVED))
                .add(createServiceDependency().setService(TokenStore.class)
                  .setRequired(true))
                .add(createServiceDependency().setService(
                        AuthenticationService.class).setRequired(true)
                        .setCallbacks(AUTHENTICATION_SERVICE_ADDED, AUTHENTICATION_SERVICE_REMOVED))
            .add(createServiceDependency().setService(IdMService.class)
                .setRequired(true))
            .add(createServiceDependency().setService(ClientService.class)
                        .setRequired(true)));

        // Async ServiceTrackers to track and load AAA STS bundles
        // TODO Put under config subsystem so this is not needed
        authenticationService = new ServiceTracker<>(context, AuthenticationService.class,
                new AAAServiceTrackerCustomizer<AuthenticationService, AuthenticationService>(new Callable() {
                    @Override
                    public Object call() {
                        AuthenticationService as = (AuthenticationService)authenticationService.getService();
                        ServiceLocator.getInstance().setAuthenticationService(as);
                        return as;
                    }
                }));
        authenticationService.open();
        idmService = new ServiceTracker<>(context, IdMService.class,
                new AAAServiceTrackerCustomizer<IdMService, IdMService>(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        IdMService is = (IdMService)idmService.getService();
                        ServiceLocator.getInstance().setIdmService(is);
                        return is;
                    }
                }));
        idmService.open();
        tokenAuthService = new ServiceTracker<>(context, TokenAuth.class,
                new AAAServiceTrackerCustomizer<TokenAuth, TokenAuth>(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        List<TokenAuth> tokenAuthCollection = (List<TokenAuth>) Lists.newArrayList(tokenAuthService.getService());
                        ServiceLocator.getInstance().setTokenAuthCollection(tokenAuthCollection);
                        return tokenAuthCollection;
                    }
                }));
        tokenAuthService.open();
        tokenStoreService = new ServiceTracker<>(context, TokenStore.class,
                new AAAServiceTrackerCustomizer<TokenStore, TokenStore>(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TokenStore ts = (TokenStore)tokenStoreService.getService();
                        ServiceLocator.getInstance().setTokenStore(ts);
                        return ts;
                    }
                }));
        tokenStoreService.open();
        clientService = new ServiceTracker<>(context, ClientService.class,
                new AAAServiceTrackerCustomizer<ClientService, ClientService>(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ClientService cs = (ClientService)clientService.getService();
                        ServiceLocator.getInstance().setClientService(cs);
                        return cs;
                    }
                }));
        LOGGER.info(STS_ACTIVATOR_INITIALIZED);
    }

    /**
     * Wrapper for AAA generic service loading.
     *
     * @author Ryan Goulding (ryandgoulding@gmail.com)
     *
     * @param <R> should be identical type to S
     * @param <S> should be identical type to R
     */
    class AAAServiceTrackerCustomizer<S> implements ServiceTrackerCustomizer<S,S> {
        private static final String UNABLE_TO_RESOLVE = "Unable to resolve {}";
		private static final String SERVICE_WAS_FOUND_MSG = "{} service was found";
        private Callable<?> callable;
        public AAAServiceTrackerCustomizer(Callable<?> callable) {
            this.callable = callable;
        }
        @SuppressWarnings("unchecked")
        @Override
        public S addingService(ServiceReference<S> reference) {
            S service = reference.getBundle().getBundleContext().getService(reference);
            LOGGER.info(SERVICE_WAS_FOUND_MSG, service.getClass());
            try {
                callable.call();
            } catch (Exception e) {
                LOGGER.error(UNABLE_TO_RESOLVE, service.getClass(), e);
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
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }
}
