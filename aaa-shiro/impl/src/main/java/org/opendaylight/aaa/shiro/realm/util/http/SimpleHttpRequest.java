/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm.util.http;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Basic utility to do an HTTP request. See {@link
 * SimpleHttpClient#requestBuilder(Class)} on how to obtain a request builder.
 *
 * @param <T> the return type of the request.
 */
// Suppressed so UT's can mock it using Mockito.
@SuppressWarnings("checkstyle:FinalClass")
public class SimpleHttpRequest<T> {
    private final Client client;
    private final Class<T> outputType;
    private URI uri;
    private String path;
    private String method;
    private MediaType mediaType;
    private Entity<?> entity;
    private final Map<String, String> queryParams = new HashMap<>();

    private SimpleHttpRequest(final Client client, final Class<T> outputType) {
        this.client = client;
        this.outputType = outputType;
    }

    /**
     * Executes the http request.
     *
     * @return the result of the http request.
     */
    public T execute() {
        WebTarget target = client.target(uri).path(path);

        // add the query params
        queryParams.forEach(target::queryParam);

        try {
            if (outputType == Response.class) {
                ClientResponseContext output = target.request(mediaType)
                        .method(method, entity, ClientResponseContext.class);
                return outputType.cast(clientResponseToResponse(output));
            } else {
                return target.request(mediaType).method(method, entity, outputType);
            }
        } catch (ResponseProcessingException e) {
            throw new WebApplicationException(e, clientResponseToResponse(e.getResponse()));
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
    static <T> Builder<T> builder(final Client client, final Class<T> outputType) {
        return new Builder<>(client, outputType);
    }

    private static Response clientResponseToResponse(final ClientResponseContext clientResponse) {
        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
        clientResponse.getHeaders().forEach((header, values) -> values.forEach(value -> rb.header(header, value)));
        rb.entity(clientResponse.getEntityStream());
        return rb.build();
    }

    private static Response clientResponseToResponse(final Response clientResponse) {
        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
        clientResponse.getHeaders().forEach((header, values) -> values.forEach(value -> rb.header(header, value)));
        return rb.build();
    }

    public static class Builder<T> {
        private final SimpleHttpRequest<T> request;

        Builder(final Client client, final Class<T> outputType) {
            request = new SimpleHttpRequest<>(client, outputType);
        }

        /**
         * Sets the URI the request is made to.
         *
         * @param uri the URI.
         * @return self, the request builder.
         */
        public Builder<T> uri(final URI uri) {
            request.uri = uri;
            return this;
        }

        /**
         * Sets the relative path the request is made to.
         *
         * @param path the path.
         * @return self, the request builder.
         */
        public Builder<T> path(final String path) {
            request.path = path;
            return this;
        }

        /**
         * Sets the method invoked in the request.
         *
         * @param method the method.
         * @return self, the request builder.
         */
        public Builder<T> method(final String method) {
            request.method = method;
            return this;
        }

        /**
         * Sets the media type of the request payload.
         *
         * @param mediaType the media type.
         * @return self, the request builder.
         */
        public Builder<T> mediaType(final MediaType mediaType) {
            request.mediaType = mediaType;
            return this;
        }

        /**
         * Sets the input payload of the request.
         *
         * @param input the input payload.
         * @return self, the request builder.
         */
        public Builder<T> entity(final Entity<?> input) {
            request.entity = input;
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
        public Builder<T> queryParam(final String theQueryParam, final String theParamValue) {
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
