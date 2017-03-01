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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
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

@RunWith(MockitoJUnitRunner.class)
public class SimpleHttpRequestTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Client client;

    @Mock
    private ClientResponse clientResponse;

    @Test
    public void execute() throws Exception {
        URI uri = new URL("http://example.com").toURI();
        String path = "path";
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.put("header1", Collections.singletonList("value1"));
        headers.put("header2", Collections.singletonList("value2"));
        SimpleHttpRequest<Response> request = SimpleHttpRequest.builder(client, Response.class)
                .uri(uri)
                .path(path)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .method("POST")
                .entity("entity")
                .build();
        when(client
                .resource(uri)
                .path(path)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .method("POST", ClientResponse.class, "entity"))
            .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getHeaders()).thenReturn(headers);

        SimpleHttpRequest<Response> spiedRequest = spy(request);

        Response response = spiedRequest.execute();

        assertThat(response.getStatus(), is(200));
        assertThat(response.getMetadata(), is(headers));
    }

}
