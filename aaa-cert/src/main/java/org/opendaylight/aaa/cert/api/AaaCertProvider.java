/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.aaa.cert.impl.KeyStoreUtilis;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.TrustKeystore;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class AaaCertProvider implements AutoCloseable, IAaaCertProvider, BindingAwareProvider {

    private final Logger LOG = LoggerFactory.getLogger(AaaCertProvider.class);
    private ServiceRegistration<IAaaCertProvider> aaaServiceRegisteration;
    private ODLKeyTool odlKeyTool;
    private CtlKeystore ctlKeyStore;
    private TrustKeystore trustKeyStore;

    public AaaCertProvider(CtlKeystore ctlKeyStore, TrustKeystore trustKeyStore) {
        LOG.info("aaa Certificate Service Initalized");
        odlKeyTool = new ODLKeyTool();
        this.ctlKeyStore = ctlKeyStore;
        this.trustKeyStore = trustKeyStore;
    }

    @Override
    public void close() throws Exception {
        LOG.info("aaa Certificate Service Closed");
        aaaServiceRegisteration.unregister();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("aaa Certificate Service Session Initiated");

        // Register the aaa OSGi
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        aaaServiceRegisteration = context.registerService(IAaaCertProvider.class, this, null);
    }

    public void createTrustKeyStore() {
        odlKeyTool.createKeyStoreImportCert(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                trustKeyStore.getCertFile(), trustKeyStore.getAlias());
    }

    @Override
    public String createTrustKeyStore(String keyStore, String storePasswd, String alias) {
        trustKeyStore.setAlias(alias);
        trustKeyStore.setName(keyStore);
        trustKeyStore.setStorePassword(storePasswd);
        if(odlKeyTool.createKeyStoreImportCert(keyStore, storePasswd, trustKeyStore.getCertFile(), alias))
            return keyStore + " Keystore created.";
        else
            return "Failed to create keystore " + keyStore;
    }

    public void createODLKeyStore() {
        createODLKeyStore(ctlKeyStore.getName(),ctlKeyStore.getStorePassword(), ctlKeyStore.getAlias(),
                  ctlKeyStore.getDname(), ctlKeyStore.getValidity());
    }

    @Override
    public String createODLKeyStore(String keyStore, String storePasswd, String alias,
            String dName, int validity) {
        ctlKeyStore.setAlias(alias);
        ctlKeyStore.setDname(dName);
        ctlKeyStore.setName(keyStore);
        ctlKeyStore.setStorePassword(storePasswd);
        ctlKeyStore.setValidity(validity);
        if(odlKeyTool.createKeyStoreWithSelfSignCert(keyStore, storePasswd, dName, alias, validity))
            return keyStore + " Keystore created.";
        else
            return "Failed to create keystore " + keyStore;
    }

    @Override
    public String getODLKeyStorCertificate(String storePasswd, String alias) {
        return odlKeyTool.getCertificate(ctlKeyStore.getName(), storePasswd, alias, true);
    }

    @Override
    public String genODLKeyStorCertificateReq(String storePasswd, String alias) {
        return odlKeyTool.generateCertificateReq(ctlKeyStore.getName(), storePasswd,
                     alias,KeyStoreUtilis.defaultSignAlg, true);
    }

    @Override
    public String addCertificateTrustStore(String storePasswd, String alias, String certificate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCertificateTrustStore(String storePasswd, String aliase) {
        return odlKeyTool.getCertificate(trustKeyStore.getName(), storePasswd, aliase, true);
    }

    @Override
    public String addCertificateODLKeyStore(String storePasswd, String alias, String certificate) {
        // TODO Auto-generated method stub
        return null;
    }
}
