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

import com.google.common.annotations.Beta;
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
import org.osgi.service.component.annotations.Component;

@Beta
@Component(factory = OSGiCertServiceConfig.FACTORY_NAME)
public final class OSGiCertServiceConfig extends ForwardingObject implements AaaCertServiceConfig {
    // Keys to for activation properties
    private static final String DELEGATE = ".delegate";

    static final String FACTORY_NAME = "org.opendaylight.aaa.cert.impl.OSGiCertServiceConfig";

    private final AaaCertServiceConfig delegate;

    @Activate
    public OSGiCertServiceConfig(final Map<String, AaaCertServiceConfig> properties) {
        delegate = verifyNotNull(properties.get(DELEGATE));
    }

    @Override
    public Boolean getUseConfig() {
        return delegate().getUseConfig();
    }

    @Override
    public Boolean getUseMdsal() {
        return delegate().getUseMdsal();
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
    public @NonNull CtlKeystore nonnullCtlKeystore() {
        return delegate().nonnullCtlKeystore();
    }

    @Override
    public @Nullable TrustKeystore getTrustKeystore() {
        return delegate().getTrustKeystore();
    }

    @Override
    public @NonNull TrustKeystore nonnullTrustKeystore() {
        return delegate().nonnullTrustKeystore();
    }

    @Override
    public @NonNull Map<Class<? extends Augmentation<AaaCertServiceConfig>>,
            Augmentation<AaaCertServiceConfig>> augmentations() {
        return delegate().augmentations();
    }

    @SuppressModernizer
    static Dictionary<String, AaaCertServiceConfig> props(final AaaCertServiceConfig delegate) {
        final var ret = new Hashtable<String, AaaCertServiceConfig>(2);
        ret.put(DELEGATE, requireNonNull(delegate));
        return ret;
    }

    @Override
    protected AaaCertServiceConfig delegate() {
        return verifyNotNull(delegate);
    }
}
