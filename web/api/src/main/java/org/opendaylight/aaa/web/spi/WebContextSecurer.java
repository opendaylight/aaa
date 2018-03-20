/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.spi;

import java.util.List;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebServer;

/**
 * Secures a {@link WebContextBuilder}.
 *
 * <p>This is a Service Provider Interface (SPI) which must be used by
 * implementations of {@link WebServer} to be able to implement the
 * {@link WebContext#urlPatternsRequiringAuthentication()}.
 *
 * <p>This interface is NOT to be used directly by code in projects
 * which create a {@link WebContext} to register on a {@link WebServer}.
 *
 * @author Michael Vorburger.ch
 */
public interface WebContextSecurer {

    /**
     * Configures the WebContext in an implementation specific manner so that it requires
     * authentication to access the given URL Patterns.  Typically, this will be done by
     * adding a <code>javax.servlet.Filter</code> (or several, and whatever else they need).
     */
    void requireAuthentication(WebContextBuilder webContextBuilder, List<String> urlPatterns);

}
