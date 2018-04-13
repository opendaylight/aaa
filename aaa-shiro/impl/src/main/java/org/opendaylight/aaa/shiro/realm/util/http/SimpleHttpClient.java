/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm.util.http;

import javax.ws.rs.client.Client;

/**
 * An utility that represents an HTTP client that allows to make
 * HTTP requests.
 */
//Suppressed so UT's can mock it using Mockito.
@SuppressWarnings("checkstyle:FinalClass")
public class SimpleHttpClient {

    private final Client client;

    public SimpleHttpClient(final Client client) {
        this.client = client;
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
}
