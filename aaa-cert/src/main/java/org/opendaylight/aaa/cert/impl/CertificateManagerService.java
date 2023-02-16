/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.AaaCertRpcService;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * CertificateManagerService implements ICertificateManager and work as adapter
 * to which AaaCertProvider is used.
 *
 * @author mserngawy
 */
public class CertificateManagerService implements ICertificateManager, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateManagerService.class);
    private static final String DEFAULT_CONFIG_FILE_PATH = "etc" + File.separator + "opendaylight" + File.separator
            + "datastore" + File.separator + "initial" + File.separator + "config" + File.separator
            + "aaa-cert-config.xml";
    private static final int PWD_LENGTH = 12;

    private final IAaaCertProvider aaaCertProvider;
    private final Registration reg;

    public CertificateManagerService(final RpcProviderService rpcProviderService, final DataBroker dataBroker,
            final AAAEncryptionService encryptionSrv, AaaCertServiceConfig aaaCertServiceConfig) {
        if (aaaCertServiceConfig == null) {
            throw new IllegalArgumentException("Certificate Manager service configuration is null");
        }
        if (aaaCertServiceConfig.getUseConfig()) {
            if (aaaCertServiceConfig.getCtlKeystore() != null
                    && aaaCertServiceConfig.getCtlKeystore().getStorePassword() != null
                    && aaaCertServiceConfig.getCtlKeystore().getStorePassword().isEmpty()) {
                LOG.debug("Set keystores password");
                final String ctlPwd = RandomStringUtils.random(PWD_LENGTH, true, true);
                final String trustPwd = RandomStringUtils.random(PWD_LENGTH, true, true);
                updateCertManagerSrvConfig(ctlPwd, trustPwd);
                final CtlKeystore ctlKeystore = new CtlKeystoreBuilder(aaaCertServiceConfig.getCtlKeystore())
                        .setStorePassword(ctlPwd).build();
                final TrustKeystore trustKeystore = new TrustKeystoreBuilder(aaaCertServiceConfig.getTrustKeystore())
                        .setStorePassword(trustPwd).build();
                aaaCertServiceConfig = new AaaCertServiceConfigBuilder(aaaCertServiceConfig).setCtlKeystore(ctlKeystore)
                        .setTrustKeystore(trustKeystore).build();
            }
            if (aaaCertServiceConfig.getUseMdsal()) {
                aaaCertProvider = new DefaultMdsalSslData(new AaaCertMdsalProvider(dataBroker, encryptionSrv),
                        aaaCertServiceConfig.getBundleName(), aaaCertServiceConfig.getCtlKeystore(),
                        aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default mdsal SslData as aaaCertProvider");
            } else {
                aaaCertProvider = new AaaCertProvider(aaaCertServiceConfig.getCtlKeystore(),
                        aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default keystore files as aaaCertProvider");
            }
            aaaCertProvider.createKeyStores();
            LOG.info("Certificate Manager service has been initialized");
            reg = rpcProviderService.registerRpcImplementation(AaaCertRpcService.class,
                new AaaCertRpcServiceImpl(aaaCertProvider));
            LOG.info("AaaCert Rpc Service has been initialized");
        } else {
            aaaCertProvider = null;
            reg = null;
            LOG.info("Certificate Manager service has not been initialized, change the initial aaa-cert-config data "
                + "and restart Opendaylight");
        }
    }

    @Override
    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public KeyStore getODLKeyStore() {
        return aaaCertProvider.getODLKeyStore();
    }

    @Override
    public KeyStore getTrustKeyStore() {
        return aaaCertProvider.getTrustKeyStore();
    }

    @Override
    public String[] getCipherSuites() {
        return aaaCertProvider.getCipherSuites();
    }

    @Override
    public String getCertificateTrustStore(final String storePasswd, final String alias, final boolean withTag) {
        return aaaCertProvider.getCertificateTrustStore(storePasswd, alias, withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(final String storePasswd, final boolean withTag) {
        return aaaCertProvider.getODLKeyStoreCertificate(storePasswd, withTag);
    }

    @Override
    public String genODLKeyStoreCertificateReq(final String storePasswd, final boolean withTag) {
        return aaaCertProvider.genODLKeyStoreCertificateReq(storePasswd, withTag);
    }

    @Override
    public SSLContext getServerContext() {
        SSLContext serverContext = null;
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(aaaCertProvider.getODLKeyStore(),
                    aaaCertProvider.getOdlKeyStoreInfo().getStorePassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(aaaCertProvider.getTrustKeyStore());

            serverContext = SSLContext.getInstance("TLS");
            serverContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (final NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException
                | KeyManagementException e) {
            LOG.error("Error while creating SSLContext ", e);
        }
        return serverContext;
    }

    @Override
    public String[] getTlsProtocols() {
        return aaaCertProvider.getTlsProtocols();
    }

    @Override
    public boolean importSslDataKeystores(final String odlKeystoreName, final String odlKeystorePwd,
            final String odlKeystoreAlias, final String trustKeystoreName, final String trustKeystorePwd,
            final String[] cipherSuites, final String tlsProtocols) {
        DefaultMdsalSslData mdsalCertProvider = (DefaultMdsalSslData) aaaCertProvider;
        if (mdsalCertProvider == null) {
            LOG.debug("aaaCertProvider is not MD-Sal Certificate Provider");
            return false;
        }
        return mdsalCertProvider.importSslDataKeystores(odlKeystoreName, odlKeystorePwd, odlKeystoreAlias,
                trustKeystoreName, trustKeystorePwd, cipherSuites, tlsProtocols);
    }

    @Override
    public void exportSslDataKeystores() {
        DefaultMdsalSslData mdsalCertProvider = (DefaultMdsalSslData) aaaCertProvider;
        if (mdsalCertProvider == null) {
            LOG.debug("aaaCertProvider is not MD-Sal Certificate Provider");
            return;
        }
        mdsalCertProvider.exportSslDataKeystores();
    }

    private static void updateCertManagerSrvConfig(final String ctlPwd, final String trustPwd) {
        try {
            LOG.debug("Update Certificate manager service config file");
            final File configFile = new File(DEFAULT_CONFIG_FILE_PATH);
            if (configFile.exists()) {
                final String storePwdTag = "store-password";
                final String ctlStoreTag = "ctlKeystore";
                final String trustStoreTag = "trustKeystore";
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                final Document doc = docBuilder.parse(configFile);
                final NodeList ndList = doc.getElementsByTagName(storePwdTag);
                for (int i = 0; i < ndList.getLength(); i++) {
                    final Node nd = ndList.item(i);
                    if (nd.getParentNode() != null && nd.getParentNode().getNodeName().equals(ctlStoreTag)) {
                        nd.setTextContent(ctlPwd);
                    } else if (nd.getParentNode() != null && nd.getParentNode().getNodeName().equals(trustStoreTag)) {
                        nd.setTextContent(trustPwd);
                    }
                }
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                final Transformer transformer = transformerFactory.newTransformer();
                final DOMSource source = new DOMSource(doc);
                final StreamResult result = new StreamResult(new File(DEFAULT_CONFIG_FILE_PATH));
                transformer.transform(source, result);
            } else {
                LOG.warn("The Certificate manager service config file does not exist {}", DEFAULT_CONFIG_FILE_PATH);
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
            LOG.error("Error while updating Certificate manager service config file", e);
        }
    }
}
