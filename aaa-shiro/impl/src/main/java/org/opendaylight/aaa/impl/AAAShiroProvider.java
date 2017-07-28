/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import javax.naming.Name;
import javax.servlet.ServletException;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.IdMServiceImpl;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.impl.shiro.oauth2.OAuth2TokenServlet;
import org.opendaylight.aaa.impl.shiro.tokenauthrealm.ServiceLocator;
import org.opendaylight.aaa.impl.shiro.tokenauthrealm.auth.AuthenticationManager;
import org.opendaylight.aaa.impl.shiro.tokenauthrealm.auth.ClientManager;
import org.opendaylight.aaa.impl.shiro.tokenauthrealm.auth.HttpBasicAuth;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for AAA shiro implementation.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class AAAShiroProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    private static volatile AAAShiroProvider INSTANCE;
    private final DataBroker dataBroker;
    private final ICertificateManager certificateManager;
    private final ShiroConfiguration shiroConfiguration;
    private final HttpService httpService;
    private final String moonEndpointPath;
    private final String oauth2EndpointPath;


    /**
     * Provider for this bundle.
     *
     * @param dataBroker injected from blueprint
     */
    private AAAShiroProvider(final DataBroker dataBroker, final ICertificateManager certificateManager,
                             final CredentialAuth<PasswordCredentials> credentialAuth,
                             final IIDMStore iidmStore, final TokenStore tokenStore,
                             final ShiroConfiguration shiroConfiguration,
                             final HttpService httpService,
                             final String moonEndpointPath,
                             final String oauth2EndpointPath,
                             final String authEndpointPath) {
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
        this.shiroConfiguration = shiroConfiguration;
        this.httpService = httpService;
        this.moonEndpointPath = moonEndpointPath;
        this.oauth2EndpointPath = oauth2EndpointPath;

        this.initializeServices(credentialAuth, iidmStore, tokenStore);
        try {
            this.registerServletContexts(this.httpService, this.moonEndpointPath, this.oauth2EndpointPath);
        } catch (final ServletException | NamespaceException e) {
            LOG.warn("Could not initialize AAA servlet endpoints", e);
        }
    }

    private void registerServletContexts(final HttpService httpService, final String moonEndpointPath,
                                         final String oauth2EndpointPath, final String authEndpointPath)
            throws ServletException, NamespaceException {

        Preconditions.checkNotNull(httpService, "httpService cannot be null");

        LOG.info("attempting registration of AAA moon and oauth2 servlets");
        httpService.registerServlet(moonEndpointPath, new org.opendaylight.aaa.shiro.moon.MoonTokenEndpoint(), null, null);
        httpService.registerServlet(oauth2EndpointPath, new OAuth2TokenServlet(), null, null);
        httpService.registerServlet(authEndpointPath, IdmLightApplication.create(), null, null);
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


        final AuthenticationService authService = new AuthenticationManager();
        ServiceLocator.getInstance().setAuthenticationService(authService);


        final ClientService clientService = new ClientManager();
        ServiceLocator.getInstance().setClientService(clientService);


        final IdMService idmService = new IdMServiceImpl(iidmStore);
        ServiceLocator.getInstance().setIdmService(idmService);


        ServiceLocator.getInstance().setCredentialAuth(credentialAuth);


        final TokenAuth tokenAuth = new HttpBasicAuth();
        final List<TokenAuth> tokenAuthList = Lists.newArrayList(tokenAuth);
        ServiceLocator.getInstance().setTokenAuthCollection(tokenAuthList);


        ServiceLocator.getInstance().setTokenStore(tokenStore);
    }

    /**
     * Singleton creation
     *
     * @param dataBroker The DataBroker
     * @param certificateManager the certificate manager
     * @param credentialAuth The CredentialAuth
     * @param iidmStore The IDM store
     * @param tokenStore The token store
     * @return the Provider
     */
    public static AAAShiroProvider newInstance(final DataBroker dataBroker,
                                               final ICertificateManager certificateManager,
                                               final CredentialAuth<PasswordCredentials> credentialAuth,
                                               final IIDMStore iidmStore, final TokenStore tokenStore,
                                               final ShiroConfiguration shiroConfiguration,
                                               final HttpService httpService,
                                               final String moonEndpointPath,
                                               final String oauth2EndpointPath,
                                               final String authEndpointPath) {
        INSTANCE = new AAAShiroProvider(dataBroker, certificateManager, credentialAuth, iidmStore, tokenStore,
                shiroConfiguration, httpService, moonEndpointPath, oauth2EndpointPath, authEndpointPath);
        return INSTANCE;
    }

    /**
     * Singleton extraction
     *
     * @return the Provider
     */
    public static AAAShiroProvider getInstance() {
        return INSTANCE;
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
