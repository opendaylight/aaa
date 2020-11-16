/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ForwardingObject;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true,
           service = ICertificateManager.class,
           property = "type=default-certificate-manager")
public final class OSGiCertServiceProvider extends ForwardingObject implements ICertificateManager {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiCertServiceProvider.class);
    private final DataBroker dataBroker;
    private final AAAEncryptionService encryptionService;

    private volatile CertificateManagerService certService;

    @Activate
    public OSGiCertServiceProvider(@Reference final DataBroker dataBroker,
            @Reference final AAAEncryptionService encryptionService) {
        this.dataBroker = dataBroker;
        this.encryptionService = encryptionService;
    }

    @Reference
    void init(final AaaCertServiceConfig certServiceConfig) {
        if (dataBroker != null && encryptionService != null) {
            certService = new CertificateManagerService(certServiceConfig, dataBroker, encryptionService);
            LOG.info("CertificateManagerService initialized");
        }
    }

    @Override
    public KeyStore getODLKeyStore() {
        return delegate().getODLKeyStore();
    }

    @Override
    public KeyStore getTrustKeyStore() {
        return delegate().getTrustKeyStore();
    }

    @Override
    public String[] getCipherSuites() {
        return delegate().getCipherSuites();
    }

    @Override
    public String[] getTlsProtocols() {
        return delegate().getTlsProtocols();
    }

    @Override
    public @NonNull String getCertificateTrustStore(
            final @NonNull String storePasswd, final @NonNull String alias, final boolean withTag) {
        return delegate().getCertificateTrustStore(storePasswd, alias, withTag);
    }

    @Override
    public @NonNull String getODLKeyStoreCertificate(final @NonNull String storePasswd, final boolean withTag) {
        return delegate().getODLKeyStoreCertificate(storePasswd, withTag);
    }

    @Override
    public @NonNull String genODLKeyStoreCertificateReq(final @NonNull String storePasswd, final boolean withTag) {
        return delegate().genODLKeyStoreCertificateReq(storePasswd, withTag);
    }

    @Override
    public SSLContext getServerContext() {
        return delegate().getServerContext();
    }

    @Override
    public boolean importSslDataKeystores(final @NonNull String odlKeystoreName, final @NonNull String odlKeystorePwd,
            final @NonNull String odlKeystoreAlias, final @NonNull String trustKeystoreName,
            final @NonNull String trustKeystorePwd, final @NonNull String[] cipherSuites,
            final @NonNull String tlsProtocols) {
        return delegate().importSslDataKeystores(odlKeystoreName, odlKeystorePwd, odlKeystoreAlias, trustKeystoreName,
                trustKeystorePwd, cipherSuites, tlsProtocols);
    }

    @Override
    public void exportSslDataKeystores() {
        delegate().exportSslDataKeystores();
    }

    @Override
    protected CertificateManagerService delegate() {
        return verifyNotNull(certService);
    }
}
