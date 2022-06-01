/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.ops4j.pax.web.service.WebContainer;
import org.ops4j.pax.web.service.WebContainerDTO;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
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
 * @author Robert Varga - reworked to use OSGi DS, which cuts the implementation down to bare bones.
 */
// FIXME: this really acts as an extender (note how we lookup in the context of target bundle) and should really be
//        eliminated in favor of such
// FIXME: even if not, OSGi R7 is changing the picture and should allow us to work without this crud
// TODO write an IT (using Pax Exam) which tests this, re-use JettyLauncherTest
@Component(scope = ServiceScope.BUNDLE)
public final class PaxWebServer implements WebServer {
    private static final Logger LOG = LoggerFactory.getLogger(PaxWebServer.class);

    // Global reference, acts as an activation guard
    @Reference
    WebContainer global = null;

    private ServiceReference<WebContainer> ref;
    private WebContainer local;

    @Override
    public String getBaseURL() {
        final WebContainerDTO details = local.getWebcontainerDTO();
        if (details.securePort != null && details.securePort > 0) {
            return "https://" + details.listeningAddresses[0] + ":" + details.securePort;
        } else {
            return "http://" + details.listeningAddresses[0] + ":" + details.port;
        }
    }

    @Override
    public Registration registerWebContext(final WebContext webContext) throws ServletException {
        return new WebContextImpl(local, webContext);
    }

    @Activate
    void activate(final ComponentContext componentContext) {
        final Bundle bundle = componentContext.getUsingBundle();
        final BundleContext bundleContext = bundle.getBundleContext();

        ref = verifyNotNull(bundle.getBundleContext().getServiceReference(WebContainer.class),
            "Failed to locate WebContext from %s", bundle);
        local = verifyNotNull(bundleContext.getService(ref), "Failed to get WebContext in %s", bundle);
        LOG.info("Activated WebServer instance for {}", bundleContext);
    }

    @Deactivate
    void deactivate(final ComponentContext componentContext) {
        final Bundle bundle = componentContext.getUsingBundle();
        final BundleContext bundleContext = bundle.getBundleContext();
        local = null;
        bundleContext.ungetService(ref);
        ref = null;
        LOG.info("Deactivated WebServer instance for {}", bundle);
    }

    private static class WebContextImpl extends AbstractRegistration {
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
            contextPath = webContext.contextPath();

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
                        servlet.initParams(), servlet.getAsyncSupported());
            }

            try {
                for (ResourceDetails resource: webContext.resources()) {
                    String alias = ensurePrependedSlash(contextPath + ensurePrependedSlash(resource.alias()));
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

        private void registerFilter(final HttpContext osgiHttpContext, final List<String> urlPatterns,
                final String name, final Filter filter, final Map<String, String> params,
                final Boolean asyncSupported) {
            final String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Filter for aliases {}: {} with async: {}", Arrays.asList(absUrlPatterns),
                    filter, asyncSupported);
            paxWeb.registerFilter(filter, absUrlPatterns, new String[] { name }, new MapDictionary<>(params),
                    asyncSupported, osgiHttpContext);
            registeredFilters.add(filter);
        }

        private String[] absolute(final List<String> relatives) {
            return relatives.stream().map(urlPattern -> contextPath + urlPattern).toArray(String[]::new);
        }

        private void registerServlet(final HttpContext osgiHttpContext, final List<String> urlPatterns,
                final String name, final Servlet servlet, final Map<String, String> params,
                final Boolean asyncSupported) throws ServletException {
            int loadOnStartup = 1;
            String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Servlet for aliases {}: {} with async: {}", absUrlPatterns,
                    servlet, asyncSupported);
            paxWeb.registerServlet(servlet, name, absUrlPatterns, new MapDictionary<>(params), loadOnStartup,
                    asyncSupported, osgiHttpContext);
            registeredServlets.add(servlet);
        }

        private void registerListener(final HttpContext osgiHttpContext, final ServletContextListener listener) {
            paxWeb.registerEventListener(listener, osgiHttpContext);
            registeredEventListeners.add(listener);
        }

        @Override
        protected void removeRegistration() {
            // The order is relevant here.. Servlets first, then Filters, Listeners last; this is the inverse of above
            registeredServlets.forEach(paxWeb::unregisterServlet);
            registeredFilters.forEach(paxWeb::unregisterFilter);
            registeredEventListeners.forEach(paxWeb::unregisterEventListener);
            registeredResources.forEach(paxWeb::unregister);
        }
    }
}
