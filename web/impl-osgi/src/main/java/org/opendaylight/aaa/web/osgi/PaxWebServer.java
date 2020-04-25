/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ResourceDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;
import org.ops4j.pax.web.service.WebContainer;
import org.ops4j.pax.web.service.WebContainerDTO;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebServer} (and {@link WebContext}) bridge implementation
 * delegating to Pax Web WebContainer (which extends an OSGi {@link HttpService}).
 *
 * @author Michael Vorburger.ch - original author
 * @author Tom Pantelis - added ServiceFactory to solve possible class loading issues in web components
 * @author Robert Varga - reworked to use OSGi DS, which cuts the implementation down to bare bones
 */
// TODO write an IT (using Pax Exam) which tests this, re-use JettyLauncherTest
@Component(service = WebServer.class, scope = ServiceScope.BUNDLE)
public class PaxWebServer implements WebServer {
    private static final Logger LOG = LoggerFactory.getLogger(PaxWebServer.class);

    @Reference
    WebContainer paxWeb = null;

    @Override
    public String getBaseURL() {
        final WebContainerDTO details = paxWeb.getWebcontainerDTO();
        if (details.securePort != null && details.securePort > 0) {
            return "https://" + details.listeningAddresses[0] + ":" + details.securePort;
        } else {
            return "http://" + details.listeningAddresses[0] + ":" + details.port;
        }
    }

    @Override
    public WebContextRegistration registerWebContext(final WebContext webContext) throws ServletException {
        return new WebContextImpl(paxWeb, webContext);
    }

    @Activate
    void activate(final BundleContext bundleContext) {
        LOG.info("Activated WebServer instance for {}", bundleContext.getBundle());
    }

    @Deactivate
    void deactivate(final BundleContext bundleContext) {
        LOG.info("Deactivated WebServer instance for {}", bundleContext.getBundle());
    }

    private static class WebContextImpl implements WebContextRegistration {
        private final String contextPath;
        private final WebContainer paxWeb;
        private final List<Servlet> registeredServlets = new ArrayList<>();
        private final List<EventListener> registeredEventListeners = new ArrayList<>();
        private final List<Filter> registeredFilters = new ArrayList<>();
        private final List<String> registeredResources = new ArrayList<>();

        WebContextImpl(final WebContainer paxWeb, final WebContext webContext) throws ServletException {
            // We ignore webContext.supportsSessions() because the OSGi HttpService / Pax Web API
            // does not seem to support not wanting session support on some web contexts
            // (it assumes always with session); but other implementation support without.

            this.paxWeb = paxWeb;
            this.contextPath = webContext.contextPath();

            // NB This is NOT the URL prefix of the context, but the context.id which is
            // used while registering the HttpContext in the OSGi service registry.
            String contextID = contextPath + ".id";

            HttpContext osgiHttpContext = paxWeb.createDefaultHttpContext(contextID);
            paxWeb.begin(osgiHttpContext);

            // The order in which we set things up here matters...

            // 1. Context parameters - because listeners, filters and servlets could need them
            paxWeb.setContextParam(new MapDictionary<>(webContext.contextParams()), osgiHttpContext);

            // 2. Listeners - because they could set up things that filters and servlets need
            webContext.listeners().forEach(listener -> registerListener(osgiHttpContext, listener));

            // 3. Filters - because subsequent servlets should already be covered by the filters
            webContext.filters().forEach(filter ->
                registerFilter(osgiHttpContext, filter.urlPatterns(), filter.name(), filter.filter(),
                        filter.initParams()));

            // 4. servlets - 'bout time for 'em by now, don't you think? ;)
            for (ServletDetails servlet : webContext.servlets()) {
                registerServlet(osgiHttpContext, servlet.urlPatterns(), servlet.name(), servlet.servlet(),
                        servlet.initParams());
            }

            try {
                for (ResourceDetails resource: webContext.resources()) {
                    String alias = ensurePrependedSlash(this.contextPath + ensurePrependedSlash(resource.alias()));
                    paxWeb.registerResources(alias, ensurePrependedSlash(resource.name()), osgiHttpContext);
                    registeredResources.add(alias);
                }
            } catch (NamespaceException e) {
                throw new ServletException("Error registering resources", e);
            }

            paxWeb.end(osgiHttpContext);
        }

        private static String ensurePrependedSlash(final String str) {
            return !str.startsWith("/") ? "/" + str : str;
        }

        void registerFilter(final HttpContext osgiHttpContext, final List<String> urlPatterns, final String name,
                final Filter filter, final Map<String, String> params) {
            boolean asyncSupported = false;
            String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Filter for aliases {}: {}", absUrlPatterns, filter);
            paxWeb.registerFilter(filter, absUrlPatterns, new String[] { name }, new MapDictionary<>(params),
                    asyncSupported, osgiHttpContext);
            registeredFilters.add(filter);
        }

        String[] absolute(final List<String> relatives) {
            return relatives.stream().map(urlPattern -> contextPath + urlPattern).toArray(String[]::new);
        }

        void registerServlet(final HttpContext osgiHttpContext, final List<String> urlPatterns, final String name,
                final Servlet servlet, final Map<String, String> params) throws ServletException {
            int loadOnStartup = 1;
            boolean asyncSupported = false;
            String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Servlet for aliases {}: {}", absUrlPatterns, servlet);
            paxWeb.registerServlet(servlet, name, absUrlPatterns, new MapDictionary<>(params), loadOnStartup,
                    asyncSupported, osgiHttpContext);
            registeredServlets.add(servlet);
        }

        void registerListener(final HttpContext osgiHttpContext, final ServletContextListener listener) {
            paxWeb.registerEventListener(listener, osgiHttpContext);
            registeredEventListeners.add(listener);
        }

        @Override
        public void close() {
            // The order is relevant here.. Servlets first, then Filters, Listeners last; this is the inverse of above
            for (Servlet registeredServlet : registeredServlets) {
                paxWeb.unregisterServlet(registeredServlet);
            }
            for (Filter filter : registeredFilters) {
                paxWeb.unregisterFilter(filter);
            }
            for (EventListener eventListener : registeredEventListeners) {
                paxWeb.unregisterEventListener(eventListener);
            }
            for (String alias : registeredResources) {
                paxWeb.unregister(alias);
            }
        }
    }
}
