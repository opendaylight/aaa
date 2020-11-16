/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

public class OSGiCertServiceConfig extends ForwardingObject implements AaaCertServiceConfig {

    static final String FACTORY_NAME = "org.opendaylight.aaa.OSGiCertServiceConfig";

    // Keys to for activation properties
    static final String DELEGATE = ".delegate";

    AaaCertServiceConfig delegate = null;

    @Override
    public @Nullable Boolean isUseConfig() {
        return delegate().isUseConfig();
    }

    @Override
    public @Nullable Boolean isUseMdsal() {
        return delegate().isUseMdsal();
    }

    @Override
    public @Nullable String getBundleName() {
        return delegate().getBundleName();
    }

    @Override
    public @Nullable CtlKeystore getCtlKeystore() {
        return delegate().getCtlKeystore();
    }

    @Override
    public @Nullable TrustKeystore getTrustKeystore() {
        return delegate().getTrustKeystore();
    }

    @Override
    public @NonNull Map<Class<? extends Augmentation<AaaCertServiceConfig>>,
            Augmentation<AaaCertServiceConfig>> augmentations() {
        return delegate().augmentations();
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (AaaCertServiceConfig) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(AaaCertServiceConfig delegate) {
        Dictionary<String, Object> ret = new Hashtable<>(2);
        ret.put(DELEGATE, requireNonNull(delegate));
        return ret;
    }

    @Override
    protected AaaCertServiceConfig delegate() {
        return verifyNotNull(delegate);
    }
}
