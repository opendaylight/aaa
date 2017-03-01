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
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Basic utility to do an HTTP request. See {@link
 * SimpleHttpClient#requestBuilder(Class)} on how to obtain a request builder.
 *
 * @param <T> the return type of the request.
 */
public class SimpleHttpRequest<T> {
    private final Client client;
    private final Class<T> outputType;
    private URI uri;
    private String path;
    private String method;
    private MediaType mediaType;
    private Object entity;

    private SimpleHttpRequest(Client client, Class<T> outputType) {
        this.client = client;
        this.outputType = outputType;
    }

    /**
     * Executes the http request.
     *
     * @return the result of the http request.
     */
    public T execute() {
        WebResource webResource = client.resource(uri);
        if (outputType == Response.class) {
            ClientResponse output = webResource.path(path).type(mediaType).method(method, ClientResponse.class, entity);
            return outputType.cast(clientResponseToResponse(output));
        } else {
            return webResource.path(path).type(mediaType).method(method, outputType, entity);
        }
    }

    /**
     * Obtain a builder for SimpleHttpRequest.
     *
     * @param client the client used when executing the request.
     * @param outputType the return type of the request.
     * @param <T> the return type of the request.
     *
     * @return the builder.
     */
    static <T> Builder<T> builder(Client client, Class<T> outputType) {
        return new Builder<>(client, outputType);
    }

    private static Response clientResponseToResponse(final ClientResponse clientResponse) {
        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
        clientResponse.getHeaders().forEach((header, values) -> values.forEach(value -> rb.header(header, value)));
        rb.entity(clientResponse.getEntityInputStream());
        return rb.build();
    }

    public static class Builder<T> {
        private SimpleHttpRequest<T> request;

        Builder(Client client, Class<T> outputType) {
            request = new SimpleHttpRequest<>(client, outputType);
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
         * Build the request.
         *
         * @return the request.
         */
        public SimpleHttpRequest<T> build() {
            return request;
        }
    }

}
