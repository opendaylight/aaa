/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Intermediate component dealing with establishing initial configuration for {@link AAAEncryptionServiceImpl}. In
 * particular it deals with generating and persisting of encryption salt and encryption password.
 */
public final class AAAEncryptionServiceConfigurator implements EncryptServiceConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptionServiceConfigurator.class);

    // Note: this is a strong binding to Blueprint, which is loading etc/opendaylight/datastore/initial/config
    private static final String DEFAULT_CONFIG_FILE_PATH = "etc" + File.separator + "opendaylight" + File.separator
        + "datastore" + File.separator + "initial" + File.separator + "config" + File.separator
        + "aaa-encrypt-service-config.xml";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EncryptServiceConfig delegate;

    public AAAEncryptionServiceConfigurator(final DataBroker dataBroker,
            final AaaEncryptServiceConfig blueprintConfig) {
        if (Strings.isNullOrEmpty(blueprintConfig.getEncryptSalt())
            || Strings.isNullOrEmpty(blueprintConfig.getEncryptKey())) {
            final var generatedConfig = generateConfig(blueprintConfig);

            // Update initial configuration and config datastore
            updateEncrySrvConfig(generatedConfig.requireEncryptKey(), generatedConfig.requireEncryptSalt());
            updateDatastore(dataBroker, generatedConfig);

            delegate = generatedConfig;
        } else {
            delegate = blueprintConfig;
        }
    }

    private static @NonNull AaaEncryptServiceConfig generateConfig(final EncryptServiceConfig blueprintConfig) {
        LOG.debug("Set the Encryption service password and encrypt salt");
        final var salt = new byte[16];
        RANDOM.nextBytes(salt);

        return new AaaEncryptServiceConfigBuilder(blueprintConfig)
            .setEncryptKey(RandomStringUtils.random(blueprintConfig.requirePasswordLength(), true, true))
            .setEncryptSalt(Base64.getEncoder().encodeToString(salt))
            .build();
    }

    // FIXME: Update configuration datastore, but only if not present?! this looks weird lifecycle-wise
    private static void updateDatastore(final DataBroker dataBroker, final @NonNull AaaEncryptServiceConfig config) {
        final var iid = InstanceIdentifier.create(AaaEncryptServiceConfig.class);

        final var tx = dataBroker.newReadWriteTransaction();
        final var existsFuture = tx.exists(LogicalDatastoreType.CONFIGURATION, iid);

        final boolean exists;
        try {
            exists = existsFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            tx.cancel();
            LOG.error("Failed to read configuration", e);
            return;
        }
        if (exists) {
            tx.cancel();
            LOG.info("Configuration already present, skipping update");
            return;
        }

        LOG.debug("Populating configuration: {}, {}", iid, config);
        tx.put(LogicalDatastoreType.CONFIGURATION, iid, config);
        // Perform the transaction.submit asynchronously
        Futures.addCallback(tx.commit(), new FutureCallback<CommitInfo>() {
            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("initDatastore: configuration data populated: {}", iid);
            }

            @Override
            public void onSuccess(final CommitInfo result) {
                LOG.info("initDatastore: transaction succeeded");
            }
        }, MoreExecutors.directExecutor());
    }

    private static void updateEncrySrvConfig(final String newPwd, final String newSalt) {
        LOG.debug("Update encryption service config file");
        try {
            final File configFile = new File(DEFAULT_CONFIG_FILE_PATH);
            if (configFile.exists()) {
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
                final Node keyNode = doc.getElementsByTagName("encrypt-key").item(0);
                keyNode.setTextContent(newPwd);
                final Node salt = doc.getElementsByTagName("encrypt-salt").item(0);
                salt.setTextContent(newSalt);
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc),
                    new StreamResult(new File(DEFAULT_CONFIG_FILE_PATH)));
            } else {
                LOG.warn("The encryption service config file does not exist {}", DEFAULT_CONFIG_FILE_PATH);
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
            LOG.error("Error while updating the encryption service config file", e);
        }
    }

    @Override
    public Class<? extends EncryptServiceConfig> implementedInterface() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEncryptKey() {
        return delegate.getEncryptKey();
    }

    @Override
    public Integer getPasswordLength() {
        return delegate.getPasswordLength();
    }

    @Override
    public String getEncryptSalt() {
        return delegate.getEncryptSalt();
    }

    @Override
    public String getEncryptMethod() {
        return delegate.getEncryptMethod();
    }

    @Override
    public String getEncryptType() {
        return delegate.getEncryptType();
    }

    @Override
    public Integer getEncryptIterationCount() {
        return delegate.getEncryptIterationCount();
    }

    @Override
    public Integer getEncryptKeyLength() {
        return delegate.getEncryptKeyLength();
    }

    @Override
    public String getCipherTransforms() {
        return delegate.getCipherTransforms();
    }
}
