/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet;

import com.google.common.annotations.Beta;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configurable;

/**
 * Utility methods for instantiating {@link HttpServlet}s from {@link Application}s, abstracting the servlet
 * implementation from application developers. Implementations of this interface are NOT thread-safe.
 *
 * @author Robert Varga
 */
@Beta
public interface HttpServletBuilder extends Configurable<HttpServletBuilder> {

    HttpServlet build();
}
