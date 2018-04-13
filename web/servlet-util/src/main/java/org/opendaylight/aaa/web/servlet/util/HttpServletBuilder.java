/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet.util;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configurable;
import org.opendaylight.yangtools.concepts.Builder;

/**
 * Utility methods for instantiating {@link HttpServlet}s from {@link Application}s, abstracting the servlet
 * implementation from application developers.
 *
 * @author Robert Varga
 */
@Beta
@NotThreadSafe
public abstract class HttpServletBuilder implements Builder<HttpServlet>, Configurable<HttpServletBuilder> {
    /*
     * Design note: this class is the only publicly-accessible contract of this package. As such it must be kept
     * implementation-agnostic and therefore it MUST NOT refer to anything outside of servlet-api and javax.ws.rs.
     */
    HttpServletBuilder() {
        // Prevents outside subclassing
    }

    public static HttpServletBuilder forApplication(final Application application) {
        return new JerseyHttpServletBuilder(application);
    }
}
