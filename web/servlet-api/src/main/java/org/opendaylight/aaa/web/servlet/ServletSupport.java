/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet;

import com.google.common.annotations.Beta;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;

/**
 * Main API entry point. An implementation of this interface is injected in the application.
 *
 * @author Robert Varga
 */
@Beta
public interface ServletSupport {

    ClientBuilder createClientBuilder();

    HttpServletBuilder createHttpServletBuilder(Application application);
}
