/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 * Copyright (c) 2020 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.tokenauthrealm.auth;

import javax.inject.Singleton;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link InheritableThreadLocal}-based {@link AuthenticationService}.
 */
@Singleton
@Component(configurationPid = "org.opendaylight.aaa.authn")
@Designate(ocd = AuthenticationManager.Configuration.class)
public final class AuthenticationManager implements AuthenticationService {
    @ObjectClassDefinition(name = "OpenDaylight AAA Authentication Configuration")
    public @interface Configuration {
        @AttributeDefinition(
            name = "Enable authentication",
            description =
                "Enable authentication by setting it to the value 'true', or 'false' if bypassing authentication.\n"
                    + "Note that bypassing authentication may result in your controller being more vulnerable to "
                    + "unauthorized accesses.\n"
                    + "Authorization, if enabled, will not work if authentication is disabled.")
        boolean authEnabled() default true;
    }

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationManager.class);

    private volatile boolean authEnabled;

    private final ThreadLocal<Authentication> auth = new InheritableThreadLocal<>();

    public AuthenticationManager() {
        // In non-Karaf environments, authEnabled is set to false by default
        this(false);
    }

    public AuthenticationManager(final boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public void setAuthEnabled(final boolean authEnabled) {
        this.authEnabled = authEnabled;
        LOG.info("Authentication is now {}", authEnabled ? "enabled" : "disabled");
    }

    @Override
    public Authentication get() {
        return auth.get();
    }

    @Override
    public void set(final Authentication authentication) {
        auth.set(authentication);
    }

    @Override
    public void clear() {
        auth.remove();
    }

    @Override
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    @Activate
    void activate(final Configuration configuration) {
        setAuthEnabled(configuration.authEnabled());
        LOG.info("Authentication Manager activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Authentication Manager deactivated");
    }

    @Modified
    void modified(final Configuration configuration) {
        setAuthEnabled(configuration.authEnabled());
    }
}
