/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm.util.http;

import static java.util.Objects.requireNonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * An utility that represents an HTTP client that allows to make HTTP requests.
 */
// Non-final for mocking
public class SimpleHttpClient {
    private final Client client;

    SimpleHttpClient(final Client client) {
        this.client = client;
    }

    /**
     * Obtain a builder for {@code SimpleHttpClient}.
     *
     * @return the {@link Builder}.
     */
    public static Builder clientBuilder() {
        return clientBuilder(ClientBuilder.newBuilder());
    }

    /**
     * Obtain a builder for {@code SimpleHttpClient}.
     *
     * @param clientBuilder a {@link ClientBuilder}
     * @return the {@link Builder}.
     */
    public static Builder clientBuilder(final ClientBuilder clientBuilder) {
        return new Builder(clientBuilder);
    }

    /**
     * Obtain a builder for {@link SimpleHttpRequest}.
     *
     * @param outputType the return type of the request.
     * @param <T> the return type of the request.
     * @return the request builder.
     */
    public <T> SimpleHttpRequest.Builder<T> requestBuilder(final Class<T> outputType) {
        return new SimpleHttpRequest.Builder<>(client, outputType);
    }

    // Non-final for mocking
    public static class Builder {
        private final ClientBuilder clientBuilder;

        Builder(final ClientBuilder clientBuilder) {
            this.clientBuilder = requireNonNull(clientBuilder);
        }

        /**
         * Sets the SSLContext to be used for SSL requests.
         *
         * @param context the SSLContext.
         * @return self, the client builder.
         */
        public Builder sslContext(final SSLContext context) {
            clientBuilder.sslContext(context);
            return this;
        }

        /**
         * Sets the hostname verifier the request is made with.
         *
         * @param verifier the hostname verifier.
         * @return self, the client builder.
         */
        public Builder hostnameVerifier(final HostnameVerifier verifier) {
            clientBuilder.hostnameVerifier(verifier);
            return this;
        }

        /**
         * Sets a JAX-RS provider to use for this request. Can be called
         * multiple times to add multiple providers.
         *
         * @param provider the provider.
         * @return self, the client builder.
         */
        public Builder provider(final Class<?> provider) {
            clientBuilder.register(provider);
            return this;
        }

        /**
         * Build the client.
         *
         * @return the client.
         */
        public SimpleHttpClient build() {
            return new SimpleHttpClient(clientBuilder.build());
        }
    }
}
