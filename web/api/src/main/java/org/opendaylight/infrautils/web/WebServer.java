/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.web;

/**
 * Web server (HTTP). This service API allows ODL applications to register web
 * components programmatically, instead of using a web.xml declaratively. This
 * is preferable because:
 * <ul>
 * <li>using code instead of hiding class names in XML enables tools such as
 * e.g. BND (in the maven-bundle-plugin) to correctly figure dependencies e.g.
 * for OSGi Import-Package headers;
 *
 * <li>explicit passing of web components instances, instead of providing class
 * names in XML files and letting a web container create the new instances using
 * the default constructor, solves a pesky dependency injection (DI) related
 * problem which typically leads to weird hoops in code through
 * <code>static</code> etc. that can be avoid using this;
 *
 * <li>tests can more easily programmatically instantiate web components.
 * </ul>
 *
 * <p>
 * This API has an OSGi-based implementation and a standalone implementation.
 *
 * @author Michael Vorburger.ch
 */
public interface WebServer {

    /**
     * Register a new web context.
     *
     * @param webContext the web context
     *
     * @return registration which allows to close the context (and remove its servlets etc.)
     */
    WebContextRegistration registerWebContext(WebContext webContext);

    /**
     * Base URL of this web server, without any contexts. In production, this would
     * likely be HTTPS with a well known hostname and fixed port configured e.g. in
     * a Karaf etc/ configuration file. In tests, this would be typically be HTTP on
     * localhost and an arbitrarily chosen port.
     *
     * @return base URL, with http[s] prefix and port, NOT ending in slash
     */
    String getBaseURL();

}
