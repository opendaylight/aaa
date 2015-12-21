/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import java.util.Collection;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.Consumer;
import org.opendaylight.controller.sal.core.api.Provider;
import org.osgi.framework.BundleContext;

/**
 * Created by wdec on 26/08/2014.
 */
public class AuthzBrokerImpl implements Broker, AutoCloseable, Provider {

    private Broker broker;
    private ProviderSession providerSession;
    private AuthenticationService authenticationService;

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    @Override
    public void close() throws Exception {

    }

    // Implements AuthzBroker handling of registering consumers or providers.
    @Override
    public ConsumerSession registerConsumer(Consumer consumer) {

        ConsumerSession realSession = broker.registerConsumer(new ConsumerWrapper(consumer));
        AuthzConsumerContextImpl authzConsumerContext = new AuthzConsumerContextImpl(realSession,
                this);
        consumer.onSessionInitiated(authzConsumerContext);
        return authzConsumerContext;
    }

    @Override
    public ConsumerSession registerConsumer(Consumer consumer, BundleContext bundleContext) {

        ConsumerSession realSession = broker.registerConsumer(new ConsumerWrapper(consumer),
                bundleContext);
        AuthzConsumerContextImpl authzConsumerContext = new AuthzConsumerContextImpl(realSession,
                this);
        consumer.onSessionInitiated(authzConsumerContext);
        return authzConsumerContext;
    }

    @Override
    public ProviderSession registerProvider(Provider provider) {

        ProviderSession realSession = broker.registerProvider(new ProviderWrapper(provider));
        AuthzProviderContextImpl authzProviderContext = new AuthzProviderContextImpl(realSession,
                this);
        provider.onSessionInitiated(authzProviderContext);
        return authzProviderContext;
    }

    @Override
    public ProviderSession registerProvider(Provider provider, BundleContext bundleContext) {

        // Allow the real broker to do its thing, while providing a wrapped
        // callback
        ProviderSession realSession = broker.registerProvider(new ProviderWrapper(provider),
                bundleContext);

        // Create Authz ProviderContext
        AuthzProviderContextImpl authzProviderContext = new AuthzProviderContextImpl(realSession,
                this);

        // Run onsessionInitiated on injected provider with the AuthZ provider
        // context.
        provider.onSessionInitiated(authzProviderContext);
        return authzProviderContext;

    }

    // Handle the AuthZBroker registration with the real broker
    @Override
    public void onSessionInitiated(ProviderSession providerSession) {

        // Get now the real DOMDataBroker and register it with the
        // AuthzDOMBroker together with the provider session
        final DOMDataBroker domDataBroker = providerSession.getService(DOMDataBroker.class);
        AuthzDomDataBroker.getInstance().setProviderSession(providerSession);
        AuthzDomDataBroker.getInstance().setDomDataBroker(domDataBroker);
        AuthzDomDataBroker.getInstance().setAuthService(this.authenticationService);
    }

    @Override
    public Collection<ProviderFunctionality> getProviderFunctionality() {
        return null;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // Wrapper for Provider

    public static class ProviderWrapper implements Provider {
        private final Provider provider;

        public ProviderWrapper(Provider provider) {
            this.provider = provider;
        }

        @Override
        public void onSessionInitiated(ProviderSession providerSession) {
            // Do a Noop when the real broker calls back
        }

        @Override
        public Collection<ProviderFunctionality> getProviderFunctionality() {
            // Allow the RestconfImpl to respond to this
            return provider.getProviderFunctionality();
        }
    }

    // Wrapper for Consumer
    public static class ConsumerWrapper implements Consumer {

        private final Consumer consumer;

        public ConsumerWrapper(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onSessionInitiated(ConsumerSession consumerSession) {
            // Do a Noop when the real broker calls back
        }

        @Override
        public Collection<ConsumerFunctionality> getConsumerFunctionality() {
            return consumer.getConsumerFunctionality();
        }
    }
}
