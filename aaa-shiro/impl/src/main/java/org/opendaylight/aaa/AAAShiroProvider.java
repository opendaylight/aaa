/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.servlet.ServletException;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.IdMServiceImpl;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.datastore.h2.H2TokenStore;
import org.opendaylight.aaa.shiro.oauth2.OAuth2TokenServlet;
import org.opendaylight.aaa.shiro.tokenauthrealm.ServiceLocator;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.AuthenticationManager;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.HttpBasicAuth;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
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
    private final ShiroConfiguration shiroConfiguration;
    private final String moonEndpointPath;
    private final String oauth2EndpointPath;

    /**
     * Constructor.
     */
    public AAAShiroProvider(final DataBroker dataBroker, final ICertificateManager certificateManager,
                             final CredentialAuth<PasswordCredentials> credentialAuth,
                             final ShiroConfiguration shiroConfiguration,
                             final HttpService httpService,
                             final String moonEndpointPath,
                             final String oauth2EndpointPath,
                             final DatastoreConfig datastoreConfig,
                             final IIDMStore idmStore) {
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
        this.shiroConfiguration = shiroConfiguration;
        this.httpService = httpService;
        this.moonEndpointPath = moonEndpointPath;
        this.oauth2EndpointPath = oauth2EndpointPath;

        if (datastoreConfig == null || !datastoreConfig.getStore().equals(DatastoreConfig.Store.H2DataStore)) {
            LOG.info("AAA Datastore has not been initialized");
            return;
        }

        final TokenStore tokenStore = new H2TokenStore(datastoreConfig.getTimeToLive().longValue(),
                datastoreConfig.getTimeToWait().longValue());
        this.initializeServices(credentialAuth, idmStore, tokenStore);
        try {
            this.registerServletContexts(this.httpService, this.moonEndpointPath, this.oauth2EndpointPath);
        } catch (final ServletException | NamespaceException e) {
            LOG.warn("Could not initialize AAA servlet endpoints", e);
        }

        INSTANCE_FUTURE.complete(this);
    }

    private void registerServletContexts(final HttpService httpService, final String moonEndpointPath,
                                         final String oauth2EndpointPath)
            throws ServletException, NamespaceException {
        LOG.info("attempting registration of AAA moon, oauth2 and auth servlets");

        Preconditions.checkNotNull(httpService, "httpService cannot be null");
        httpService.registerServlet(moonEndpointPath, new org.opendaylight.aaa.shiro.moon.MoonTokenEndpoint(),
                null, null);
        httpService.registerServlet(oauth2EndpointPath, new OAuth2TokenServlet(), null, null);
    }

    /**
     * Initialize AAA Services.  This method will evolve over time as ServiceLocator is refactored/removed.
     *
     * @param credentialAuth wired via blueprint
     * @param iidmStore wired via blueprint
     * @param tokenStore wired via blueprint
     */
    private void initializeServices(final CredentialAuth<PasswordCredentials> credentialAuth,
                                    final IIDMStore iidmStore, final TokenStore tokenStore) {
        try {
            new StoreBuilder(iidmStore).init();
        } catch (final IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }

        final AuthenticationService authService = new AuthenticationManager();
        ServiceLocator.getInstance().setAuthenticationService(authService);

        final IdMService idmService = new IdMServiceImpl(iidmStore);
        ServiceLocator.getInstance().setIdmService(idmService);

        ServiceLocator.getInstance().setCredentialAuth(credentialAuth);

        final TokenAuth tokenAuth = new HttpBasicAuth();
        final List<TokenAuth> tokenAuthList = Lists.newArrayList(tokenAuth);
        ServiceLocator.getInstance().setTokenAuthCollection(tokenAuthList);

        ServiceLocator.getInstance().setTokenStore(tokenStore);
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
}
