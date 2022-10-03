/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

/**
 * Secures a {@link WebContext.Builder}.
 *
 * @author Michael Vorburger.ch
 */
public interface WebContextSecurer {
    /**
     * Configure the WebContext to require auth for specified URLs.
     *
     * <p>
     * Configure the WebContext so that it requires authentication to access the
     * given URL Patterns. Typically, this will be done by adding a {@code javax.servlet.Filter} (or several, and
     * whatever else they need).
     *
     * @param webContextBuilder builder to secure
     * @param asyncSupported true if asynchronous communication should also be supported
     * @param urlPatterns URL patterns that require authentication
     */
    void requireAuthentication(WebContext.Builder webContextBuilder, boolean asyncSupported, String... urlPatterns);

    /**
     * Configure the WebContext to require auth for specified URLs.
     *
     * <p>
     * Configures the WebContext so that it requires authentication to access the
     * given URL Patterns. Typically, this will be done by adding a {@code javax.servlet.Filter} (or several, and
     * whatever else they need).
     *
     * <p>
     * This method is equivalent to {@code requireAuthentication(webContextBuilder, false, urlPatterns}.
     *
     * @param webContextBuilder builder to secure
     * @param urlPatterns URL patterns that require authentication
     */
    default void requireAuthentication(final WebContext.Builder webContextBuilder, final String... urlPatterns) {
        requireAuthentication(webContextBuilder, false, urlPatterns);
    }

    /**
     * Configure the WebContext to require auth all URLs.
     *
     * <p>
     * Configures the WebContext so that all its URL patterns ({@code/**}) require authentication.
     *
     * @param webContextBuilder builder to secure
     * @see #requireAuthentication(WebContext.Builder, String...)
     */
    default void requireAuthentication(final WebContext.Builder webContextBuilder) {
        requireAuthentication(webContextBuilder, "/*");
    }
}
