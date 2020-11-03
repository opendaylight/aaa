/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet.jersey2;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.kohsuke.MetaInfServices;
import org.opendaylight.aaa.web.servlet.HttpServletBuilder;
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.osgi.service.component.annotations.Component;

@Beta
@Component(immediate = true)
@MetaInfServices
@Singleton
@NonNullByDefault
public final class JerseyServletSupport implements ServletSupport {
    @Inject
    public JerseyServletSupport() {
        // This is a stateless service
    }

    @Override
    public HttpServletBuilder createHttpServletBuilder(final Application application) {
        return new JerseyHttpServletBuilder(application);
    }

    @Override
    public ClientBuilder newClientBuilder() {
        return new JerseyClientBuilder();
    }
}
