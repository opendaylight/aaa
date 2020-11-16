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
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.ctlkeystore.CipherSuitesBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
@Designate(ocd = OSGIAaaCertServiceConfig.class)
public final class OSGiCertServiceConfigBootstrap implements ClusteredDataTreeChangeListener<AaaCertServiceConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiCertServiceConfigBootstrap.class);

    private final DataBroker dataBroker;
    private final AaaCertServiceConfig defaultConfig;

    @Reference(target = "(component.factory=" + OSGiCertServiceConfig.FACTORY_NAME + ")")
    private ComponentFactory configFactory;
    private ListenerRegistration<?> registration;
    private ComponentInstance instance;

    @Activate
    public OSGiCertServiceConfigBootstrap(@Reference final DataBroker dataBroker,
            final OSGIAaaCertServiceConfig config) {
        this.dataBroker = requireNonNull(dataBroker);
        this.defaultConfig = buildDefaultConfig(requireNonNull(config));
    }

    @Activate
    synchronized void activate() {
        LOG.info("Initializing OSGiCertServiceConfigBootstrap...");
        registration = verifyNotNull(dataBroker).registerDataTreeChangeListener(
                DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                        InstanceIdentifier.create(AaaCertServiceConfig.class)), this);
        LOG.info("Listening for aaa certificate service configuration");
    }

    @Deactivate
    synchronized void close() {
        LOG.info("Closing OSGiCertServiceConfigBootstrap...");
        if (registration != null) {
            registration.close();
        }
        if (instance != null) {
            instance.dispose();
        }
        LOG.info("No longer listening for aaa certificate service configuration");
    }

    @Override
    public synchronized void onInitialData() {
        updateInstance(null);
    }

    @Override
    public synchronized void onDataTreeChanged(
            final @NonNull Collection<DataTreeModification<AaaCertServiceConfig>> changes) {
        updateInstance(Iterables.getLast(changes).getRootNode().getDataAfter());
    }

    @Holding("this")
    private void updateInstance(final AaaCertServiceConfig config) {
        final Dictionary<String, AaaCertServiceConfig> props;
        if (config == null) {
            props = OSGiCertServiceConfig.props(defaultConfig);
        } else {
            props = OSGiCertServiceConfig.props(config);
        }
        final ComponentInstance newInstance = configFactory.newInstance(props);
        if (instance != null) {
            instance.dispose();
        }
        instance = newInstance;
    }

    private static AaaCertServiceConfig buildDefaultConfig(final OSGIAaaCertServiceConfig config) {
        final var ctlKeystore = new CtlKeystoreBuilder()
                .setName(config.ctlKeystoreName())
                .setAlias(config.ctlKeystoreAlias())
                .setStorePassword(config.ctlKeystoreStorePassword())
                .setDname(config.ctlKeystoreDname())
                .setValidity(config.ctlKeystoreValidity())
                .setKeyAlg(config.ctlKeystoreKeyAlg())
                .setSignAlg(config.ctlKeystoreSignAlg())
                .setKeysize(config.ctlKeystoreKeysize())
                .setTlsProtocols(config.ctlKeystoreTlsProtocol())
                .setCipherSuites(Arrays.stream(config.ctlKeystoreCipherSuites())
                        .map(t -> new CipherSuitesBuilder().setSuiteName(t).build())
                        .toList())
                .build();
        final var trustKeystore = new TrustKeystoreBuilder()
                .setName(config.trustKeystoreName())
                .setStorePassword(config.trustKeystoreStorePassword())
                .build();
        return new AaaCertServiceConfigBuilder()
                .setUseConfig(config.useConfig())
                .setUseMdsal(config.useMdsal())
                .setBundleName(config.bundleName())
                .setCtlKeystore(ctlKeystore)
                .setTrustKeystore(trustKeystore)
                .build();
    }
}
