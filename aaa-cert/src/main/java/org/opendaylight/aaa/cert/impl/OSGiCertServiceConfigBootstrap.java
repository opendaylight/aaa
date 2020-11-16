/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
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
public final class OSGiCertServiceConfigBootstrap implements ClusteredDataTreeChangeListener<AaaCertServiceConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiCertServiceConfigBootstrap.class);

    @Reference
    DataBroker dataBroker = null;

    @Reference(target = "(component.factory=" + OSGiCertServiceConfig.FACTORY_NAME + ")")
    ComponentFactory configFactory = null;

    private ListenerRegistration<?> registration;
    private ComponentInstance instance;

    @Activate
    synchronized void activate() {
        registration = dataBroker.registerDataTreeChangeListener(
                DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                        InstanceIdentifier.create(AaaCertServiceConfig.class)), this);
        LOG.info("Listening for aaa certificate service configuration");
    }

    @Deactivate
    synchronized void deactivate() {
        registration.close();
        registration = null;
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
        LOG.info("No longer listening for aaa certificate service configuration");
    }

    @Override
    public synchronized void onInitialData() {
        updateInstance(null);
    }

    @Override
    public synchronized void onDataTreeChanged(
            @NonNull Collection<DataTreeModification<AaaCertServiceConfig>> changes) {
        updateInstance(Iterables.getLast(changes).getRootNode().getDataAfter());
    }

    @Holding("this")
    private void updateInstance(AaaCertServiceConfig config) {
        if (registration != null) {
            final ComponentInstance newInstance =
                    configFactory.newInstance(OSGiCertServiceConfig.props(withDefaults(config)));
            if (instance != null) {
                instance.dispose();
            }
            instance = newInstance;
        }
    }

    private AaaCertServiceConfig withDefaults(final AaaCertServiceConfig config) {
        // FIXME load the xml and fill with that
        AaaCertServiceConfigBuilder builder = config != null
                ? new AaaCertServiceConfigBuilder(config) : new AaaCertServiceConfigBuilder();

        if (builder.getUseConfig() == null) {
            builder.setUseConfig(true);
        }
        if (builder.getUseMdsal() == null) {
            builder.setUseMdsal(true);
        }

        if (Strings.isNullOrEmpty(builder.getBundleName())) {
            builder.setBundleName("opendaylight");
        }

        builder.setCtlKeystore(withDefaults(builder.getCtlKeystore()));
        builder.setTrustKeystore(withDefaults(builder.getTrustKeystore()));

        return builder.build();
    }

    private CtlKeystore withDefaults(final CtlKeystore ctlKeystore) {
        final CtlKeystoreBuilder builder = ctlKeystore != null
                ? new CtlKeystoreBuilder(ctlKeystore) : new CtlKeystoreBuilder();

        if (Strings.isNullOrEmpty(builder.getName())) {
            builder.setName("ctl.jks");
        }
        if (Strings.isNullOrEmpty(builder.getAlias())) {
            builder.setAlias("controller");
        }
        if (Strings.isNullOrEmpty(builder.getStorePassword())) {
            builder.setStorePassword("");
        }
        if (Strings.isNullOrEmpty(builder.getDname())) {
            builder.setDname("CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA");
        }
        if (Strings.isNullOrEmpty(builder.getTlsProtocols())) {
            builder.setTlsProtocols("");
        }
        if (Strings.isNullOrEmpty(builder.getKeyAlg())) {
            builder.setKeyAlg("RSA");
        }
        if (Strings.isNullOrEmpty(builder.getSignAlg())) {
            builder.setSignAlg("SHA1WithRSAEncryption");
        }

        if (builder.getKeysize() == null) {
            builder.setKeysize(1024);
        }
        if (builder.getValidity() == null) {
            builder.setValidity(365);
        }
        if (builder.getCipherSuites() == null) {
            builder.setCipherSuites(new ArrayList<>());
        }
        return builder.build();
    }

    private TrustKeystore withDefaults(final TrustKeystore trustKeystore) {
        final TrustKeystoreBuilder builder = trustKeystore != null
                ? new TrustKeystoreBuilder(trustKeystore) : new TrustKeystoreBuilder();

        if (Strings.isNullOrEmpty(builder.getName())) {
            builder.setName("truststore.jks");
        }

        if (builder.getStorePassword() == null) {
            builder.setStorePassword("");
        }

        return builder.build();
    }
}
