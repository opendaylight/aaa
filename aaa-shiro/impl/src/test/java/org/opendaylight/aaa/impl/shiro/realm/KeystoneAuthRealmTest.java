/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;


import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.impl.shiro.keystone.domain.KeystoneAuth;
import org.opendaylight.aaa.impl.shiro.realm.util.http.SimpleHttpClient;
import org.opendaylight.aaa.impl.shiro.realm.util.http.SimpleHttpRequest;
import org.opendaylight.aaa.impl.shiro.realm.util.http.UntrustedSSL;

@RunWith(MockitoJUnitRunner.class)
public class KeystoneAuthRealmTest {

    @Mock
    private ICertificateManager certificateManager;

    @Mock
    private SSLContext sslContext;

    @Mock
    private SimpleHttpRequest.Builder<Response> requestBuilder;

    @Mock
    private SimpleHttpRequest<Response> httpRequest;

    @Mock
    private SimpleHttpClient.Builder clientBuilder;

    @Mock
    private SimpleHttpClient client;

    @Mock
    private Response response;

    @Captor
    private ArgumentCaptor<KeystoneAuth> keystoneAuthArgumentCaptor;

    @Spy
    private KeystoneAuthRealm keystoneAuthRealm;

    @Before
    public void setup() throws MalformedURLException, URISyntaxException {
        final String testUrl = "http://example.com";

        when(certificateManager.getServerContext()).thenReturn(sslContext);
        when(client.requestBuilder(Response.class)).thenReturn(requestBuilder);
        when(clientBuilder.provider(JacksonJsonProvider.class)).thenReturn(clientBuilder);
        when(clientBuilder.sslContext(any())).thenReturn(clientBuilder);
        when(clientBuilder.hostnameVerifier(any())).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(client);
        when(requestBuilder.uri(new URL(testUrl).toURI())).thenReturn(requestBuilder);
        when(requestBuilder.path("v3/auth/tokens")).thenReturn(requestBuilder);
        when(requestBuilder.method(HttpMethod.POST)).thenReturn(requestBuilder);
        when(requestBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE)).thenReturn(requestBuilder);
        when(requestBuilder.entity(any())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(httpRequest);
        when(httpRequest.execute()).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());

        keystoneAuthRealm.setUrl(testUrl);
    }

    @Test
    public void doGetAuthenticationInfo() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        final AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token, client);
        verify(requestBuilder).entity(keystoneAuthArgumentCaptor.capture());
        KeystoneAuth keystoneAuth = keystoneAuthArgumentCaptor.getValue();
        assertThat(keystoneAuth.getAuth().getIdentity().getMethods(), arrayContaining("password"));
        assertThat(keystoneAuth.getAuth().getIdentity().getPassword().getUser().getName(), is("user"));
        assertThat(keystoneAuth.getAuth().getIdentity().getPassword().getUser().getPassword(), is("password"));
        assertThat(keystoneAuth.getAuth().getIdentity().getPassword().getUser().getDomain().getName(),
                is("Default"));
        assertThat(info, notNullValue());
        ODLPrincipal principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertThat(principal.getUsername(), is("user"));
        assertThat(principal.getUserId(), is("user@Default"));
        assertThat(principal.getDomain(), is("Default"));
        char[] credentials = (char[]) info.getCredentials();
        assertThat(new String(credentials), is("password"));
    }

    @Test
    public void doGetAuthenticationInfoNotAuthorized() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        when(response.getStatus()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token, client);
        assertThat(info, nullValue());
    }

    @Test
    public void doGetAuthenticationInfoCustomDefaultDomain() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        keystoneAuthRealm.setDefaultDomain("sdn");
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token, client);
        verify(requestBuilder).entity(keystoneAuthArgumentCaptor.capture());
        KeystoneAuth keystoneAuth = keystoneAuthArgumentCaptor.getValue();
        assertThat(keystoneAuth.getAuth().getIdentity().getPassword().getUser().getDomain().getName(),
                is("sdn"));
        assertThat(info, notNullValue());
        ODLPrincipal principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertThat(principal.getUserId(), is("user@sdn"));
        assertThat(principal.getDomain(), is("sdn"));
    }

    @Test
    public void doGetAuthenticationInfoCustomDomain() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user@sdn", "password");
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token, client);
        verify(requestBuilder).entity(keystoneAuthArgumentCaptor.capture());
        KeystoneAuth keystoneAuth = keystoneAuthArgumentCaptor.getValue();
        assertThat(keystoneAuth.getAuth().getIdentity().getPassword().getUser().getDomain().getName(),
                is("sdn"));
        assertThat(info, notNullValue());
        ODLPrincipal principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertThat(principal.getUserId(), is("user@sdn"));
        assertThat(principal.getDomain(), is("sdn"));
    }

    @Test(expected = AuthenticationException.class)
    public void doGetAuthenticationInfoNullSslContext() throws Exception {
        final UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        when(certificateManager.getServerContext()).thenReturn(null);
        keystoneAuthRealm.doGetAuthenticationInfo(token);
    }

    @Test(expected = AuthenticationException.class)
    public void doGetAuthenticationInfoInvalidURL() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user@sdn", "password");
        final String invalidUrl = "not_an_url";
        keystoneAuthRealm.setUrl(invalidUrl);
        keystoneAuthRealm.doGetAuthenticationInfo(token, client);
    }

    @Test(expected = AuthenticationException.class)
    public void doGetAuthenticationInfoUnknownTokenType() throws Exception {
        AuthenticationToken token = new AuthenticationToken() {
            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }
        };
        keystoneAuthRealm.doGetAuthenticationInfo(token, client);
    }

    @Test(expected = AuthenticationException.class)
    public void doGetAuthenticationInfoNullToken() throws Exception {
        keystoneAuthRealm.doGetAuthenticationInfo(null, client);
    }

    @Test
    public void getClientTrusted() {
        keystoneAuthRealm.getClient(true, certificateManager, clientBuilder);
        verify(clientBuilder).hostnameVerifier(same(HttpsURLConnection.getDefaultHostnameVerifier()));
        verify(clientBuilder).sslContext(same(sslContext));
    }

    @Test
    public void getClientUnTrusted() {
        keystoneAuthRealm.getClient(false, certificateManager, clientBuilder);
        verify(clientBuilder).hostnameVerifier(same(UntrustedSSL.getHostnameVerifier()));
        verify(clientBuilder).sslContext(same(UntrustedSSL.getSSLContext()));
    }

}
