/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm.util.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.aaa.shiro.keystone.domain.KeystoneToken;

@RunWith(MockitoJUnitRunner.class)
public class SimpleHttpRequestTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Client client;

    @Mock
    private ClientResponseContext clientResponse;

    @Mock
    KeystoneToken theToken;

    @Test
    public void execute() throws Exception {
        URI uri = new URL("http://example.com").toURI();
        String path = "path";
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.put("header1", Collections.singletonList("value1"));
        headers.put("header2", Collections.singletonList("value2"));
        SimpleHttpRequest<Response> request = SimpleHttpRequest.builder(client, Response.class)
                .uri(uri)
                .path(path)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .method("POST")
                .entity(Entity.text("entity"))
                .build();
        when(client.target(uri)
                .path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .method("POST", Entity.text("entity"), ClientResponseContext.class))
            .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getHeaders()).thenReturn(headers);

        SimpleHttpRequest<Response> spiedRequest = Mockito.spy(request);

        Response response = spiedRequest.execute();

        assertThat(response.getStatus(), is(200));
        assertThat(response.getMetadata(), is(headers));
    }

    @Test
    public void keystoneTokenGetter() throws MalformedURLException, URISyntaxException {
        URI uri = new URL("http://example.com").toURI();
        String path = "path";

        KeystoneToken.Token ksToken = new KeystoneToken.Token();
        when(theToken.getToken()).thenReturn(ksToken);

        SimpleHttpRequest<KeystoneToken> request = SimpleHttpRequest.builder(client, KeystoneToken.class)
                .uri(uri)
                .path(path)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .method("POST")
                .entity(Entity.text("entity"))
                .queryParam("nocatalog", "true")
                .build();
        when(client.target(uri)
                .path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .method("POST", Entity.text("entity"), KeystoneToken.class))
                .thenReturn(theToken);
        SimpleHttpRequest<KeystoneToken> spiedRequest = Mockito.spy(request);
        KeystoneToken response = spiedRequest.execute();
        assertThat(response.getToken().getRoles().size(), is(0));
    }

}
