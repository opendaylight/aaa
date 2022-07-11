/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ForwardingObject;
import java.util.Dictionary;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfig;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiPasswordServiceConfig.FACTORY_NAME)
public final class OSGiPasswordServiceConfig extends ForwardingObject implements PasswordServiceConfig {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.aaa.impl.password.service.OSGiPasswordServiceConfig";

    // Keys to for activation properties
    @VisibleForTesting
    static final String DELEGATE = ".delegate";

    private PasswordServiceConfig delegate;

    @Override
    public Map<Class<? extends Augmentation<PasswordServiceConfig>>, Augmentation<PasswordServiceConfig>>
            augmentations() {
        return delegate().augmentations();
    }

    @Override
    public String getAlgorithm() {
        return delegate().getAlgorithm();
    }

    @Override
    public Integer getIterations() {
        return delegate().getIterations();
    }

    @Override
    public String getPrivateSalt() {
        return delegate().getPrivateSalt();
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate().equals(obj);
    }

    @Override
    protected PasswordServiceConfig delegate() {
        return verifyNotNull(delegate);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (PasswordServiceConfig) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    static Dictionary<String, ?> props(final PasswordServiceConfig delegate) {
        return FrameworkUtil.asDictionary(Map.of(DELEGATE, delegate));
    }
}
