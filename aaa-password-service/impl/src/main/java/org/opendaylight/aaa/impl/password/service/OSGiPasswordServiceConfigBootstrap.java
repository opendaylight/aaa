/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.password.service.config.rev170619.PasswordServiceConfigBuilder;
import org.opendaylight.yangtools.concepts.Registration;
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
@Component(service = { })
public final class OSGiPasswordServiceConfigBootstrap implements DataListener<PasswordServiceConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiPasswordServiceConfigBootstrap.class);

    private final ComponentFactory<OSGiPasswordServiceConfig> configFactory;
    private Registration registration;
    private ComponentInstance<?> instance;

    @Activate
    public OSGiPasswordServiceConfigBootstrap(@Reference final DataBroker dataBroker,
            @Reference(target = "(component.factory=" + OSGiPasswordServiceConfig.FACTORY_NAME + ")")
            final ComponentFactory<OSGiPasswordServiceConfig> configFactory) {
        this.configFactory  = requireNonNull(configFactory);
        registration = dataBroker.registerDataListener(
            DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
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
    public synchronized void dataChangedTo(final PasswordServiceConfig data) {
        // FIXME: at this point we need to populate default values -- from the XML file
        if (registration != null) {
            final var newInstance = configFactory.newInstance(
                OSGiPasswordServiceConfig.props(data != null ? data : new PasswordServiceConfigBuilder().build()));
            if (instance != null) {
                instance.dispose();
            }
            instance = newInstance;
        }
    }
}
