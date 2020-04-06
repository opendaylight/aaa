/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm.util.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.opendaylight.aaa.provider.GsonProvider;
import org.opendaylight.aaa.shiro.keystone.domain.KeystoneToken;
import org.opendaylight.aaa.shiro.realm.util.http.SimpleHttpRequest.Builder;

public class SimpleHttpRequestTest extends JerseyTest {
    private static final KeystoneToken.Token.Role ROLE = new KeystoneToken.Token.Role("name", "id");
    private static final KeystoneToken KEYSTONE_TOKEN = new KeystoneToken(
            new KeystoneToken.Token(ImmutableList.of(ROLE)));

    @Path("keystone")
    public static class KeystoneHandler {
        @POST
        public KeystoneToken keystoneToken(final String input) {
            return KEYSTONE_TOKEN;
        }
    }

    @Path("simple")
    public static class SimpleResponseHandler {
        @POST
        public Response handle(final String input) {
            return Response.ok("hello").build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(KeystoneHandler.class, SimpleResponseHandler.class, GsonProvider.class);
    }

    @Test
    public void testSimpleResponse() {
        final Builder<Response> builder = SimpleHttpClient.clientBuilder()
                .hostnameVerifier(UntrustedSSL.getHostnameVerifier())
                .sslContext(UntrustedSSL.getSSLContext()).provider(GsonProvider.class).build()
                .requestBuilder(Response.class);

        SimpleHttpRequest<Response> request = builder.uri(getBaseUri()).path("simple")
                .mediaType(MediaType.TEXT_PLAIN_TYPE).method(HttpMethod.POST).entity("input").build();

        Response response = request.execute();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void testKeystoneTokenGetter() {
        final Builder<KeystoneToken> builder = SimpleHttpClient.clientBuilder()
                .hostnameVerifier(UntrustedSSL.getHostnameVerifier())
                .sslContext(UntrustedSSL.getSSLContext()).provider(GsonProvider.class).build()
                .requestBuilder(KeystoneToken.class);

        SimpleHttpRequest<KeystoneToken> request = builder.uri(getBaseUri()).path("keystone")
                .mediaType(MediaType.APPLICATION_JSON_TYPE).method(HttpMethod.POST).entity("input").build();

        KeystoneToken response = request.execute();
        assertThat(response.getToken().getRoles().size(), is(1));
        assertThat(response.getToken().getRoles().get(0).getName(), is(ROLE.getName()));
        assertThat(response.getToken().getRoles().get(0).getId(), is(ROLE.getId()));
    }
}
