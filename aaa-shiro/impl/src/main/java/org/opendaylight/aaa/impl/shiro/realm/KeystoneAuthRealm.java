/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import static org.opendaylight.aaa.impl.shiro.principal.ODLPrincipalImpl.createODLPrincipal;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.basic.HttpBasicAuth;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.impl.AAAShiroProvider;
import org.opendaylight.aaa.impl.shiro.keystone.domain.KeystoneAuth;
import org.opendaylight.aaa.impl.shiro.realm.util.http.SimpleHttpRequest;
import org.opendaylight.aaa.impl.shiro.realm.util.http.UntrustedSSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeystoneAuthRealm is a Shiro Realm that authenticates users from Openstack Keystone.
 */
public class KeystoneAuthRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneAuthRealm.class);

    private static final String DEFAULT_DOMAIN = "Default";
    private static final String USERNAME_DOMAIN_SEPARATOR = "@";
    private static final String FATAL_ERROR_BASIC_AUTH_ONLY = "{\"error\":\"Only basic authentication is supported\"}";
    private static final String FATAL_ERROR_INVALID_URL = "{\"error\":\"Invalid URL to Kesytone server\"}";
    private static final String UNABLE_TO_AUTHENTICATE = "{\"error\":\"Could not authenticate\"}";
    private static final String AUTH_PATH = "v3/auth/tokens";

    private URI keystoneServerURI = null;
    private boolean sslVerification = true;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) {

        if (!(authenticationToken instanceof UsernamePasswordToken)) {
            LOG.error("Only basic authentication is supported");
            throw new AuthenticationException(FATAL_ERROR_BASIC_AUTH_ONLY);
        }

        if (Objects.isNull(keystoneServerURI)) {
            LOG.error("Invalid URL to Keystone server");
            throw new AuthenticationException(FATAL_ERROR_INVALID_URL);
        }

        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        final String qualifiedUser = usernamePasswordToken.getUsername();
        final String password = new String(usernamePasswordToken.getPassword());
        final String[] qualifiedUserArray = qualifiedUser.split(USERNAME_DOMAIN_SEPARATOR, 2);
        final String username = qualifiedUserArray.length > 0 ? qualifiedUserArray[0] : qualifiedUser;
        final String domain = qualifiedUserArray.length > 1 ? qualifiedUserArray[1] : DEFAULT_DOMAIN;
        final SSLContext sslContext = sslVerification ? getSecureSSLContext() : UntrustedSSL.getSSLContext();
        final HostnameVerifier hostnameVerifier = sslVerification ?
                HttpsURLConnection.getDefaultHostnameVerifier() : UntrustedSSL.getHostnameVerifier();

        final KeystoneAuth keystoneAuth = new KeystoneAuth(username, password, domain);
        SimpleHttpRequest<Response> httpRequest = SimpleHttpRequest.builder(Response.class)
                .uri(keystoneServerURI)
                .path(AUTH_PATH)
                .sslContext(sslContext)
                .hostnameVerifier(hostnameVerifier)
                .method(HttpMethod.POST)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .provider(JacksonJsonProvider.class)
                .entity(keystoneAuth)
                .build();

        Response response;
        try {
            response = httpRequest.execute();
        } catch (Exception e) {
            LOG.debug("Authentication attempt unsuccessful", e);
            throw new AuthenticationException(UNABLE_TO_AUTHENTICATE, e);
        }

        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            LOG.debug("No account could be associated with the specified token, response: {}", response);
            return null;
        }

        final String userId = username + USERNAME_DOMAIN_SEPARATOR + domain;
        final ODLPrincipal odlPrincipal = createODLPrincipal(username, domain, userId, null);
        return new SimpleAuthenticationInfo(odlPrincipal, password.toCharArray(), getName());
    }

    private SSLContext getSecureSSLContext() {
        final ICertificateManager certificateManager = AAAShiroProvider.getInstance().getCertificateManager();
        final SSLContext sslContext = certificateManager.getServerContext();
        if (Objects.isNull(sslContext)) {
            LOG.error("Could not get a valid SSL context from certificate manager");
            throw new AuthenticationException(UNABLE_TO_AUTHENTICATE);
        }
        return sslContext;
    }

    /**
     * Injected from <code>shiro.ini</code>.
     *
     * @param keystoneServerURL specified in <code>shiro.ini</code>
     */
    public void setKeystoneServerURL(final String keystoneServerURL) {
        try {
            keystoneServerURI = new URL(keystoneServerURL).toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            LOG.error("The keystone server URL {} could not be correctly parsed", keystoneServerURL, e);
        }
    }

    public void setSslVerification(final boolean sslVerification) {
        this.sslVerification = sslVerification;
    }

}
