/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ResourceDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.runtime.HttpServiceRuntime;
import org.osgi.service.http.runtime.HttpServiceRuntimeConstants;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebServer} implementation based on
 * <a href="https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.http.whiteboard.html">OSGi HTTP Whiteboard</a>.
 */
@RequireHttpWhiteboard
@Component(scope = ServiceScope.BUNDLE)
public final class WhiteboardWebServer implements WebServer {
    private static final Logger LOG = LoggerFactory.getLogger(WhiteboardWebServer.class);

    private final Bundle bundle;
    @Reference
    private volatile ServiceReference<HttpServiceRuntime> serviceRuntime;

    /**
     * Construct a {@link WhiteboardWebServer} to a {@link ComponentContext}.
     *
     * @param componentContext A {@link ComponentContext}
     */
    @Activate
    public WhiteboardWebServer(final ComponentContext componentContext) {
        bundle = componentContext.getUsingBundle();
        LOG.debug("Activated WebServer for bundle {}", bundle);
    }

    @Deactivate
    void deactivate() {
        LOG.debug("Deactivated WebServer for bundle {}", bundle);
    }

    @Override
    public String getBaseURL() {
        final var endpoint = serviceRuntime.getProperty(HttpServiceRuntimeConstants.HTTP_SERVICE_ENDPOINT);
        if (endpoint instanceof String str) {
            return str;
        } else if (endpoint instanceof String[] endpoints) {
            return getBaseURL(Arrays.asList(endpoints));
        } else if (endpoint instanceof Collection) {
            // Safe as per OSGi Compendium R7 section 140.15.3.1
            @SuppressWarnings("unchecked")
            final var cast = (Collection<String>) endpoint;
            return getBaseURL(cast);
        } else {
            throw new IllegalStateException("Unhandled endpoint " + endpoint);
        }
    }

    private static String getBaseURL(final Collection<String> endpoints) {
        for (var endpoint : endpoints) {
            if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
                return endpoint;
            }
        }
        throw new IllegalStateException("Cannot select base URL from " + endpoints);
    }

    @Override
    public Registration registerWebContext(final WebContext webContext) throws ServletException {
        final var bundleContext = bundle.getBundleContext();
        final var builder = ImmutableList.<ServiceRegistration<?>>builder();

        // The order in which we set things up here matters...

        // 1. ServletContextHelper, to which all others are bound to
        final var contextPath = webContext.contextPath();
        // TODO: can we create a better name?
        final var contextName = contextPath + ".id";

        final var contextProps = contextProperties(contextName, contextPath, webContext.contextParams());
        LOG.debug("Registering context {} with properties {}", contextName, contextProps);
        builder.add(bundleContext.registerService(ServletContextHelper.class,
            new WhiteboardServletContextHelper(bundle), FrameworkUtil.asDictionary(contextProps)));

        // 2. Listeners - because they could set up things that filters and servlets need
        final var contextSelect = "(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + contextName + ")";
        for (var listener : webContext.listeners()) {
            final var props = Map.of(
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, contextSelect,
                HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER, "true");
            LOG.debug("Registering listener {} with properties {}", listener, props);
            builder.add(bundleContext.registerService(ServletContextListener.class, listener,
                FrameworkUtil.asDictionary(props)));
        }

        // 3. Filters - because subsequent servlets should already be covered by the filters
        for (var filter : webContext.filters()) {
            final var props = filterProperties(contextSelect, filter);
            LOG.debug("Registering filter {} with properties {}", filter, props);
            builder.add(bundleContext.registerService(Filter.class, filter.filter(),
                FrameworkUtil.asDictionary(props)));
        }

        // 4. Servlets - 'bout time for 'em by now, don't you think? ;)
        for (var servlet : webContext.servlets()) {
            final var props = servletProperties(contextSelect, servlet);
            LOG.debug("Registering servlet {} with properties {}", servlet, props);
            builder.add(bundleContext.registerService(Servlet.class, servlet.servlet(),
                FrameworkUtil.asDictionary(props)));
        }

        // 5. Resources
        for (var resource : webContext.resources()) {
            final var props = resourceProperties(contextSelect, resource);
            LOG.debug("Registering resource {} with properties {}", resource, props);
            builder.add(bundleContext.registerService(Object.class, WhiteboardResource.INSTANCE,
                FrameworkUtil.asDictionary(props)));
        }

        final var services = builder.build();
        LOG.info("Bundle {} registered context path {} with {} service(s)", bundle, contextPath, services.size());
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                // The order does not have to be reversed: we unregister ServletContextHelper first, hence everybody
                // becomes unbound
                services.forEach(ServiceRegistration::unregister);
            }
        };
    }

    private static Map<String, Object> contextProperties(final String contextName, final String contextPath,
            final Map<String, String> params) {
        final var builder = ImmutableMap.<String, Object>builder()
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, contextName)
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH, contextPath);

        for (var e : params.entrySet()) {
            builder.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_INIT_PARAM_PREFIX + e.getKey(), e.getValue());
        }

        return builder.build();
    }

    private static Map<String, Object> filterProperties(final String contextSelect, final FilterDetails filter) {
        final var builder = ImmutableMap.<String, Object>builder()
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, contextSelect)
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_ASYNC_SUPPORTED, filter.asyncSupported())
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_NAME, filter.name())
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, absolutePatterns(filter.urlPatterns()));

        for (var e : filter.initParams().entrySet()) {
            builder.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_INIT_PARAM_PREFIX + e.getKey(), e.getValue());
        }

        return builder.build();
    }

    private static Map<String, Object> resourceProperties(final String contextSelect, final ResourceDetails resource) {
        final var alias = absolutePath(resource.alias());

        return Map.of(
            HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, contextSelect,
            HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN, alias.endsWith("/") ? alias + '*' : alias + "/*",
            HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX, absolutePath(resource.name()));
    }

    private static Map<String, Object> servletProperties(final String contextSelect, final ServletDetails servlet) {
        final var builder = ImmutableMap.<String, Object>builder()
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, contextSelect)
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, servlet.asyncSupported())
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, servlet.name())
            .put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, absolutePatterns(servlet.urlPatterns()));

        for (var e : servlet.initParams().entrySet()) {
            builder.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX + e.getKey(), e.getValue());
        }

        return builder.build();
    }

    private static String absolutePath(final String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private static List<String> absolutePatterns(final List<String> urlPatterns) {
        return urlPatterns.stream()
            // Reject duplicates
            .distinct()
            // Ease of debugging
            .sorted()
            .collect(Collectors.toUnmodifiableList());
    }
}
