/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.RandomStringUtils;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.odlparent.logging.markers.Markers;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intermediate component dealing with establishing initial configuration for {@link AAAEncryptionServiceImpl}. In
 * particular it deals with generating and persisting of encryption salt and encryption password.
 *
 * <p>
 * We primarily listen to the configuration being present. Whenever the salt is missing or the password does not match
 * the required length, we generate them and persist them. This mode of operation means we potentially have a loop, i.e.
 * our touching the datastore will trigger again {@link #onDataTreeChanged(Collection)}, which will re-evaluate the
 * conditions and we try again.
 */
@Component(service = { })
public final class OSGiEncryptionServiceConfigurator implements DataTreeChangeListener<AaaEncryptServiceConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiEncryptionServiceConfigurator.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final @NonNull AaaEncryptServiceConfig DEFAULT_CONFIG = new AaaEncryptServiceConfigBuilder()
        // Note: mirrors defaults from YANG file
        .setEncryptMethod("PBKDF2WithHmacSHA1")
        .setEncryptType("AES")
        .setEncryptIterationCount(32768)
        .setEncryptKeyLength(128)
        .setCipherTransforms("AES/CBC/PKCS5Padding")
        .setPasswordLength(12)
        .build();

    private final ComponentFactory<AAAEncryptionServiceImpl> factory;
    private final DataBroker dataBroker;

    @GuardedBy("this")
    private Registration reg;
    @GuardedBy("this")
    private ComponentInstance<AAAEncryptionServiceImpl> instance;
    @GuardedBy("this")
    private AaaEncryptServiceConfig current;

    @Activate
    public OSGiEncryptionServiceConfigurator(@Reference final DataBroker dataBroker,
            @Reference(target = "(component.factory=" + AAAEncryptionServiceImpl.FACTORY_NAME + ")")
            final ComponentFactory<AAAEncryptionServiceImpl> factory) {
        this.dataBroker = requireNonNull(dataBroker);
        this.factory = requireNonNull(factory);
        reg = dataBroker.registerDataTreeChangeListener(
            DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(AaaEncryptServiceConfig.class)),
            this);
        LOG.debug("AAA Encryption Service configurator started");
    }

    @Deactivate
    public synchronized void deactivate() {
        reg.close();
        reg = null;
        disableInstance();
        LOG.debug("AAA Encryption Service configurator stopped");
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<AaaEncryptServiceConfig>> changes) {
        // Acquire the last reported configuration and check if it needs to have salt/password generated.
        final var dsConfig = Iterables.getLast(changes).getRootNode().getDataAfter();
        if (dsConfig == null || needKey(dsConfig) || needSalt(dsConfig)) {
            // Generate salt/key as needed and persist it -- causing us to be re-invoked later.
            updateDatastore(dsConfig);
        } else {
            // Configuration is self-consistent, proceed to activate an instance based on it
            updateInstance(dsConfig);
        }
    }

    @Override
    public void onInitialData() {
        updateDatastore(null);
    }

    @VisibleForTesting
    static @NonNull AaaEncryptServiceConfig generateConfig(final @Nullable AaaEncryptServiceConfig datastoreConfig) {
        // Select template and decide which parts need to be updated
        final var template = datastoreConfig != null ? datastoreConfig : DEFAULT_CONFIG;
        final var builder = new AaaEncryptServiceConfigBuilder(template);
        if (needKey(template)) {
            LOG.debug("Set the Encryption Service salt");
            builder.setEncryptKey(RandomStringUtils.random(template.requirePasswordLength(), true, true));
        }
        if (needSalt(template)) {
            LOG.debug("Set the Encryption Service salt");
            final var salt = new byte[16];
            RANDOM.nextBytes(salt);
            builder.setEncryptSalt(Base64.getEncoder().encodeToString(salt));
        }
        return builder.build();
    }

    private void updateDatastore(final @Nullable AaaEncryptServiceConfig expected) {
        final var target = generateConfig(expected);

        // Careful update of the datastore: we are coming from DTCL thread, so inherently 'expected' may already be out
        // of date, either by user action, or our update from another node (in a cluster). We rely on transaction's
        // read&put atomicity to do the right thing here.
        final var iid = InstanceIdentifier.create(AaaEncryptServiceConfig.class);
        final var tx = dataBroker.newReadWriteTransaction();
        final var readFuture = tx.read(LogicalDatastoreType.CONFIGURATION, iid);

        final AaaEncryptServiceConfig actual;
        try {
            actual = readFuture.get().orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            // Read failed: all we can do now is to disable the service and hope for an external recovery action -- like
            // a restart of this component or a write to the datastore (which will trigger a retry).
            tx.cancel();
            LOG.error("Failed to read configuration, disabling service", e);
            synchronized (this) {
                disableInstance();
            }
            return;
        }

        if (!Objects.equals(actual, expected)) {
            // Yup, there has been a race -- log that fact and bail out
            tx.cancel();
            LOG.debug(Markers.confidential(), "Skipping update on datastore mismatch: expected {} actual {}",
                expected, actual);
            return;
        }

        LOG.debug(Markers.confidential(), "Updating configuration to {}", target);
        tx.put(LogicalDatastoreType.CONFIGURATION, iid, target);
        Futures.addCallback(tx.commit(), new FutureCallback<CommitInfo>() {
            @Override
            public void onFailure(final Throwable throwable) {
                // Async update: we should get a new onDataTreeChanged() callback
                LOG.warn("Configuration update failed, attempting to continue", throwable);
            }

            @Override
            public void onSuccess(final CommitInfo result) {
                LOG.info("Configuration update succeeded");
            }
        }, MoreExecutors.directExecutor());
    }

    @Holding("this")
    private void disableInstance() {
        if (instance != null) {
            instance.dispose();
            instance = null;
            current = null;
            LOG.info("Encryption Service disabled");
        }
    }

    private synchronized void updateInstance(final AaaEncryptServiceConfig newConfig) {
        if (reg == null) {
            LOG.debug("Skipping instance update due to shutdown");
            return;
        }
        if (newConfig.equals(current)) {
            LOG.debug("Skipping instance update due to equal configuration");
            return;
        }

        disableInstance();
        instance = factory.newInstance(FrameworkUtil.asDictionary(
            AAAEncryptionServiceImpl.props(new EncryptServiceConfigImpl(newConfig))));
        current = newConfig;
        LOG.info("Encryption Service enabled");
    }

    private static boolean needKey(final AaaEncryptServiceConfig config) {
        final var key = config.getEncryptKey();
        return key == null || key.length() != config.requirePasswordLength();
    }

    private static boolean needSalt(final AaaEncryptServiceConfig config) {
        final var salt = config.getEncryptSalt();
        return salt == null || salt.isEmpty();
    }
}
