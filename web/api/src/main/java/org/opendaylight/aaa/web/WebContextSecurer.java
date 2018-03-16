/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

/**
 * Secures a {@link WebContextBuilder}.
 *
 * @author Michael Vorburger.ch
 */
public interface WebContextSecurer {

    /**
     * Configures the WebContext in an implementation specific manner so that it requires
     * authentication to access the given URL Patterns.  Typically, this will be done by
     * adding a <code>javax.servlet.Filter</code> (or several, and whatever else they need).
     */
    void requireAuthentication(WebContextBuilder webContextBuilder, String... urlPatterns);

    /**
     * Configures the WebContext so that all its URL patterns (<code>/**</code>) require authentication.
     * @see #requireAuthentication(WebContextBuilder, String...)
     */
    default void requireAuthentication(WebContextBuilder webContextBuilder) {
        requireAuthentication(webContextBuilder, "/*");
    }
}
