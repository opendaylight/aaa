/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.shiro.realm.util.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.aaa.impl.shiro.keystone.domain.KeystoneToken;

@RunWith(MockitoJUnitRunner.class)
public class SimpleHttpRequestTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Client client;

    @Mock
    private ClientResponse clientResponse;

    @Mock
    KeystoneToken theToken;

    @Test
    public void execute() throws Exception {
        URI uri = new URL("http://example.com").toURI();
        String path = "path";
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.put("header1", Collections.singletonList("value1"));
        headers.put("header2", Collections.singletonList("value2"));
        SimpleHttpRequest<Response> request = SimpleHttpRequest.builder(Response.class)
                .uri(uri)
                .path(path)
                .sslContext(UntrustedSSL.getSSLContext())
                .hostnameVerifier(UntrustedSSL.getHostnameVerifier())
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .method("POST")
                .entity("entity")
                .build();
        when(client.resource(uri)
                .path(path)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .method("POST", ClientResponse.class, "entity"))
            .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getHeaders()).thenReturn(headers);

        SimpleHttpRequest<Response> spiedRequest = spy(request);
        doReturn(client).when(spiedRequest).createClient(any());

        Response response = spiedRequest.execute();

        verify(spiedRequest).createClient(any());

        assertThat(response.getStatus(), is(200));
        assertThat(response.getMetadata(), is(headers));
    }

    @Test
    public void keystoneTokenGetter() throws MalformedURLException, URISyntaxException {
        URI uri = new URL("http://example.com").toURI();
        String path = "path";

        KeystoneToken.Token ksToken = new KeystoneToken.Token();
        when(theToken.getToken()).thenReturn(ksToken);

        SimpleHttpRequest<KeystoneToken> request = SimpleHttpRequest.builder(KeystoneToken.class)
                .uri(uri)
                .path(path)
                .sslContext(UntrustedSSL.getSSLContext())
                .hostnameVerifier(UntrustedSSL.getHostnameVerifier())
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .method("POST")
                .entity("entity")
                .queryParams("nocatalog", "true")
                .build();
        when(client.resource(uri)
                .path(path)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .method("POST", KeystoneToken.class, "entity"))
                .thenReturn(theToken);
        SimpleHttpRequest<KeystoneToken> spiedRequest = spy(request);
        doReturn(client).when(spiedRequest).createClient(any());
        KeystoneToken response = spiedRequest.execute();
        verify(spiedRequest).createClient(any());
        assertThat(response.getToken().getRoles().size(), is(0));
    }

}
