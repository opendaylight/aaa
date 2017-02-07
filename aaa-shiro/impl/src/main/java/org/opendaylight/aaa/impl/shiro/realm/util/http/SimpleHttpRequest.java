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

public class SimpleHttpRequest<T> {
    private URI uri;
    private String path;
    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;
    private String method;
    private MediaType mediaType;
    private Object entity;
    private Set<Class<?>> providers = new HashSet<>();
    private Class<T> outputType;

    private SimpleHttpRequest() {}

    public T execute() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().addAll(providers);
        clientConfig.getProperties().put(
                HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(hostnameVerifier, sslContext));
        Client client = Client.create(clientConfig);
        WebResource webResource = client.resource(uri);
        if (outputType == Response.class) {
            ClientResponse output = webResource.path(path).type(mediaType).method(method, ClientResponse.class, entity);
            return outputType.cast(clientResponseToResponse(output));
        } else {
            return webResource.path(path).type(mediaType).method(method, outputType, entity);
        }
    }

    private static Response clientResponseToResponse(final ClientResponse clientResponse) {
        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
        clientResponse.getHeaders().forEach(rb::header);
        rb.entity(clientResponse.getEntityInputStream());
        return rb.build();
    }

    public static <T> Builder<T> builder(Class<T> outputType) {
        return new Builder<>(outputType);
    }

    public static final class Builder<T> {
        private SimpleHttpRequest<T> request;
        private Builder(Class<T> outputType) {
            request = new SimpleHttpRequest<>();
            request.outputType = outputType;
        }

        public Builder<T> uri(URI uri) {
            request.uri = uri;
            return this;
        }

        public Builder<T> path(String path) {
            request.path = path;
            return this;
        }

        public Builder<T> sslContext(SSLContext sslContext) {
            request.sslContext = sslContext;
            return this;
        }

        public Builder<T> hostnameVerifier(HostnameVerifier hostnameVerifier) {
            request.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder<T> method(String method) {
            request.method = method;
            return this;
        }

        public Builder<T> mediaType(MediaType mediaType) {
            request.mediaType = mediaType;
            return this;
        }

        public Builder<T> entity(Object input) {
            request.entity = input;
            return this;
        }

        public Builder<T> provider(Class<?> provider) {
            request.providers.add(provider);
            return this;
        }

        public SimpleHttpRequest<T> build() {
            return request;
        }
    }

}
