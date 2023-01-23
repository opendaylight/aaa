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
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true, property = "type=default")
public final class OSGiPasswordHashService implements PasswordHashService {
    private volatile DefaultPasswordHashService delegate = null;

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

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
    void bindConfig(final PasswordServiceConfig config) {
        updatedConfig(config);
    }

    void unbindConfig(final PasswordServiceConfig config) {
        delegate = null;
    }

    void updatedConfig(final PasswordServiceConfig config) {
        delegate = new DefaultPasswordHashService(config);
    }
}
