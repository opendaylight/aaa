/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.opendaylight.aaa.datastore.h2.H2Store;
import org.opendaylight.aaa.datastore.h2.H2TokenStore;
import org.opendaylight.aaa.datastore.h2.IdmLightConfig;
import org.opendaylight.aaa.datastore.h2.IdmLightConfigBuilder;
import org.opendaylight.aaa.datastore.h2.IdmLightSimpleConnectionProvider;
import org.opendaylight.aaa.shiro.tokenauthrealm.ServiceLocator;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.HttpBasicAuth;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for AAA shiro implementation.
 */
public class AAAShiroProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    private static final CompletableFuture<AAAShiroProvider> INSTANCE_FUTURE = new CompletableFuture<>();

    private static volatile AAAShiroProvider INSTANCE;

    private static IIDMStore iidmStore;

    private final DataBroker dataBroker;
    private final ICertificateManager certificateManager;
    private final ShiroConfiguration shiroConfiguration;
    private final TokenStore tokenStore;

    /**
     * Provider for this bundle.
     *
     * @param dataBroker injected from blueprint
     * @param authService injected from blueprint
     */
    private AAAShiroProvider(final DataBroker dataBroker, final ICertificateManager certificateManager,
            final CredentialAuth<PasswordCredentials> credentialAuth,
            final ShiroConfiguration shiroConfiguration,
            final DatastoreConfig datastoreConfig,
            final String dbUsername,
            final String dbPassword,
            final AuthenticationService authService) {
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
        this.shiroConfiguration = shiroConfiguration;

        if (datastoreConfig != null && datastoreConfig.getStore()
                .equals(DatastoreConfig.Store.H2DataStore)) {
            final IdmLightConfig config = new IdmLightConfigBuilder().dbUser(dbUsername).dbPwd(dbPassword).build();
            iidmStore = new H2Store(new IdmLightSimpleConnectionProvider(config));
            tokenStore = new H2TokenStore(datastoreConfig.getTimeToLive().longValue(),
                    datastoreConfig.getTimeToWait().longValue());
        } else {
            iidmStore = null;
            tokenStore = null;
            LOG.info("AAA Datastore has not been initialized");
            return;
        }
        this.initializeServices(credentialAuth, iidmStore, tokenStore, authService);
    }

    /**
     * Initialize AAA Services.  This method will evolve over time as ServiceLocator is refactored/removed.
     *
     * @param credentialAuth wired via blueprint
     * @param iidmStore wired via blueprint
     * @param tokenStore wired via blueprint
     * @param authService wired via blueprint
     */
    private void initializeServices(final CredentialAuth<PasswordCredentials> credentialAuth,
            final IIDMStore iidmStore, final TokenStore tokenStore, final AuthenticationService authService) {
        try {
            new StoreBuilder(iidmStore).initWithDefaultUsers(IIDMStore.DEFAULT_DOMAIN);
        } catch (final IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }

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
     * Singleton creation.
     *
     * @param dataBroker the DataBroker
     * @param certificateManager the certificate manager
     * @param credentialAuth the CredentialAuth
     * @param shiroConfiguration shiro config
     * @param datastoreConfig data store config
     * @param dbUsername database username
     * @param dbPassword database password
     * @param authenticationService the authentication service
     * @return the Provider
     */
    public static AAAShiroProvider newInstance(final DataBroker dataBroker,
            final ICertificateManager certificateManager,
            final CredentialAuth<PasswordCredentials> credentialAuth,
            final ShiroConfiguration shiroConfiguration,
            final DatastoreConfig datastoreConfig,
            final String dbUsername,
            final String dbPassword,
            final AuthenticationService authenticationService) {
        INSTANCE = new AAAShiroProvider(dataBroker, certificateManager, credentialAuth, shiroConfiguration,
                datastoreConfig, dbUsername, dbPassword, authenticationService);
        INSTANCE_FUTURE.complete(INSTANCE);
        return INSTANCE;
    }

    /**
     * Singleton extraction.
     *
     * @return the Provider
     */
    public static AAAShiroProvider getInstance() {
        return INSTANCE;
    }

    public static CompletableFuture<AAAShiroProvider> getInstanceFuture() {
        return INSTANCE_FUTURE;
    }

    /**
     * Get IDM data store.
     *
     * @return IIDMStore data store
     */
    public static IIDMStore getIdmStore() {
        return iidmStore;
    }

    /**
     * Set IDM data store, only used for test.
     *
     * @param store data store
     */
    public static void setIdmStore(final IIDMStore store) {
        iidmStore = store;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("AAAShiroProvider Session Initiated");
    }

    /**
     * Extract the data broker.
     *
     * @return the data broker
     */
    public DataBroker getDataBroker() {
        return dataBroker;
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
        return shiroConfiguration;
    }
}
