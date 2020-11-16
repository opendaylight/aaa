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
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiCertServiceConfig.FACTORY_NAME)
public final class OSGiCertServiceConfig extends ForwardingObject implements AaaCertServiceConfig {

    static final String FACTORY_NAME = "org.opendaylight.aaa.cert.impl.OSGiCertServiceConfig";

    // Keys to for activation properties
    static final String DELEGATE = ".delegate";

    private AaaCertServiceConfig delegate;

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
    public CtlKeystore nonnullCtlKeystore() {
        return Objects.requireNonNullElse(getCtlKeystore(), CtlKeystoreBuilder.empty());
    }

    @Override
    public @Nullable TrustKeystore getTrustKeystore() {
        return delegate().getTrustKeystore();
    }

    @Override
    public @NonNull TrustKeystore nonnullTrustKeystore() {
        return Objects.requireNonNullElse(getTrustKeystore(), TrustKeystoreBuilder.empty());
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
