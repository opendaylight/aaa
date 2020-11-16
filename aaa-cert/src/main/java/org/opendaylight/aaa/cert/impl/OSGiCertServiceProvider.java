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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true,
           service = ICertificateManager.class,
           property = "type=default-certificate-manager")
public final class OSGiCertServiceProvider extends ForwardingObject implements ICertificateManager {

    @Reference
    private DataBroker dataBroker;

    @Reference
    private AAAEncryptionService encryptionService;

    volatile CertificateManagerService certService = null;

    private AaaCertServiceConfig serviceConfig;

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
    void bindConfig(AaaCertServiceConfig certServiceConfig) {
        updatedConfig(certServiceConfig);
    }

    void unbindConfig(AaaCertServiceConfig certServiceConfig) {
        certService = null;
    }

    void updatedConfig(AaaCertServiceConfig certServiceConfig) {
        if (dataBroker != null && encryptionService != null) {
            certService = new CertificateManagerService(certServiceConfig, dataBroker, encryptionService);
        } else {
            this.serviceConfig = certServiceConfig;
        }
    }

    @Activate
    void init() {
        if (certService == null && serviceConfig != null) {
            certService = new CertificateManagerService(serviceConfig, dataBroker, encryptionService);
            serviceConfig = null;
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
            @NonNull String storePasswd, @NonNull String alias, boolean withTag) {
        return delegate().getCertificateTrustStore(storePasswd, alias, withTag);
    }

    @Override
    public @NonNull String getODLKeyStoreCertificate(@NonNull String storePasswd, boolean withTag) {
        return delegate().getODLKeyStoreCertificate(storePasswd, withTag);
    }

    @Override
    public @NonNull String genODLKeyStoreCertificateReq(@NonNull String storePasswd, boolean withTag) {
        return delegate().genODLKeyStoreCertificateReq(storePasswd, withTag);
    }

    @Override
    public SSLContext getServerContext() {
        return delegate().getServerContext();
    }

    @Override
    public boolean importSslDataKeystores(@NonNull String odlKeystoreName, @NonNull String odlKeystorePwd,
                                          @NonNull String odlKeystoreAlias, @NonNull String trustKeystoreName,
                                          @NonNull String trustKeystorePwd, @NonNull String[] cipherSuites,
                                          @NonNull String tlsProtocols) {
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
