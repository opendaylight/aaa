/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.shiro.realm.util.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SimpleHttpRequest {
    private URI uri;
    private String path;
    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;
    private String method;
    private MediaType mediaType;
    private Object entity;
    private Set<Class<?>> providers = new HashSet<>();
    private Class<?> outputType;

    private SimpleHttpRequest() {}

    public Response execute() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().addAll(providers);
        clientConfig.getProperties().put(
                HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(hostnameVerifier, sslContext));
        Client client = Client.create(clientConfig);
        WebResource webResource = client.resource(uri);
        ClientResponse clientResponse;
        clientResponse = webResource.path(path).type(mediaType).method(method, ClientResponse.class, entity);
        return clientResponseToResponse(clientResponse, outputType);
    }

    private static <T> Response clientResponseToResponse(final ClientResponse clientResponse, Class<T> theTypeObject) {
        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
        clientResponse.getHeaders().forEach(rb::header);
        rb.entity(clientResponse.getEntity(theTypeObject));
        return rb.build();
    }

    public static <T> Builder builder(Class<T> theOutputType) {
        return new Builder(theOutputType);
    }

    public static final class Builder {
        private SimpleHttpRequest request;
        private <T> Builder(Class<T> theOutputType) {
            this.request = new SimpleHttpRequest();
            this.request.outputType = theOutputType;
        }

        public Builder uri(URI uri) {
            request.uri = uri;
            return this;
        }

        public Builder path(String path) {
            request.path = path;
            return this;
        }

        public Builder sslContext(SSLContext sslContext) {
            request.sslContext = sslContext;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            request.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder method(String method) {
            request.method = method;
            return this;
        }

        public Builder mediaType(MediaType mediaType) {
            request.mediaType = mediaType;
            return this;
        }

        public Builder entity(Object input) {
            request.entity = input;
            return this;
        }

        public Builder provider(Class<?> provider) {
            request.providers.add(provider);
            return this;
        }

        public SimpleHttpRequest build() {
            return request;
        }
    }

}
