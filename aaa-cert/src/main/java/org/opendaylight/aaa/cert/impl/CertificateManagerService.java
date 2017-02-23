/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
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
import java.security.Security;
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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * CertificateManagerService implements ICertificateManager and work as adapter to which AaaCertProvider is used.
 *
 * @author mserngawy
 *
 */
public class CertificateManagerService implements ICertificateManager {

    private final static Logger LOG = LoggerFactory.getLogger(CertificateManagerService.class);

    private final String DEFAULT_CONFIG_FILE_PATH = "etc" + File.separator + "opendaylight" + File.separator
            + "datastore" + File.separator + "initial" + File.separator + "config" + File.separator + "aaa-cert-config.xml";
    private final int pwdLenght = 12;
    private final IAaaCertProvider aaaCertProvider;

    public CertificateManagerService(AaaCertServiceConfig aaaCertServiceConfig, final DataBroker dataBroker, final AAAEncryptionService encryptionSrv) {
       if (aaaCertServiceConfig.isUseConfig()) {
            if (aaaCertServiceConfig.getCtlKeystore().getStorePassword().isEmpty()) {
                LOG.debug("Set the Encryption service password ");
                final String ctlPwd = RandomStringUtils.random(pwdLenght, true, true);
                final String trustPwd = RandomStringUtils.random(pwdLenght, true, true);
                updateCertManagerSrvConfig(ctlPwd, trustPwd);
                final CtlKeystore ctlKeystore = new CtlKeystoreBuilder(aaaCertServiceConfig.getCtlKeystore())
                        .setStorePassword(ctlPwd).build();
                final TrustKeystore trustKeystore = new TrustKeystoreBuilder(aaaCertServiceConfig.getTrustKeystore())
                        .setStorePassword(trustPwd).build();
                aaaCertServiceConfig = new AaaCertServiceConfigBuilder(aaaCertServiceConfig)
                        .setCtlKeystore(ctlKeystore)
                        .setTrustKeystore(trustKeystore)
                        .build();
            }
            if (aaaCertServiceConfig.isUseMdsal()) {
                aaaCertProvider = new DefaultMdsalSslData(new AaaCertMdsalProvider(dataBroker, encryptionSrv), aaaCertServiceConfig.getBundleName(),
                        aaaCertServiceConfig.getCtlKeystore(), aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default mdsal SslData as aaaCertProvider");
            } else {
                aaaCertProvider = new AaaCertProvider(aaaCertServiceConfig.getCtlKeystore(), aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default keystore files as aaaCertProvider");
            }
            aaaCertProvider.createKeyStores();
            LOG.info("Certificate Manager service has been initialized");
        } else {
            aaaCertProvider = null;
            LOG.info("Certificate Manager service has not been initialized, change the initial aaa-cert-config data and restart Opendaylight");
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
    public String getCertificateTrustStore(String storePasswd, String alias, boolean withTag) {
        return aaaCertProvider.getCertificateTrustStore(storePasswd, alias, withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(String storePasswd, boolean withTag) {
        return aaaCertProvider.getODLKeyStoreCertificate(storePasswd, withTag);
    }

    @Override
    public String genODLKeyStoreCertificateReq(String storePasswd, boolean withTag) {
        return aaaCertProvider.genODLKeyStoreCertificateReq(storePasswd, withTag);
    }

    @Override
    public SSLContext getServerContext() {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        SSLContext serverContext = null;
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(aaaCertProvider.getODLKeyStore(), aaaCertProvider.getOdlKeyStoreInfo().getStorePassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(aaaCertProvider.getTrustKeyStore());

            serverContext = SSLContext.getInstance(KeyStoreConstant.TLS_PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (final NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException  e) {
            LOG.error("Error while creating SSLContext ", e);
        }
        return serverContext;
    }

    @Override
    public String[] getTlsProtocols() {
        return aaaCertProvider.getTlsProtocols();
    }

    private void updateCertManagerSrvConfig(String ctlPwd, String trustPwd) {
        try {
            LOG.debug("Update encryption service config file");
            final File configFile = new File(DEFAULT_CONFIG_FILE_PATH);
            if (configFile.exists()) {
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                final Document doc = docBuilder.parse(configFile);
                final NodeList ndList = doc.getElementsByTagName("store-password");
                final Node ctlkey = ndList.item(0);
                ctlkey.setTextContent(ctlPwd);
                final Node trustkey = ndList.item(1);
                trustkey.setTextContent(trustPwd);
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                final Transformer transformer = transformerFactory.newTransformer();
                final DOMSource source = new DOMSource(doc);
                final StreamResult result = new StreamResult(new File(DEFAULT_CONFIG_FILE_PATH));
                transformer.transform(source, result);
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
            LOG.error("Error while update encryption service config file ", e);
        }
    }

}
