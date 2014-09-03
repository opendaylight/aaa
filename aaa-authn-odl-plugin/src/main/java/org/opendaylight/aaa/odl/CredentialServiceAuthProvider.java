/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.odl;

import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.controller.netconf.auth.AuthProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AuthProvider implementation delegating to AD-SAL UserManager instance.
 */
public final class CredentialServiceAuthProvider implements AuthProvider {
    private static final Logger logger = LoggerFactory.getLogger(CredentialServiceAuthProvider.class);

    public static final String DOMAIN = null;
    private final CredServiceTrackerCustomizer credServiceTrackerCustomizer;

    public CredentialServiceAuthProvider(final BundleContext bundleContext) {

        credServiceTrackerCustomizer = new CredServiceTrackerCustomizer(bundleContext);
        final ServiceTracker<CredentialAuth<PasswordCredentials>, CredentialAuth<PasswordCredentials>> listenerTracker =
                new ServiceTracker<>(bundleContext, CredentialAuth.class.getName(), credServiceTrackerCustomizer);
        listenerTracker.open();
    }

    /**
     * Authenticate user. This implementation tracks IUserManager and delegates the decision to it. If the service is not
     * available, IllegalStateException is thrown.
     */
    @Override
    public synchronized boolean authenticated(final String username, final String password) {
        CredentialAuth<PasswordCredentials> credService = credServiceTrackerCustomizer.getNullableCredService();
        if (credService == null) {
            logger.warn("Cannot authenticate user '{}', Credential service is missing", username);
            throw new IllegalStateException("Credential service is not available");
        }

        Claim claim;
        try {
            claim = credService.authenticate(new PasswordCredentialsWrapper(username, password), DOMAIN);
        } catch (AuthenticationException e) {
            logger.debug("Authentication failed for user '{}' : {}", username);
            return false;
        }

        logger.debug("Authentication result for user '{}' : {}", username, claim.domain());
        return true;
    }

    private static final class PasswordCredentialsWrapper implements PasswordCredentials {
        private final String username;
        private final String password;

        public PasswordCredentialsWrapper(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public String username() {
            return username;
        }

        @Override
        public String password() {
            return password;
        }
    }

    private static final class CredServiceTrackerCustomizer implements ServiceTrackerCustomizer<CredentialAuth<PasswordCredentials>, CredentialAuth<PasswordCredentials>> {
        private final BundleContext bundleContext;

        private CredentialAuth<PasswordCredentials> nullableCredService;

        public CredServiceTrackerCustomizer(final BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        public synchronized CredentialAuth<PasswordCredentials> getNullableCredService() {
            return nullableCredService;
        }

        @Override
        public CredentialAuth<PasswordCredentials> addingService(final ServiceReference<CredentialAuth<PasswordCredentials>> reference) {
            logger.trace("Credential service {} added", reference);
            synchronized (this) {
                nullableCredService = bundleContext.getService(reference);
            }
            return nullableCredService;
        }

        @Override
        public void modifiedService(final ServiceReference<CredentialAuth<PasswordCredentials>> reference, final CredentialAuth<PasswordCredentials> service) {
            logger.trace("Replacing modified Credential service {}", reference);
            synchronized (this) {
                nullableCredService = service;
            }
        }

        @Override
        public void removedService(final ServiceReference<CredentialAuth<PasswordCredentials>> reference, final CredentialAuth<PasswordCredentials> service) {
            logger.trace("Removing Credential service {}. This AuthProvider will fail to authenticate every time", reference);
            synchronized (this) {
                nullableCredService = null;
            }
        }
    }
}
