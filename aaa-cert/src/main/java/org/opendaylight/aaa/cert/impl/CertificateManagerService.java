/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CertificateManagerService implements ICertificateManager and work as adapter to which AaaCertProvider is used.
 *
 * @author mserngawy
 *
 */
public class CertificateManagerService implements ICertificateManager {

    private final static Logger LOG = LoggerFactory.getLogger(CertificateManagerService.class);

    private final IAaaCertProvider aaaCertProvider;

    public CertificateManagerService(final AaaCertServiceConfig aaaCertServiceConfig, final DataBroker dataBroker, final AAAEncryptionService encryptionSrv) {
       if (aaaCertServiceConfig.isUseConfig()) {
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
}
