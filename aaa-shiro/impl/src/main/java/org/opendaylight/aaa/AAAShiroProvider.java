/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import com.google.common.base.Preconditions;
import java.util.concurrent.CompletableFuture;
import javax.servlet.ServletException;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.datastore.h2.H2TokenStore;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.HttpBasicAuth;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for AAA shiro implementation.
 */
public final class AAAShiroProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    public static final CompletableFuture<AAAShiroProvider> INSTANCE_FUTURE = new CompletableFuture<>();

    private final DataBroker dataBroker;
    private final ICertificateManager certificateManager;
    private final HttpService httpService;
    private final TokenStore tokenStore;
    private final ShiroConfiguration shiroConfiguration;
    private final String moonEndpointPath;
    private final String oauth2EndpointPath;
    private final TokenAuthenticators tokenAuthenticators;
    private final AuthenticationService authenticationService;
    private final PasswordHashService passwordHashService;

    /**
     * Constructor.
     */
    public AAAShiroProvider(final DataBroker dataBroker,
                            final ICertificateManager certificateManager,
                            final CredentialAuth<PasswordCredentials> credentialAuth,
                            final ShiroConfiguration shiroConfiguration,
                            final HttpService httpService,
                            final String moonEndpointPath,
                            final String oauth2EndpointPath,
                            final DatastoreConfig datastoreConfig,
                            final IIDMStore iidmStore,
                            final AuthenticationService authenticationService,
                            final PasswordHashService passwordHashService) {
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
        this.shiroConfiguration = shiroConfiguration;
        this.httpService = httpService;
        this.moonEndpointPath = moonEndpointPath;
        this.oauth2EndpointPath = oauth2EndpointPath;
        this.authenticationService = authenticationService;
        this.passwordHashService = passwordHashService;

        if (datastoreConfig == null || !datastoreConfig.getStore().equals(DatastoreConfig.Store.H2DataStore)) {
            LOG.info("AAA Datastore has not been initialized");
            tokenStore = null;
            tokenAuthenticators = new TokenAuthenticators();
            return;
        }

        tokenStore = new H2TokenStore(datastoreConfig.getTimeToLive().longValue(),
                datastoreConfig.getTimeToWait().longValue());

        initializeIIDMStore(iidmStore);

        tokenAuthenticators = buildTokenAuthenticators(credentialAuth);

        try {
            this.registerServletContexts();
        } catch (final ServletException | NamespaceException e) {
            LOG.warn("Could not initialize AAA servlet endpoints", e);
        }

        INSTANCE_FUTURE.complete(this);
    }

    private static TokenAuthenticators buildTokenAuthenticators(CredentialAuth<PasswordCredentials> credentialAuth) {
        return new TokenAuthenticators(new HttpBasicAuth(credentialAuth));
    }

    private void registerServletContexts() throws ServletException, NamespaceException {
        LOG.info("attempting registration of AAA moon servlet");

        Preconditions.checkNotNull(httpService, "httpService cannot be null");

        httpService.registerServlet(moonEndpointPath, new org.opendaylight.aaa.shiro.moon.MoonTokenEndpoint(),
                null, null);
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
        if (httpService != null) {
            httpService.unregister(moonEndpointPath);
            httpService.unregister(oauth2EndpointPath);
        }
    }

    /**
     * Extract the data broker.
     *
     * @return the data broker
     */
    public DataBroker getDataBroker() {
        return this.dataBroker;
    }

    /**
     * Extract the certificate manager.
     *
     * @return the certificate manager.
     */
    public ICertificateManager getCertificateManager() {
        return certificateManager;
    }

    /**
     * Extract Shiro related configuration.
     *
     * @return Shiro related configuration.
     */
    public ShiroConfiguration getShiroConfiguration() {
        return this.shiroConfiguration;
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public TokenAuthenticators getTokenAuthenticators() {
        return tokenAuthenticators;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public PasswordHashService getPasswordHashService() {
        return passwordHashService;
    }
}
