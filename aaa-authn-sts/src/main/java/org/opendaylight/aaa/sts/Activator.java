/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

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
        new ServiceTracker<>(context, AuthenticationService.class,
                new AAAServiceTrackerCustomizer<AuthenticationService, AuthenticationService>()).open();
        new ServiceTracker<>(context, IdMService.class,
                new AAAServiceTrackerCustomizer<IdMService, IdMService>()).open();
        new ServiceTracker<>(context, TokenAuth.class,
                new AAAServiceTrackerCustomizer<TokenAuth, TokenAuth>()).open();
        new ServiceTracker<>(context, TokenStore.class,
                new AAAServiceTrackerCustomizer<TokenStore, TokenStore>()).open();
        new ServiceTracker<>(context, ClientService.class,
                new AAAServiceTrackerCustomizer<ClientService, ClientService>()).open();
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
    class AAAServiceTrackerCustomizer<R,S> implements ServiceTrackerCustomizer<R,S> {
        private static final String SERVICE_WAS_FOUND_MSG = "{} service was found";

        @SuppressWarnings("unchecked")
        @Override
        public S addingService(ServiceReference<R> reference) {
            R service = reference.getBundle().getBundleContext().getService(reference);
            LOGGER.info(SERVICE_WAS_FOUND_MSG, service.getClass());
            return (S) service;
        }

        @Override
        public void modifiedService(ServiceReference<R> reference, S service) {
        }

        @Override
        public void removedService(ServiceReference<R> reference, S service) {
        }
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }
}
