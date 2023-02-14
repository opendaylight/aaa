/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.Base64;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class OSGiEncryptionServiceConfiguratorTest {
    private static final @NonNull InstanceIdentifier<AaaEncryptServiceConfig> IID =
        InstanceIdentifier.create(AaaEncryptServiceConfig.class);

    @Mock
    private DataBroker dataBroker;
    @Mock
    private ComponentFactory<AAAEncryptionServiceImpl> factory;
    @Mock
    private ComponentInstance<AAAEncryptionServiceImpl> instance;
    @Mock
    private ListenerRegistration<?> registration;
    @Mock
    private ReadWriteTransaction transaction;
    @Mock
    private DataTreeModification<AaaEncryptServiceConfig> treeModification;
    @Mock
    private DataObjectModification<AaaEncryptServiceConfig> objectModification;
    @Captor
    private ArgumentCaptor<DataTreeIdentifier<AaaEncryptServiceConfig>> treeIdCaptor;
    @Captor
    private ArgumentCaptor<DataTreeChangeListener<AaaEncryptServiceConfig>> listenerCaptor;
    @Captor
    private ArgumentCaptor<AaaEncryptServiceConfig> configCaptor;
    @Captor
    private ArgumentCaptor<Dictionary<String, ?>> propertiesCaptor;

    private OSGiEncryptionServiceConfigurator configurator;

    @Before
    public void before() {
        doReturn(registration).when(dataBroker).registerDataTreeChangeListener(treeIdCaptor.capture(),
            listenerCaptor.capture());

        configurator = new OSGiEncryptionServiceConfigurator(dataBroker, factory);

        assertEquals(DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, IID), treeIdCaptor.getValue());
        assertSame(configurator, listenerCaptor.getValue());
    }

    @Test
    public void testImmediateDeactivate() {
        doNothing().when(registration).close();
        configurator.deactivate();
    }

    @Test
    public void testEmptyDatastore() {
        // Config datastore write is expected: capture what gets written
        doReturn(transaction).when(dataBroker).newReadWriteTransaction();
        doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(transaction)
            .read(LogicalDatastoreType.CONFIGURATION, IID);
        doNothing().when(transaction).put(eq(LogicalDatastoreType.CONFIGURATION), eq(IID), configCaptor.capture());
        doReturn(CommitInfo.emptyFluentFuture()).when(transaction).commit();

        configurator.onInitialData();

        final var config = configCaptor.getValue();
        assertEquals("AES/CBC/PKCS5Padding", config.getCipherTransforms());
        assertEquals(Integer.valueOf(32768), config.getEncryptIterationCount());
        assertEquals(Integer.valueOf(128), config.getEncryptKeyLength());
        assertEquals("PBKDF2WithHmacSHA1", config.getEncryptMethod());
        assertEquals("AES", config.getEncryptType());
        assertEquals(Integer.valueOf(12), config.getPasswordLength());

        final var salt = Base64.getDecoder().decode(config.getEncryptSalt());
        assertEquals(16, salt.length);

        final var key = config.getEncryptKey();
        assertNotNull(key);
        assertEquals(12, key.length());

        // Now we circle around are report that config. We expect the factory to be called
        doReturn(config).when(objectModification).getDataAfter();
        doReturn(objectModification).when(treeModification).getRootNode();
        doReturn(instance).when(factory).newInstance(propertiesCaptor.capture());

        configurator.onDataTreeChanged(List.of(treeModification));

        final var props = propertiesCaptor.getValue();
        assertNotNull(props);
        assertEquals(1, props.size());
        final var configObj = props.elements().nextElement();
        assertNotNull(configObj);
        assertThat(configObj, instanceOf(EncryptServiceConfig.class));
        final var serviceConfig = (EncryptServiceConfig) configObj;
        assertArrayEquals(salt, serviceConfig.getEncryptSalt());
        assertEquals(key, serviceConfig.getEncryptKey());

        // Now shut down configurator
        doNothing().when(registration).close();
        doNothing().when(instance).dispose();
        configurator.deactivate();
    }
}
