/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.servlet.ServletException;
import org.opendaylight.aaa.AAAShiroProvider;
import org.opendaylight.aaa.web.osgi.PaxWebServer;
import org.ops4j.pax.web.service.WebContainer;

/**
 * Initializer for AAA's web components in an OSGi environment.
 *
 * @author Michael Vorburger.ch
 */
public class OsgiWebInitializer {

    private final WebInitializer web;

    public OsgiWebInitializer(WebContainer paxWebContainer, AAAShiroProvider provider) throws ServletException {
        this.web = new WebInitializer(new PaxWebServer(paxWebContainer), provider);
    }

    public void close() {
        web.close();
    }

}
