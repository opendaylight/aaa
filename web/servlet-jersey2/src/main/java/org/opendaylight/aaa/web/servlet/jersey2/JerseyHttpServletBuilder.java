/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet.jersey2;

import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.opendaylight.aaa.web.servlet.HttpServletBuilder;

/**
 * Jersey-based implementation of {@link HttpServletBuilder}.
 *
 * @author Robert Varga
 */
final class JerseyHttpServletBuilder implements HttpServletBuilder {
    private ResourceConfig config;

    JerseyHttpServletBuilder(final Application application) {
        config = ResourceConfig.forApplication(application);
    }

    @Override
    public HttpServlet build() {
        return new ServletContainer(config);
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public HttpServletBuilder property(final String name, final Object value) {
        config = config.property(name, value);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Class<?> componentClass) {
        config = config.register(componentClass);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Class<?> componentClass, final int priority) {
        config = config.register(componentClass, priority);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Class<?> componentClass, final Class<?>... contracts) {
        config = config.register(componentClass, contracts);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Class<?> componentClass, final Map<Class<?>, Integer> contracts) {
        config = config.register(componentClass, contracts);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Object component) {
        config = config.register(component);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Object component, final int priority) {
        config = config.register(component, priority);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Object component, final Class<?>... contracts) {
        config = config.register(component, contracts);
        return this;
    }

    @Override
    public HttpServletBuilder register(final Object component, final Map<Class<?>, Integer> contracts) {
        config = config.register(component, contracts);
        return this;
    }
}
