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
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ResourceDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;
import org.ops4j.pax.web.service.WebContainer;
import org.ops4j.pax.web.service.WebContainerDTO;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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
 */
@Singleton
public class PaxWebServer implements ServiceFactory<WebServer> {

    // TODO write an IT (using Pax Exam) which tests this, re-use JettyLauncherTest

    private static final Logger LOG = LoggerFactory.getLogger(PaxWebServer.class);

    private final WebContainer paxWeb;
    private final ServiceRegistration<?> serviceRegistration;

    @Inject
    public PaxWebServer(final @Reference WebContainer paxWebContainer, final BundleContext bundleContext) {
        this.paxWeb = paxWebContainer;
        serviceRegistration = bundleContext.registerService(WebServer.class, this, null);
        LOG.info("PaxWebServer initialized & WebServer service factory registered");
    }

    @PreDestroy
    public void close() {
        serviceRegistration.unregister();
    }

    String getBaseURL() {
        WebContainerDTO details = paxWeb.getWebcontainerDTO();
        if (details.securePort != null && details.securePort > 0) {
            return "https://" + details.listeningAddresses[0] + ":" + details.securePort;
        } else {
            return "http://" + details.listeningAddresses[0] + ":" + details.port;
        }
    }

    @Override
    public WebServer getService(final Bundle bundle, final ServiceRegistration<WebServer> registration) {
        LOG.info("Creating WebServer instance for bundle {}", bundle);

        final BundleContext bundleContext = bundle.getBundleContext();

        // Get the WebContainer service using the given bundle's context so the WebContainer service instance uses
        // the bundle's class loader.
        final ServiceReference<WebContainer> webContainerServiceRef =
                bundleContext.getServiceReference(WebContainer.class);

        final WebContainer bundleWebContainer;
        if (webContainerServiceRef != null) {
            bundleWebContainer = bundleContext.getService(webContainerServiceRef);
        } else {
            bundleWebContainer = null;
        }

        if (bundleWebContainer == null) {
            throw new IllegalStateException("WebContainer OSGi service not found using bundle: " + bundle.toString());
        }

        return new WebServer() {
            @Override
            public WebContextRegistration registerWebContext(final WebContext webContext) throws ServletException {
                return new WebContextImpl(bundleWebContainer, webContext) {
                    @Override
                    public void close() {
                        super.close();

                        try {
                            bundleContext.ungetService(webContainerServiceRef);
                        } catch (IllegalStateException e) {
                            LOG.debug("Error from ungetService", e);
                        }
                    }
                };
            }

            @Override
            public String getBaseURL() {
                return PaxWebServer.this.getBaseURL();
            }
        };
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration<WebServer> registration,
            final WebServer service) {
        // no-op
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
            for (ServletContextListener listener : webContext.listeners()) {
                registerListener(osgiHttpContext, listener);
            }

            // 3. Filters - because subsequent servlets should already be covered by the filters
            for (FilterDetails filter : webContext.filters()) {
                registerFilter(osgiHttpContext, filter.urlPatterns(), filter.name(), filter.filter(),
                        filter.initParams(), filter.getAsyncSupported());
            }

            // 4. servlets - 'bout time for 'em by now, don't you think? ;)
            for (ServletDetails servlet : webContext.servlets()) {
                registerServlet(osgiHttpContext, servlet.urlPatterns(), servlet.name(), servlet.servlet(),
                        servlet.initParams(),servlet.getAsyncSupported());
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
            return str.startsWith("/") ? str : "/" + str;
        }

        private String[] absolute(final List<String> relatives) {
            return relatives.stream().map(urlPattern -> contextPath + urlPattern).toArray(String[]::new);
        }

        private void registerFilter(final HttpContext osgiHttpContext, final List<String> urlPatterns,
                final String name, final Filter filter, final Map<String, String> params,
                Boolean asyncSupported) {
            String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Filter for aliases {}: {}", absUrlPatterns, filter);
            paxWeb.registerFilter(filter, absUrlPatterns, new String[] { name }, new MapDictionary<>(params),
                    asyncSupported, osgiHttpContext);
            registeredFilters.add(filter);
        }

        private void registerServlet(final HttpContext osgiHttpContext, final List<String> urlPatterns,
                final String name, final Servlet servlet, final Map<String, String> params,
                Boolean asyncSupported) throws ServletException {
            int loadOnStartup = 1;
            String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Servlet for aliases {}: {}", absUrlPatterns, servlet);
            paxWeb.registerServlet(servlet, name, absUrlPatterns, new MapDictionary<>(params), loadOnStartup,
                    asyncSupported, osgiHttpContext);
            registeredServlets.add(servlet);
        }

        private void registerListener(final HttpContext osgiHttpContext, final ServletContextListener listener) {
            paxWeb.registerEventListener(listener, osgiHttpContext);
            registeredEventListeners.add(listener);
        }

        @Override
        public void close() {
            // The order is relevant here.. Servlets first, then Filters, Listeners last; this is the inverse of above
            registeredServlets.forEach(paxWeb::unregisterServlet);
            registeredFilters.forEach(paxWeb::unregisterFilter);
            registeredEventListeners.forEach(paxWeb::unregisterEventListener);
            registeredResources.forEach(paxWeb::unregister);
        }
    }
}
