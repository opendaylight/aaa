/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;

import org.opendaylight.aaa.cert.DataTree.impl.SslDataTreeChangeListener;
import org.opendaylight.aaa.cert.api.IAaaCertMdsalProvider;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStoresBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements AutoCloseable, BindingAwareProvider, IAaaCertMdsalProvider {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);
    private ServiceRegistration<IAaaCertMdsalProvider> aaaCertMdsalServiceRegisteration;
    private DataBroker dataBroker;
    private final ODLKeyTool odlKeyTool;
    private SslDataTreeChangeListener sslTree;

    public AaaCertMdsalProvider(String password, String initialVector) {
        LOG.info("AaaCertMdsalProvider Initialized");
        odlKeyTool = new ODLKeyTool(initialVector.getBytes(), password);
    }

    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        LOG.info("Aaa Certificate Mdsal Service Session Initiated");
        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        aaaCertMdsalServiceRegisteration = context.registerService(IAaaCertMdsalProvider.class, this, null);

        // Retrieve the data broker to create transactions
        dataBroker =  arg0.getSALService(DataBroker.class);
        KeyStores keyStoreData = new KeyStoresBuilder().setId(KeyStoresDataUtils.KEYSTORES_DATA_TREE).build();
        MdsalUtils.initalizeDatastore(LogicalDatastoreType.CONFIGURATION, dataBroker, KeyStoresDataUtils.getKeystoresIid(), keyStoreData);
        MdsalUtils.initalizeDatastore(LogicalDatastoreType.OPERATIONAL, dataBroker, KeyStoresDataUtils.getKeystoresIid(), keyStoreData);
        sslTree = new SslDataTreeChangeListener(dataBroker);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Aaa Certificate Mdsal Service Closed");
        aaaCertMdsalServiceRegisteration.unregister();
        if (sslTree != null){
            sslTree.close();
        }
    }

    @Override
    public KeyStore getODLKeyStore(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStore getTrustKeyStore(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getCipherSuites(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SslData addSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, String odlKeystoreDname, String odlKeystoreKeyAlg, String odlKeystoreSignAlg,
            int odlKeystoreKeysize, int odlKeystoreValidity, String trustKeystoreName, String trustKeystorePwd,
            String[] cipherSuites) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addODLStoreSignedCertificate(String bundleName, String alias, String certificate) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addTrustNodeCertificate(String bundleName, String alias, String certificate) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String genODLStorCertificateReq(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getODLStoreCertificate(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTrustStoreCertificate(String bundleName, String aliase) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SslData getSslData(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SslData importSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, KeyStore odlKeyStore, String trustKeystoreName, String trustKeystorePwd,
            KeyStore trustKeyStore, String[] cipherSuites) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeSslData(String bundleName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SslData updateSslData(SslData sslData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SslData addSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, String odlKeystoreDname, String trustKeystoreName, String trustKeystorePwd,
            String[] cipherSuites) {
        // TODO Auto-generated method stub
        return null;
    }
}
