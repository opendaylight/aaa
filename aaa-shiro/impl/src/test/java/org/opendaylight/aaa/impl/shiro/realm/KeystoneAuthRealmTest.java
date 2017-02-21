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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Assert;
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
import org.opendaylight.aaa.impl.shiro.keystone.domain.KeystoneToken;
import org.opendaylight.aaa.impl.shiro.realm.util.http.SimpleHttpRequest;
import org.opendaylight.aaa.impl.shiro.realm.util.http.UntrustedSSL;

@RunWith(MockitoJUnitRunner.class)
public class KeystoneAuthRealmTest {

    @Mock
    private SimpleHttpRequest.Builder<KeystoneToken> builder;

    @Mock
    private ICertificateManager certificateManager;

    @Mock
    private SSLContext sslContext;

    @Mock
    private SimpleHttpRequest<KeystoneToken> httpRequest;

    @Mock
    private KeystoneToken response;

    @Captor
    private ArgumentCaptor<KeystoneAuth> keystoneAuthArgumentCaptor;

    @Spy
    private KeystoneAuthRealm keystoneAuthRealm;

    private KeystoneToken.Token ksToken;

    @Before
    public void setup() throws MalformedURLException, URISyntaxException {
        final String testUrl = "http://example.com";
        // a token for a user without roles
        ksToken = new KeystoneToken.Token();

        when(certificateManager.getServerContext()).thenReturn(sslContext);
        when(builder.uri(new URL(testUrl).toURI())).thenReturn(builder);
        when(builder.path("v3/auth/tokens")).thenReturn(builder);
        when(builder.sslContext(same(sslContext))).thenReturn(builder);
        when(builder.hostnameVerifier(same(HttpsURLConnection.getDefaultHostnameVerifier()))).thenReturn(builder);
        when(builder.method(HttpMethod.POST)).thenReturn(builder);
        when(builder.mediaType(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder);
        when(builder.provider(JacksonJsonProvider.class)).thenReturn(builder);
        when(builder.entity(any())).thenReturn(builder);
        when(builder.build()).thenReturn(httpRequest);
        when(builder.queryParams(any(String.class), any(String.class))).thenReturn(builder);
        when(httpRequest.execute()).thenReturn(response);
        when(response.getToken()).thenReturn(ksToken);

        keystoneAuthRealm.setUrl(testUrl);
        doReturn(certificateManager).when(keystoneAuthRealm).getCertificateManager();
        doReturn(builder).when(keystoneAuthRealm).getHttpRequestBuilder(KeystoneToken.class);
    }

    @Test
    public void doGetAuthenticationInfo() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        final AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token);
        verify(builder).entity(keystoneAuthArgumentCaptor.capture());
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
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token);
        Assert.assertFalse(info.getPrincipals().isEmpty());
        Assert.assertTrue(info.getPrincipals().asSet().size() == 1);
        ODLPrincipal authData = (ODLPrincipal) info.getPrincipals().asList().get(0);
        Assert.assertEquals(0, authData.getRoles().size());
    }

    @Test
    public void doGetAuthenticationInfoAuthorized() throws Exception {
        // update the response mock
        List<KeystoneToken.Token.Role> theRoles = Stream.of("admin", "ninja", "death-star-commander")
                .map(roleName -> new KeystoneToken.Token.Role(roleName, roleName)).collect(Collectors.toList());
        ksToken = new KeystoneToken.Token(theRoles);
        when(response.getToken()).thenReturn(ksToken);

        UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token);
        Assert.assertFalse(info.getPrincipals().isEmpty());
        Assert.assertTrue(info.getPrincipals().asSet().size() == 1);
        ODLPrincipal authData = (ODLPrincipal) info.getPrincipals().asList().get(0);
        Assert.assertEquals(3, authData.getRoles().size());
    }

    @Test
    public void doGetAuthenticationInfoNoSslVerification() throws Exception {
        final UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        keystoneAuthRealm.setSslVerification(false);
        when(builder.sslContext(same(UntrustedSSL.getSSLContext()))).thenReturn(builder);
        when(builder.hostnameVerifier(same(UntrustedSSL.getHostnameVerifier()))).thenReturn(builder);
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token);
        assertThat(info, notNullValue());
    }

    @Test
    public void doGetAuthenticationInfoCustomDefaultDomain() throws Exception {
        UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
        keystoneAuthRealm.setDefaultDomain("sdn");
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token);
        verify(builder).entity(keystoneAuthArgumentCaptor.capture());
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
        AuthenticationInfo info = keystoneAuthRealm.doGetAuthenticationInfo(token);
        verify(builder).entity(keystoneAuthArgumentCaptor.capture());
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
        keystoneAuthRealm.doGetAuthenticationInfo(token);
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
        keystoneAuthRealm.doGetAuthenticationInfo(token);
    }

    @Test(expected = AuthenticationException.class)
    public void doGetAuthenticationInfoNullToken() throws Exception {
        keystoneAuthRealm.doGetAuthenticationInfo(null);
    }

}
