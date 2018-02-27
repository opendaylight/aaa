/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi.impl;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;
import org.ops4j.pax.cdi.api.OsgiService;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.ops4j.pax.web.service.WebContainer;
import org.ops4j.pax.web.service.WebContainerDTO;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebServer} (and {@link WebContext}) bridge implementation
 * delegating to Pax Web WebContainer (which extends an OSGi {@link HttpService}).
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = WebServer.class)
public class WebContextProviderOSGiImpl implements WebServer {

    // TODO write an IT (using Pax Exam) which tests this, re-use JettyLauncherTest

    private static final Logger LOG = LoggerFactory.getLogger(WebContextProviderOSGiImpl.class);

    private final WebContainer paxWeb;

    @Inject
    public WebContextProviderOSGiImpl(@OsgiService WebContainer osgiHttpService) {
        this.paxWeb = osgiHttpService;
    }

    @Override
    public String getBaseURL() {
        WebContainerDTO details = paxWeb.getWebcontainerDTO();
        return "http://" + details.listeningAddresses[0] + ":" + details.port;
    }

    @Override
    public WebContextRegistration registerWebContext(WebContext webContext) throws ServletException {
        return new WebContextImpl(webContext);
    }

    private class WebContextImpl implements WebContextRegistration {

        private final String contextPath;
        private final HttpContext osgiHttpContext;

        private final Queue<Servlet> registeredServlets = new ConcurrentLinkedQueue<>();
        private final Queue<EventListener> registeredEventListeners = new ConcurrentLinkedQueue<>();
        private final Queue<Filter> registeredFilters = new ConcurrentLinkedQueue<>();

        WebContextImpl(WebContext webContext) throws ServletException {
            // ignore webContext.hasSessions(); OSGi HttpService / Pax Web does not seem to support on/off
            // (it probably assumes always with session?)
            this.contextPath = webContext.contextPath();
            this.osgiHttpContext = paxWeb.createDefaultHttpContext();

            // The order in which we set things up here matters...

            // 1. contextParams - because filters, servlets and listeners could need them
            paxWeb.setContextParam(new MapDictionary<>(webContext.contextParams()), osgiHttpContext);

            // 2. filters - because subsequent servlets should already be covered by the filters
            webContext.filters().forEach(filter ->
                registerFilter(filter.urlPatterns(), filter.name(), filter.filter(), filter.initParams()));

            // 3. servlets - 'bout time for 'em by now, no? ;)
            for (ServletDetails servlet : webContext.servlets()) {
                registerServlet(servlet.urlPatterns(), servlet.name(), servlet.servlet(), servlet.initParams());
            }

            // 4. listeners - once all of above is set up
            webContext.listeners().forEach(listener -> registerListener(listener));
        }

        void registerFilter(List<String> urlPatterns, String name, Filter filter, Map<String, String> params) {
            boolean asyncSupported = false;
            paxWeb.registerFilter(filter, new String[] { name }, absolute(urlPatterns), new MapDictionary<>(params),
                    asyncSupported, osgiHttpContext);
            registeredFilters.add(filter);
        }

        String[] absolute(List<String> relatives) {
            return relatives.stream().map(urlPattern -> contextPath + "/" + urlPattern).toArray(String[]::new);
        }

        void registerServlet(List<String> urlPatterns, String name, Servlet servlet, Map<String, String> params)
                throws ServletException {
            int loadOnStartup = 1;
            boolean asyncSupported = false;
            String[] absUrlPatterns = absolute(urlPatterns);
            LOG.info("Registering Servlet for aliases {}: {}", absUrlPatterns, servlet);
            paxWeb.registerServlet(servlet, name, absUrlPatterns, new MapDictionary<>(params), loadOnStartup,
                    asyncSupported, osgiHttpContext);
            registeredServlets.add(servlet);
        }

        void registerListener(ServletContextListener listener) {
            paxWeb.registerEventListener(listener, osgiHttpContext);
            registeredEventListeners.add(listener);
        }

        @Override
        public void close() {
            // The order is relevant here.. Servlets first, then Filters, Listeners last
            for (Servlet registeredServlet : registeredServlets) {
                paxWeb.unregisterServlet(registeredServlet);
            }
            for (Filter filter : registeredFilters) {
                paxWeb.unregisterFilter(filter);
            }
            for (EventListener eventListener : registeredEventListeners) {
                paxWeb.unregisterEventListener(eventListener);
            }
        }

    }

}
