/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static java.util.Objects.requireNonNull;

import java.util.Base64;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;

record EncryptServiceConfigImpl(@NonNull AaaEncryptServiceConfig delegate) implements EncryptServiceConfig {
    EncryptServiceConfigImpl {
        requireNonNull(delegate);
    }

    @Override
    public Class<? extends EncryptServiceConfig> implementedInterface() {
        // This implementation is not generated and used only internally, hence this method is never called
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEncryptKey() {
        return delegate.requireEncryptKey();
    }

    @Override
    public byte[] getEncryptSalt() {
        return Base64.getDecoder().decode(delegate.requireEncryptSalt());
    }

    @Override
    public String getEncryptMethod() {
        return delegate.getEncryptMethod();
    }

    @Override
    public String getEncryptType() {
        return delegate.getEncryptType();
    }

    @Override
    public Integer getEncryptIterationCount() {
        return delegate.getEncryptIterationCount();
    }

    @Override
    public Integer getEncryptKeyLength() {
        return delegate.getEncryptKeyLength();
    }

    @Override
    public String getCipherTransforms() {
        return delegate.getCipherTransforms();
    }
}
