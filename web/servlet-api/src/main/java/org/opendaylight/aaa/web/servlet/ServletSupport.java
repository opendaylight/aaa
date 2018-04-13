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
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Main API entry point. An implementation of this interface is injected in the application.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface ServletSupport {
    /**
     * Create a new {@link ClientBuilder}.
     *
     * @return A new ClientBuilder.
     */
    ClientBuilder newClientBuilder();

    /**
     * Create a new {@link HttpServletBuilder} for an {@link Application}.
     *
     * @return A new HttpServletBuilder.
     */
    HttpServletBuilder createHttpServletBuilder(Application application);
}
