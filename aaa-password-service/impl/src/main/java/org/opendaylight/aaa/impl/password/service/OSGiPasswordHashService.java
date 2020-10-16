/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService.Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "org.opendaylight.aaa.password.service", property = "type=default")
@Designate(ocd = Configuration.class)
public final class OSGiPasswordHashService implements PasswordHashService {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiPasswordHashService.class);

    private DefaultPasswordHashService delegate;

    @Override
    public PasswordHash getPasswordHash(final String password) {
        return delegate.getPasswordHash(password);
    }

    @Override
    public PasswordHash getPasswordHash(final String password, final String salt) {
        return delegate.getPasswordHash(password, salt);
    }

    @Override
    public boolean passwordsMatch(final String plaintext, final String stored, final String salt) {
        return delegate.passwordsMatch(plaintext, stored, salt);
    }

    @Activate
    void activate(final Configuration config) {
        delegate = new DefaultPasswordHashService(config);
        LOG.info("Password Hash Service started");
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Password Hash Service stopped");
    }
}
