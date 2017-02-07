/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import static org.opendaylight.aaa.impl.shiro.principal.ODLPrincipalImpl.createODLPrincipal;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeystoneRealm is a Shiro Realm that authenticates users from Openstack Keystone.
 *
 */
public class KeystoneRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneRealm.class);

    private static final String USERNAME_DOMAIN_SEPARATOR = "@";
    /**
     * The message that is displayed if credentials are missing or malformed
     */
    private static final String FATAL_ERROR_DECODING_CREDENTIALS = "{\"error\":\"Unable to decode credentials\"}";

    /**
     * The message that is displayed if non-Basic Auth is attempted
     */
    private static final String FATAL_ERROR_BASIC_AUTH_ONLY = "{\"error\":\"Only basic authentication is supported by TokenAuthRealm\"}";

    /**
     * The message that is displayed if URL is invalid
     */
    private static final String FATAL_ERROR_INVALID_URL = "{\"error\":\"Invalid URL to Kesytone server\"}";

    /**
     * The purposefully generic message displayed if <code>TokenAuth</code> is
     * unable to validate the given credentials
     */
    private static final String UNABLE_TO_AUTHENTICATE = "{\"error\":\"Could not authenticate\"}";

    private static final String KEYSTONE_PAYLOAD = "{\"auth\":{\"identity\":{\"methods\":[\"password\"]," +
            "\"password\":{\"user\":{\"name\":\"%s\",\"domain\":{\"id\":\"%s\"},\"password\":\"%s\"}}}}}";

    private URI keystoneServerURI;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {

        final String username;
        final String password;
        String domain = HttpBasicAuth.DEFAULT_DOMAIN;

        try {
            final String qualifiedUser = extractUsername(authenticationToken);
            if (qualifiedUser.contains(USERNAME_DOMAIN_SEPARATOR)) {
                final String[] qualifiedUserArray = qualifiedUser.split(USERNAME_DOMAIN_SEPARATOR);
                if (qualifiedUser.length() < 2) {
                    throw new AuthenticationException(FATAL_ERROR_DECODING_CREDENTIALS);
                }
                username = qualifiedUserArray[0];
                domain = qualifiedUserArray[1];
            } else {
                username = qualifiedUser;
            }
            password = extractPassword(authenticationToken);

        } catch (NullPointerException e) {
            throw new AuthenticationException(FATAL_ERROR_DECODING_CREDENTIALS, e);
        } catch (ClassCastException e) {
            throw new AuthenticationException(FATAL_ERROR_BASIC_AUTH_ONLY, e);
        }

        final ODLPrincipal principal = keystoneAuthenticate(username,password,domain);
        return new SimpleAuthenticationInfo(principal, password.toCharArray(), getName());
    }

    private ODLPrincipal keystoneAuthenticate(final String username, final String password, final String domain) {

        if (Objects.isNull(keystoneServerURI)) {
            throw new AuthenticationException(FATAL_ERROR_INVALID_URL);
        }

        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1");
            System.setProperty("https.protocols", "TLSv1");
            sslContext.init(null, getTrustManager(), new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new AuthenticationException(UNABLE_TO_AUTHENTICATE, e);
        }

        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(
                HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(getInsecureHostnameVerifier(), sslContext));
        final Client client = Client.create(config);
        final WebResource webResource = client.resource(keystoneServerURI);
        final String input = buildPayload(username, password, domain);
        LOG.trace("Sending authentication request to keystone with payload {}", input);

        final String output;
        final ClientResponse response;
        try {
            response = webResource.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, input);
            output = response.getEntity(String.class);
        } catch (UniformInterfaceException | ClientHandlerException e) {
            LOG.debug("Authentication attempt unsuccessful", e);
            throw new AuthenticationException(UNABLE_TO_AUTHENTICATE, e);
        }

        if (response.getClientResponseStatus() != ClientResponse.Status.CREATED) {
            LOG.debug("Authentication attempt unsuccessful, response: {}", response);
            throw new AuthenticationException(UNABLE_TO_AUTHENTICATE);
        }

        LOG.debug("Keystone authentication successful: {}", output);
        final String userId = username + USERNAME_DOMAIN_SEPARATOR + domain;
        return createODLPrincipal(username, domain, userId, null);
    }

    /**
     * Injected from <code>shiro.ini</code>.
     *
     * @param keystoneServerURL specified in <code>shiro.ini</code>
     */
    public void setKeystoneServerURL(final String keystoneServerURL) {
        try {
            URL url = new URL(keystoneServerURL);
            if (!"https".equals(url.getProtocol())) {
                LOG.error("The keystone server URL is not https. Only https supported");
                return;
            }
            keystoneServerURI = url.toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            LOG.error("The keystone server URL could not be parsed", e);
        }
    }

    private static String extractUsername(final AuthenticationToken authenticationToken) {
        return (String) authenticationToken.getPrincipal();
    }

    private static String extractPassword(final AuthenticationToken authenticationToken) {

        final UsernamePasswordToken upt = (UsernamePasswordToken) authenticationToken;
        return new String(upt.getPassword());
    }

    private static String buildPayload(final String username, final String password, final String domain) {
        return String.format(KEYSTONE_PAYLOAD, username, domain, password);
    }

    private TrustManager[] getTrustManager() {
        return new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }
                }
        };
    }

    private HostnameVerifier getInsecureHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
    }

}
