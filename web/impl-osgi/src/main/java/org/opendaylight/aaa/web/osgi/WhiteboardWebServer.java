/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;

@RequireHttpWhiteboard
@Component(scope = ServiceScope.BUNDLE)
public final class WhiteboardWebServer implements WebServer {
    private final Bundle bundle;

    @Activate
    public WhiteboardWebServer(final ComponentContext componentContext) {
        bundle = componentContext.getUsingBundle();
    }

    @Override
    public String getBaseURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Registration registerWebContext(final WebContext webContext) throws ServletException {
        final var bundleContext = bundle.getBundleContext();
        // FIXME: normalize w.r.t. slashes
        final var contextPath = webContext.contextPath();
        // TODO: can we create a better name?
        final var contextName = contextPath + ".id";
        final var contextSelect = "(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + contextName + ")";
        final var builder = ImmutableList.<ServiceRegistration<?>>builder();

        // The order in which we set things up here matters...

        // 1. ServletContextHelper
        builder.add(bundleContext.registerService(ServletContextHelper.class,
            new WhiteboardServletContextHelper(bundle), new MapDictionary<>(Map.of(
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, contextName,
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH, contextPath))));

        // 2. Listeners - because they could set up things that filters and servlets need
        for (var listener : webContext.listeners()) {
            builder.add(bundleContext.registerService(ServletContextListener.class, listener,
                new MapDictionary<>(Map.of(
                    HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, contextSelect,
                    HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER, Boolean.TRUE))));
        }

        // 3. Filters - because subsequent servlets should already be covered by the filters
        for (FilterDetails filter : webContext.filters()) {
            builder.add(bundleContext.registerService(Filter.class, filter.filter(), new MapDictionary<>(Map.of(
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, contextSelect

                // FIXME: properties
                ))));

//            registerFilter(osgiHttpContext, filter.urlPatterns(), filter.name(), filter.filter(),
//                    filter.initParams(), filter.getAsyncSupported());


//            private void registerFilter(final HttpContext osgiHttpContext, final List<String> urlPatterns,
//                final String name, final Filter filter, final Map<String, String> params,
//                final Boolean asyncSupported) {
//            final String[] absUrlPatterns = absolute(urlPatterns);
//            LOG.info("Registering Filter for aliases {}: {} with async: {}", Arrays.asList(absUrlPatterns),
//                    filter, asyncSupported);
//            paxWeb.registerFilter(filter, absUrlPatterns, new String[] { name }, new MapDictionary<>(params),
//                    asyncSupported, osgiHttpContext);
//            registeredFilters.add(filter);
        }

        }





        final var services = builder.build();

        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                services.forEach(ServiceRegistration::unregister);
            }
        };
    }
}
