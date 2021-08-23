/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.util.Collection;
import org.checkerframework.checker.lock.qual.Holding;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfigBuilder;
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
public final class OSGiPasswordServiceConfigBootstrap
        implements ClusteredDataTreeChangeListener<PasswordServiceConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiPasswordServiceConfigBootstrap.class);

    @Reference
    DataBroker dataBroker = null;

    @Reference(target = "(component.factory=" + OSGiPasswordServiceConfig.FACTORY_NAME + ")")
    ComponentFactory<OSGiPasswordServiceConfig> configFactory = null;

    private ListenerRegistration<?> registration;
    private ComponentInstance<?> instance;

    @Activate
    synchronized void activate() {
        registration = dataBroker.registerDataTreeChangeListener(
            DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(PasswordServiceConfig.class)), this);
        LOG.info("Listening for password service configuration");
    }

    @Deactivate
    synchronized void deactivate() {
        registration.close();
        registration = null;
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
        LOG.info("No longer listening for password service configuration");
    }

    @Override
    public synchronized void onInitialData() {
        updateInstance(null);
    }

    @Override
    public synchronized void onDataTreeChanged(final Collection<DataTreeModification<PasswordServiceConfig>> changes) {
        // FIXME: at this point we need to populate default values -- from the XML file
        updateInstance(Iterables.getLast(changes).getRootNode().getDataAfter());
    }

    @Holding("this")
    private void updateInstance(final PasswordServiceConfig config) {
        if (registration != null) {
            final ComponentInstance<?> newInstance = configFactory.newInstance(
                OSGiPasswordServiceConfig.props(config != null ? config : new PasswordServiceConfigBuilder().build()));
            if (instance != null) {
                instance.dispose();
            }
            instance = newInstance;
        }
    }
}
