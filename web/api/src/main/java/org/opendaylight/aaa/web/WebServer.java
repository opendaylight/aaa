/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import javax.servlet.ServletException;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Web server (HTTP). This service API allows ODL applications to register web
 * components programmatically, instead of using a web.xml declaratively; see
 * the {@link WebContext} for why this is preferable.
 *
 * <p>
 * This API has an OSGi-based as well as a "standalone" implementation suitable
 * e.g. for tests.
 *
 * @author Michael Vorburger.ch
 */
public interface WebServer {
    /**
     * Register a new web context.
     *
     * @param webContext the web context
     * @return registration which allows to close the context (and remove its servlets etc.)
     * @throws ServletException if registration of any of the components of the web context failed
     */
    Registration registerWebContext(WebContext webContext) throws ServletException;

    /**
     * Get base URL of this web server, without any contexts.
     *
     * <p>
     * In production, this would likely be HTTPS with a well known hostname and fixed port configured.
     * For example, in Karaf etc/ configuration file. In tests, this would be typically be HTTP on
     * localhost and an arbitrarily chosen port.
     *
     * @return base URL, with http[s] prefix and port, NOT ending in slash
     */
    String getBaseURL();
}
