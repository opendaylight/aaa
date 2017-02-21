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
import java.util.Set;
import java.util.stream.Collectors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.impl.AAAShiroProvider;
import org.opendaylight.aaa.impl.shiro.keystone.domain.KeystoneAuth;
import org.opendaylight.aaa.impl.shiro.keystone.domain.KeystoneToken;
import org.opendaylight.aaa.impl.shiro.realm.util.http.SimpleHttpRequest;
import org.opendaylight.aaa.impl.shiro.realm.util.http.UntrustedSSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeystoneAuthRealm is a Shiro Realm that authenticates users from OpenStack Keystone.
 */
public class KeystoneAuthRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneAuthRealm.class);

    private static final String NO_CATALOG_OPTION = "nocatalog";
    private static final String DEFAULT_KEYSTONE_DOMAIN = "Default";
    private static final String USERNAME_DOMAIN_SEPARATOR = "@";
    private static final String FATAL_ERROR_BASIC_AUTH_ONLY = "{\"error\":\"Only basic authentication is supported\"}";
    private static final String FATAL_ERROR_INVALID_URL = "{\"error\":\"Invalid URL to Keystone server\"}";
    private static final String UNABLE_TO_AUTHENTICATE = "{\"error\":\"Could not authenticate\"}";
    private static final String AUTH_PATH = "v3/auth/tokens";

    private volatile URI serverUri = null;
    private volatile boolean sslVerification = true;
    private volatile String defaultDomain = DEFAULT_KEYSTONE_DOMAIN;

    public KeystoneAuthRealm() {
        setName("KeystoneAuthRealm");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final Object primaryPrincipal = getAvailablePrincipal(principalCollection);
        final ODLPrincipal odlPrincipal;
        try {
            odlPrincipal = (ODLPrincipal) primaryPrincipal;
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        } catch (ClassCastException e) {
            LOG.error("Couldn't decode authorization request", e);
        }
        return new SimpleAuthorizationInfo();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) {

        final URI theServerUri = getServerUri();
        final boolean hasSslVerification = getSslVerification();
        final String theDefaultDomain = getDefaultDomain();

        if (!(authenticationToken instanceof UsernamePasswordToken)) {
            LOG.error("Only basic authentication is supported");
            throw new AuthenticationException(FATAL_ERROR_BASIC_AUTH_ONLY);
        }

        if (Objects.isNull(theServerUri)) {
            LOG.error("Invalid URL to Keystone server");
            throw new AuthenticationException(FATAL_ERROR_INVALID_URL);
        }

        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        final String qualifiedUser = usernamePasswordToken.getUsername();
        final String password = new String(usernamePasswordToken.getPassword());
        final String[] qualifiedUserArray = qualifiedUser.split(USERNAME_DOMAIN_SEPARATOR, 2);
        final String username = qualifiedUserArray.length > 0 ? qualifiedUserArray[0] : qualifiedUser;
        final String domain = qualifiedUserArray.length > 1 ? qualifiedUserArray[1] : theDefaultDomain;

        final SSLContext sslContext;
        final HostnameVerifier hostnameVerifier;
        if (hasSslVerification) {
            sslContext = getSecureSSLContext();
            hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        } else {
            sslContext = UntrustedSSL.getSSLContext();
            hostnameVerifier = UntrustedSSL.getHostnameVerifier();
        }

        final KeystoneAuth keystoneAuth = new KeystoneAuth(username, password, domain);
        final SimpleHttpRequest<KeystoneToken> httpRequest = getHttpRequestBuilder(KeystoneToken.class)
                .uri(theServerUri)
                .path(AUTH_PATH)
                .sslContext(sslContext)
                .hostnameVerifier(hostnameVerifier)
                .method(HttpMethod.POST)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .provider(JacksonJsonProvider.class)
                .entity(keystoneAuth)
                .queryParams(NO_CATALOG_OPTION,"")
                .build();

        KeystoneToken theToken;
        try {
            theToken = httpRequest.execute();
        } catch (WebApplicationException e) {
            LOG.debug("Unable to authenticate - Keystone result code: {}",
                    e.getResponse().getStatus(),
                    e);
            return null;
        }

        final Set<String> theRoles = theToken.getToken().getRoles()
                .stream()
                .map(KeystoneToken.Token.Role::getName)
                .collect(Collectors.toSet());

        final String userId = username + USERNAME_DOMAIN_SEPARATOR + domain;
        final ODLPrincipal odlPrincipal = createODLPrincipal(username, domain, userId, theRoles);
        return new SimpleAuthenticationInfo(odlPrincipal, password.toCharArray(), getName());
    }

    private SSLContext getSecureSSLContext() {
        final ICertificateManager certificateManager = getCertificateManager();
        final SSLContext sslContext = certificateManager.getServerContext();
        if (Objects.isNull(sslContext)) {
            LOG.error("Could not get a valid SSL context from certificate manager");
            throw new AuthenticationException(UNABLE_TO_AUTHENTICATE);
        }
        return sslContext;
    }

    /**
     * Used to obtain the certificate that will provide an SSL context.
     *
     * @return the certificate manager.
     */
    protected ICertificateManager getCertificateManager() {
        return AAAShiroProvider.getInstance().getCertificateManager();
    }

    /**
     * Used to obtain an http request builder.
     *
     * @param outputType the output type of the request.
     * @param <T> the output type of the request.
     * @return the request builder.
     */
    protected <T> SimpleHttpRequest.Builder<T> getHttpRequestBuilder(Class<T> outputType) {
        return SimpleHttpRequest.builder(outputType);
    }

    /**
     * The URI of the Keystone server.
     *
     * @return the URI.
     */
    public URI getServerUri() {
        return serverUri;
    }

    /**
     * Whether SSL verification is performed or untrusted access is allowed.
     *
     * @return the SSL verification flag.
     */
    public boolean getSslVerification() {
        return sslVerification;
    }

    /**
     * Default domain to use when no domain is provided within the user
     * credentials.
     *
     * @return the default domain.
     */
    public String getDefaultDomain() {
        return defaultDomain;
    }

    /**
     * The URL of the Keystone server. Injected from
     * <code>shiro.ini</code>.
     *
     * @param url the URL specified in <code>shiro.ini</code>.
     */
    public void setUrl(final String url) {
        try {
            serverUri = new URL(url).toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            LOG.error("The keystone server URL {} could not be correctly parsed", url, e);
            serverUri = null;
        }
    }

    /**
     * Whether SSL verification is performed or untrusted access is allowed.
     * Injected from <code>shiro.ini</code>.
     *
     * @param sslVerification specified in <code>shiro.ini</code>
     */
    public void setSslVerification(final boolean sslVerification) {
        this.sslVerification = sslVerification;
    }

    /**
     * Default domain to use when no domain is provided within the user
     * credentials. Injected from <code>shiro.ini</code>.
     *
     * @param defaultDomain specified in <code>shiro.ini</code>
     */
    public void setDefaultDomain(final String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }

}
