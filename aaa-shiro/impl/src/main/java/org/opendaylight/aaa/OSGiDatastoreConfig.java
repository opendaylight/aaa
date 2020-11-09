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

import com.google.common.collect.ForwardingObject;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

public class OSGiDatastoreConfig extends ForwardingObject implements DatastoreConfig {

    static final String FACTORY_NAME = "org.opendaylight.aaa.OSGiDatastoreConfig";

    // Keys to for activation properties
    static final String DELEGATE = ".delegate";

    DatastoreConfig delegate = null;

    @Override
    protected DatastoreConfig delegate() {
        return verifyNotNull(delegate);
    }

    @Override
    public @Nullable Store getStore() {
        return delegate().getStore();
    }

    @Override
    public @Nullable Uint64 getTimeToLive() {
        return delegate().getTimeToLive();
    }

    @Override
    public @Nullable Uint64 getTimeToWait() {
        return delegate().getTimeToWait();
    }

    @Override
    public @NonNull Map<Class<? extends Augmentation<DatastoreConfig>>, Augmentation<DatastoreConfig>> augmentations() {
        return delegate().augmentations();
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (DatastoreConfig) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(DatastoreConfig delegate) {
        Dictionary<String, Object> ret = new Hashtable<>(2);
        ret.put(DELEGATE, requireNonNull(delegate));
        return ret;
    }
}
