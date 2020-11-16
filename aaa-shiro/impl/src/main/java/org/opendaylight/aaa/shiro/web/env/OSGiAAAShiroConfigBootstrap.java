/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.util.Collection;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfigurationBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
public final class OSGiAAAShiroConfigBootstrap implements ClusteredDataTreeChangeListener<ShiroConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiAAAShiroConfigBootstrap.class);

    @Reference
    DataBroker dataBroker;

    @Reference(target = "(component.factory=" + OSGiAAAShiroConfig.FACTORY_NAME + ")")
    ComponentFactory configFactory;

    private ListenerRegistration<?> registration;
    private ComponentInstance instance;

    @Activate
    synchronized void activate() {
        registration = dataBroker.registerDataTreeChangeListener(DataTreeIdentifier.create(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(ShiroConfiguration.class)), this);
        LOG.info("Listening for shiro configuration");
    }

    @Deactivate
    synchronized void deactivate() {
        registration.close();
        instance.dispose();
        LOG.info("No longer listening for shiro configuration");
    }

    @Override
    public synchronized void onInitialData() {
        updateInstance(null);
    }

    @Override
    public synchronized void onDataTreeChanged(
            @NonNull final Collection<DataTreeModification<ShiroConfiguration>> changes) {
        updateInstance(Iterables.getLast(changes).getRootNode().getDataAfter());
    }

    @Holding("this")
    private void updateInstance(final ShiroConfiguration config) {
        if (registration != null) {
            // TODO fill in default values from xml
            final ComponentInstance newInstance = configFactory.newInstance(OSGiAAAShiroConfig.props(config != null
                    ? config : new ShiroConfigurationBuilder().build()));
            if (instance != null) {
                instance.dispose();
            }
            instance = newInstance;
        }
    }
}
