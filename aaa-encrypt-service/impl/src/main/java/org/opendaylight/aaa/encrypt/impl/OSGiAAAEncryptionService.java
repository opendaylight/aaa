/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Deprecated
@Component(immediate = true, service = AAAEncryptionService.class, property = "type=default")
public final class OSGiAAAEncryptionService implements AAAEncryptionService {
    @Reference
    DataBroker dataBroker;

    private volatile AAAEncryptionServiceImpl delegate = null;

    @Override
    public String encrypt(final String data) {
        return delegate.encrypt(data);
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        return delegate.encrypt(data);
    }

    @Override
    public String decrypt(final String encryptedData) {
        return delegate.decrypt(encryptedData);
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) {
        return delegate.decrypt(encryptedData);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
    void bindConfig(final AaaEncryptServiceConfig config) {
        updatedConfig(config);
    }

    void unbindConfig(final AaaEncryptServiceConfig config) {
        delegate = null;
    }

    void updatedConfig(final AaaEncryptServiceConfig config) {
        delegate = new AAAEncryptionServiceImpl(config, dataBroker);
    }
}
