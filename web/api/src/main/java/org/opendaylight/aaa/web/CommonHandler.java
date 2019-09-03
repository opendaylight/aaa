/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The CommonHandler interface is the base interface for handlers for web server usage.
 *
 * <p>The purpose is not to use dependency on Jetty Web Server</p>
 *
 * <p>This is a functional interface whose functional method is getHandler().</p>
 *
 * @param <T> the type of handler that is processed
 */
@FunctionalInterface
public interface CommonHandler<T> {
    /**
     * Get the current handler to be processed by web server.
     *
     * @return a not null instance of Jetty handler object
     */
    @NonNull T getHandler();
}
