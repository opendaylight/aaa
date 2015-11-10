/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * Important debug information related to ServiceLocator member changes is
 * enabled through the following command:
 * <code>log:set debug org.opendaylight.aaa.sts</code>
 *
 * @author liemmn
 *
 */
public class ServiceLocator {

    private static final ServiceLocator INSTANCE = new ServiceLocator();

    private static final Logger LOG = LoggerFactory.getLogger(ServiceLocator.class);

    protected volatile List<TokenAuth> tokenAuthCollection = new Vector<>();

    protected volatile CredentialAuth<PasswordCredentials> credentialAuth;

    protected volatile TokenStore tokenStore;

    protected volatile AuthenticationService authenticationService;

    protected volatile IdMService idmService;

    protected volatile ClientService clientService;

    private ServiceLocator() {
        // private to support singleton pattern
    }

    public static ServiceLocator getInstance() {
        return INSTANCE;
    }

    /**
     * Called through reflection by the sts activator.
     *
     * @see org.opendaylight.aaa.sts.Activator
     * @param tokenAuth
     */
    protected void tokenAuthAdded(TokenAuth tokenAuth) {
        final String tokenAuthClassName = tokenAuth.getClass().getName();
        LOG.debug("Adding TokenAuth with class name " + tokenAuthClassName);
        this.tokenAuthCollection.add(tokenAuth);
    }

    /**
     * Called through reflection by the sts activator.
     *
     * @see org.opendaylight.aaa.sts.Activator
     * @param tokenAuth
     */
    protected void tokenAuthRemoved(TokenAuth tokenAuth) {
        final String tokenAuthClassName = tokenAuth.getClass().getName();
        LOG.debug("OSGi triggered removal of TokenAuth with class name " + tokenAuthClassName);
        this.tokenAuthCollection.remove(tokenAuth);
    }

    protected void tokenStoreAdded(TokenStore tokenStore) {
        final String tokenStoreClassName = tokenStore.getClass().getName();
        LOG.debug("OSGi triggered addition of TokenStore with class name " + tokenStoreClassName);
        this.tokenStore = tokenStore;
    }

    protected void tokenStoreRemoved(TokenStore tokenStore) {
        final String tokenStoreClassName = tokenStore.getClass().getName();
        LOG.debug("OSGi triggered removal of TokenStore with class name " + tokenStoreClassName);
        this.tokenStore = null;
    }

    protected void authenticationServiceAdded(AuthenticationService authenticationService) {
        final String authenticationServiceClassName = authenticationService.getClass().getName();
        LOG.debug("OSGi triggered addition of AuthenticationService with class name " + authenticationServiceClassName);
        this.authenticationService = authenticationService;
    }

    protected void authenticationServiceRemoved(AuthenticationService authenticationService) {
        final String authenticationServiceClassName = authenticationService.getClass().getName();
        LOG.debug("OSGi triggered removal of AuthenticationService with class name " + authenticationServiceClassName);
        this.authenticationService = null;
    }

    protected void credentialAuthAdded(CredentialAuth<PasswordCredentials> credentialAuth) {
        final String credentialAuthClassName = credentialAuth.getClass().getName();
        LOG.debug("OSGi triggered addition of CredentialAuth with class name " + credentialAuthClassName);
        this.credentialAuth = credentialAuth;
    }

    protected void credentialAuthAddedRemoved(CredentialAuth<PasswordCredentials> credentialAuth) {
        final String credentialAuthClassName = credentialAuth.getClass().getName();
        LOG.debug("OSGi triggered removal of CredentialAuth with class name " + credentialAuthClassName);
        this.credentialAuth = null;
    }

    //
    // public facing APIs that can be explicitly called.
    //

    public List<TokenAuth> getTokenAuthCollection() {
        return tokenAuthCollection;
    }

    public void setTokenAuthCollection(
            List<TokenAuth> tokenAuthCollection) {
        String changeMessage = "ODL triggered modification of TokenAuthCollection";
        final String toTokenAuthCollection =
                Arrays.toString(tokenAuthCollection.toArray());
        final String fromTokenAuthCollection =
                Arrays.toString(this.tokenAuthCollection.toArray());
        if (!fromTokenAuthCollection.equals(toTokenAuthCollection)) {
            changeMessage += "; the TokenAuthCollection is changed from "
                    + fromTokenAuthCollection
                    + " to " + toTokenAuthCollection;
        }
        LOG.debug(changeMessage);
        this.tokenAuthCollection = tokenAuthCollection;
    }

    public CredentialAuth<PasswordCredentials> getCredentialAuth() {
        return credentialAuth;
    }

    public synchronized void setCredentialAuth(
            CredentialAuth<PasswordCredentials> credentialAuth) {
        String changeMessage = "ODL triggered modification of CredentialAuth";
        final String toCredentialAuthClassName = credentialAuth.getClass().getName();
        final String fromCredentialAuthClassName = this.credentialAuth.getClass().getName();
        if (!fromCredentialAuthClassName.equals(toCredentialAuthClassName)) {
            changeMessage += "; the CredentialAuth class is changed from "
                    + fromCredentialAuthClassName
                    + " to " + toCredentialAuthClassName;
        }
        LOG.debug(changeMessage);
        this.credentialAuth = credentialAuth;
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public void setTokenStore(TokenStore tokenStore) {
        String changeMessage = "ODL triggered modification of TokenStore";
        final String toTokenStoreClassName = tokenStore.getClass().getName();
        final String fromTokenStoreClassName = this.tokenStore.getClass().getName();
        if (!fromTokenStoreClassName.equals(toTokenStoreClassName)) {
            changeMessage += "; the TokenStore class is changed from "
                    + fromTokenStoreClassName
                    + " to " + toTokenStoreClassName;
        }
        LOG.debug(changeMessage);
        this.tokenStore = tokenStore;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(
            AuthenticationService authenticationService) {
        String changeMessage = "ODL triggered modification of AuthenticationService";
        final String toAuthenticationServiceClassName = authenticationService.getClass().getName();
        final String fromAuthenticationServiceClassName = this.authenticationService.getClass().getName();
        if (!fromAuthenticationServiceClassName.equals(toAuthenticationServiceClassName)) {
            changeMessage += "; the AuthenticationService class is changed from "
                    + fromAuthenticationServiceClassName
                    + " to " + toAuthenticationServiceClassName;
        }
        LOG.debug(changeMessage);
        this.authenticationService = authenticationService;
    }

    public IdMService getIdmService() {
        return idmService;
    }

    public void setIdmService(IdMService idmService) {
        String changeMessage = "ODL triggered modification of IdMService";
        final String toIdMServiceClassName = idmService.getClass().getName();
        final String fromIdMServiceClassName = this.idmService.getClass().getName();
        if (!fromIdMServiceClassName.equals(toIdMServiceClassName)) {
            changeMessage += "; the IdMService class is changed from "
                    + fromIdMServiceClassName
                    + " to " + toIdMServiceClassName;
        }
        LOG.debug(changeMessage);
        this.idmService = idmService;
    }

    public ClientService getClientService() {
        return clientService;
    }

    public void setClientService(ClientService clientService) {
        String changeMessage = "ODL triggered modification of ClientService";
        final String toClientServiceClassName = clientService.getClass().toString();
        final String fromClientServiceClassName = clientService.getClass().toString();
        if (!fromClientServiceClassName.equals(toClientServiceClassName)) {
            changeMessage += "; the ClientService class is changed from "
                    + fromClientServiceClassName
                    + " to " + toClientServiceClassName;
        }
        LOG.debug(changeMessage);
        this.clientService = clientService;
    }
}
