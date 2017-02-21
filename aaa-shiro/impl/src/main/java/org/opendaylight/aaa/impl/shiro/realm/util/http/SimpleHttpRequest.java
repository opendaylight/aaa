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
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Basic utility to do an HTTP request.
 *
 * @param <T> the return type of the request.
 */
public class SimpleHttpRequest<T> {
    private URI uri;
    private String path;
    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;
    private String method;
    private MediaType mediaType;
    private Object entity;
    private Set<Class<?>> providers = new HashSet<>();
    private Map<String, String> queryParams = new HashMap<>();
    private Class<T> outputType;

    private SimpleHttpRequest() {}

    /**
     * Executes the http request.
     *
     * @return the result of the http request.
     */
    public T execute() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().addAll(providers);
        clientConfig.getProperties().put(
                HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(hostnameVerifier, sslContext));
        Client client = createClient(clientConfig);
        WebResource webResource = client.resource(uri)
                .path(path);

        // add the query params
        queryParams.entrySet().forEach(queryParamEntry ->
                webResource.queryParam(queryParamEntry.getKey(), queryParamEntry.getValue()));
        try {
            if (outputType == Response.class) {
                ClientResponse output = webResource
                        .type(mediaType)
                        .method(method, ClientResponse.class, entity);
                return outputType.cast(clientResponseToResponse(output));
            } else {
                return webResource
                        .type(mediaType)
                        .method(method, outputType, entity);
            }
        } catch (UniformInterfaceException theException) {
            throw new WebApplicationException(theException,
                    clientResponseToResponse(theException.getResponse()));
        }
    }

    /**
     * Used to obtain the client with a given config.
     *
     * @param config the config.
     * @return the client.
     */
    protected Client createClient(ClientConfig config) {
        return Client.create(config);
    }

    /**
     * Obtain a builder for SimpleHttpRequest.
     *
     * @param outputType the return type of the request.
     * @param <T> the return type of the request.
     *
     * @return the builder.
     */
    public static <T> Builder<T> builder(Class<T> outputType) {
        return new Builder<>(outputType);
    }

    private static Response clientResponseToResponse(final ClientResponse clientResponse) {
        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
        clientResponse.getHeaders().forEach((header, values) -> values.forEach(value -> rb.header(header, value)));
        rb.entity(clientResponse.getEntityInputStream());
        return rb.build();
    }

    public static class Builder<T> {
        private SimpleHttpRequest<T> request;

        private Builder(Class<T> outputType) {
            request = new SimpleHttpRequest<>();
            request.outputType = outputType;
        }

        /**
         * Sets the URI the request is made to.
         *
         * @param uri the URI.
         * @return self, the request builder.
         */
        public Builder<T> uri(URI uri) {
            request.uri = uri;
            return this;
        }

        /**
         * Sets the relative path the request is made to.
         *
         * @param path the path.
         * @return self, the request builder.
         */
        public Builder<T> path(String path) {
            request.path = path;
            return this;
        }

        /**
         * Sets the SSLContext to be used for SSL requests.
         *
         * @param sslContext the SSLContext.
         * @return self, the request builder.
         */
        public Builder<T> sslContext(SSLContext sslContext) {
            request.sslContext = sslContext;
            return this;
        }

        /**
         * Sets the hostname verifier the request is made with.
         *
         * @param hostnameVerifier the hortname verifier.
         * @return self, the request builder.
         */
        public Builder<T> hostnameVerifier(HostnameVerifier hostnameVerifier) {
            request.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * Sets the method invoked in the request.
         *
         * @param method the method.
         * @return self, the request builder.
         */
        public Builder<T> method(String method) {
            request.method = method;
            return this;
        }

        /**
         * Sets the media type of the request payload.
         *
         * @param mediaType the media type.
         * @return self, the request builder.
         */
        public Builder<T> mediaType(MediaType mediaType) {
            request.mediaType = mediaType;
            return this;
        }

        /**
         * Sets the input payload of the request.
         *
         * @param input the input payload.
         * @return self, the request builder.
         */
        public Builder<T> entity(Object input) {
            request.entity = input;
            return this;
        }

        /**
         * Sets a JAX-RS provider to use for this request. Can be called
         * multiple times to add multiple providers.
         *
         * @param provider the provider.
         * @return self, the request builder.
         */
        public Builder<T> provider(Class<?> provider) {
            request.providers.add(provider);
            return this;
        }

        /**
         * Add query parameters to the request. Can be called multiple times,
         * to add multiple query parameters. Values are overwritten when assigned
         * the same keys.
         *
         * @param theQueryParam the parameter name
         * @param theParamValue the parameter value
         * @return  self, the request builder
         */
        public Builder<T> queryParams(String theQueryParam, String theParamValue) {
            request.queryParams.put(theQueryParam, theParamValue);
            return this;
        }

        /**
         * Build the request.
         *
         * @return the request.
         */
        public SimpleHttpRequest<T> build() {
            return request;
        }
    }

}
