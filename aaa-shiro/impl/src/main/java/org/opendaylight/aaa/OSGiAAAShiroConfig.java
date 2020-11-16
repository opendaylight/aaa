/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Main;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Urls;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiAAAShiroConfig.FACTORY_NAME)
public final class OSGiAAAShiroConfig extends ForwardingObject implements ShiroConfiguration {

    static final String FACTORY_NAME = "org.opendaylight.aaa.OSGiAAAShiroConfig";

    // Keys to for activation properties
    static final String DELEGATE = ".delegate";

    ShiroConfiguration delegate = null;

    @Override
    protected ShiroConfiguration delegate() {
        return verifyNotNull(delegate);
    }

    @Override
    public @Nullable List<Main> getMain() {
        return delegate().getMain();
    }

    @Override
    public @Nullable List<Urls> getUrls() {
        return delegate().getUrls();
    }

    @Override
    public @NonNull Map<Class<? extends Augmentation<ShiroConfiguration>>,
            Augmentation<ShiroConfiguration>> augmentations() {
        return delegate().augmentations();
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (ShiroConfiguration) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(ShiroConfiguration delegate) {
        Dictionary<String, Object> ret = new Hashtable<>(2);
        ret.put(DELEGATE, requireNonNull(delegate));
        return ret;
    }
}
