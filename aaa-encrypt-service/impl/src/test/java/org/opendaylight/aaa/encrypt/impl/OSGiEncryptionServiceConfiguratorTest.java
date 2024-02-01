/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.Base64;
import java.util.Dictionary;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

@ExtendWith(MockitoExtension.class)
class OSGiEncryptionServiceConfiguratorTest {
    private static final @NonNull InstanceIdentifier<AaaEncryptServiceConfig> IID =
        InstanceIdentifier.create(AaaEncryptServiceConfig.class);
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ComponentFactory<AAAEncryptionServiceImpl> factory;
    @Mock
    private ComponentInstance<AAAEncryptionServiceImpl> instance;
    @Mock
    private Registration registration;
    @Mock
    private ReadWriteTransaction transaction;
    @Captor
    private ArgumentCaptor<DataTreeIdentifier<AaaEncryptServiceConfig>> treeIdCaptor;
    @Captor
    private ArgumentCaptor<DataListener<AaaEncryptServiceConfig>> listenerCaptor;
    @Captor
    private ArgumentCaptor<AaaEncryptServiceConfig> configCaptor;
    @Captor
    private ArgumentCaptor<Dictionary<String, ?>> propertiesCaptor;

    private OSGiEncryptionServiceConfigurator configurator;

    @BeforeEach
    void before() {
        doReturn(registration).when(dataBroker).registerDataListener(treeIdCaptor.capture(), listenerCaptor.capture());

        configurator = new OSGiEncryptionServiceConfigurator(dataBroker, factory);

        assertEquals(DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, IID), treeIdCaptor.getValue());
        assertSame(configurator, listenerCaptor.getValue());
    }

    @Test
    void testImmediateDeactivate() {
        doNothing().when(registration).close();
        configurator.deactivate();
    }

    @Test
    void testEmptyDatastore() {
        // Config datastore write is expected: capture what gets written
        doReturn(transaction).when(dataBroker).newReadWriteTransaction();
        doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(transaction)
            .read(LogicalDatastoreType.CONFIGURATION, IID);
        doNothing().when(transaction).put(eq(LogicalDatastoreType.CONFIGURATION), eq(IID), configCaptor.capture());
        doReturn(CommitInfo.emptyFluentFuture()).when(transaction).commit();

        configurator.dataChangedTo(null);

        final var config = configCaptor.getValue();
        assertEquals("AES/MCG/NoPadding", config.getCipherTransforms());
        assertEquals(Integer.valueOf(32768), config.getEncryptIterationCount());
        assertEquals(Integer.valueOf(128), config.getEncryptKeyLength());
        assertEquals("PBKDF2WithHmacSHA1", config.getEncryptMethod());
        assertEquals("AES", config.getEncryptType());
        assertEquals(Integer.valueOf(12), config.getPasswordLength());
        assertEquals(Integer.valueOf(128), config.requireAuthTagLength());

        final var salt = Base64.getDecoder().decode(config.getEncryptSalt());
        assertEquals(16, salt.length);

        final var key = config.getEncryptKey();
        assertNotNull(key);
        assertEquals(12, key.length());

        // Now we circle around are report that config. We expect the factory to be called
        doReturn(instance).when(factory).newInstance(propertiesCaptor.capture());

        configurator.dataChangedTo(config);

        final var props = propertiesCaptor.getValue();
        assertNotNull(props);
        assertEquals(1, props.size());
        final var serviceConfig = assertInstanceOf(EncryptServiceConfig.class, props.elements().nextElement());
        assertArrayEquals(salt, serviceConfig.getEncryptSalt());
        assertEquals(key, serviceConfig.getEncryptKey());

        // Now shut down configurator
        doNothing().when(registration).close();
        doNothing().when(instance).dispose();
        configurator.deactivate();
    }
}
