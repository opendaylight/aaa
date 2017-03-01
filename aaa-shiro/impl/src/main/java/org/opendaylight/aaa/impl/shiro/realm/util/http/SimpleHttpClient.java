/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.shiro.realm.util.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * An utility that represents an HTTP client that allows to make
 * HTTP requests.
 */
public class SimpleHttpClient {

    private final Client client;

    private SimpleHttpClient(Client client) {
        this.client = client;
    }

    /**
     * Obtain a builder for {@code SimpleHttpClient}.
     *
     * @return the client builder.
     */
    public static Builder clientBuilder() {
        return new Builder();
    }

    /**
     * Obtain a builder for {@link SimpleHttpRequest}.
     *
     * @param outputType the return type of the request.
     * @param <T> the return type of the request.
     * @return the request builder.
     */
    public <T> SimpleHttpRequest.Builder<T> requestBuilder(Class<T> outputType) {
        return new SimpleHttpRequest.Builder<>(client, outputType);
    }

    public static class Builder {

        private SSLContext sslContext;
        private HostnameVerifier hostnameVerifier;
        private final Set<Class<?>> providers = new HashSet<>();

        private Builder() {}

        /**
         * Sets the SSLContext to be used for SSL requests.
         *
         * @param sslContext the SSLContext.
         * @return self, the client builder.
         */
        public Builder sslContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Sets the hostname verifier the request is made with.
         *
         * @param hostnameVerifier the hostname verifier.
         * @return self, the client builder.
         */
        public Builder hostnameVerifier(final HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
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
            providers.add(provider);
            return this;
        }

        /**
         * Build the client.
         *
         * @return the client.
         */
        public SimpleHttpClient build() {
            final ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getClasses().addAll(providers);
            clientConfig.getProperties().put(
                    HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                    new HTTPSProperties(hostnameVerifier, sslContext));
            final Client client = Client.create(clientConfig);
            return new SimpleHttpClient(client);
        }

    }
}
