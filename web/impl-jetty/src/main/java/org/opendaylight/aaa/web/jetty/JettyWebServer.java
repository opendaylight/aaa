/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.jetty;

import java.util.EnumSet;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebServer} (and {@link WebContext}) implementation based on Jetty.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@SuppressWarnings("checkstyle:IllegalCatch") // Jetty LifeCycle start() and stop() throws Exception
public class JettyWebServer implements WebServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyWebServer.class);

    private final int httpPort;
    private final Server server;
    private final ContextHandlerCollection contextHandlerCollection;

    public JettyWebServer(int httpPort) {
        this.httpPort = httpPort;
        this.server = new Server();
        server.setStopAtShutdown(true);

        ServerConnector http = new ServerConnector(server);
        http.setHost("localhost");
        http.setPort(httpPort);
        http.setIdleTimeout(30000);
        server.addConnector(http);

        this.contextHandlerCollection = new ContextHandlerCollection();
        server.setHandler(contextHandlerCollection);
    }

    @Override
    public String getBaseURL() {
        return "http://localhost:" + httpPort;
    }

    @PostConstruct
    @SuppressWarnings("checkstyle:IllegalThrows") // Jetty WebAppContext.getUnavailableException() throws Throwable
    public void start() throws Throwable {
        server.start();
        LOG.info("Started Jetty-based HTTP web server on port {} ({}).", httpPort, hashCode());
    }

    @PreDestroy
    public void stop() throws Exception {
        LOG.info("Stopping Jetty-based web server...");
        // NB server.stop() will call stop() on all ServletContextHandler/WebAppContext
        server.stop();
        LOG.info("Stopped Jetty-based web server.");
    }

    @Override
    public synchronized WebContextRegistration registerWebContext(WebContext webContext) throws ServletException {
        ServletContextHandler handler = new ServletContextHandler(contextHandlerCollection, webContext.contextPath(),
                webContext.supportsSessions() ? ServletContextHandler.SESSIONS : ServletContextHandler.NO_SESSIONS);

        // The order in which we do things here must be the same as
        // the equivalent in org.opendaylight.aaa.web.osgi.PaxWebServer

        // 1. Context parameters - because listeners, filters and servlets could need them
        webContext.contextParams().entrySet().forEach(entry -> handler.setAttribute(entry.getKey(), entry.getValue()));
        // also handler.getServletContext().setAttribute(name, value), both seem work

        // 2. Listeners - because they could set up things that filters and servlets need
        webContext.listeners().forEach(listener -> handler.addEventListener(listener));

        // 3. Filters - because subsequent servlets should already be covered by the filters
        webContext.filters().forEach(filter -> {
            FilterHolder filterHolder = new FilterHolder(filter.filter());
            filterHolder.setInitParameters(filter.initParams());
            filter.urlPatterns().forEach(
                urlPattern -> handler.addFilter(filterHolder, urlPattern, EnumSet.allOf(DispatcherType.class))
            );
        });

        // 4. servlets - 'bout time for 'em by now, don't you think? ;)
        webContext.servlets().forEach(servlet -> {
            ServletHolder servletHolder = new ServletHolder(servlet.name(), servlet.servlet());
            servletHolder.setInitParameters(servlet.initParams());
            servletHolder.setInitOrder(1); // AKA <load-on-startup> 1
            servlet.urlPatterns().forEach(
                urlPattern -> handler.addServlet(servletHolder, urlPattern)
            );
        });

        restart(handler);

        return () -> close(handler);
    }

    private void restart(AbstractLifeCycle lifecycle) throws ServletException {
        try {
            lifecycle.start();
        } catch (Exception e) {
            if (e instanceof ServletException) {
                throw (ServletException) e;
            } else {
                throw new ServletException("registerServlet() start failed", e);
            }
        }
    }

    private void close(ServletContextHandler handler) {
        try {
            handler.stop();
            handler.destroy();
        } catch (Exception e) {
            LOG.error("close() failed", e);
        }
        contextHandlerCollection.removeHandler(handler);
    }
}
