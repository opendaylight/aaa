/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.aaa.cert.CtlKeyStoreProvider;
import org.opendaylight.aaa.cert.KeyToolResult;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class AaaCertProvider implements AutoCloseable, IAaaCertProvider, BindingAwareProvider {

    private final Logger LOG = LoggerFactory.getLogger(AaaCertProvider.class);
    private ServiceRegistration<IAaaCertProvider> aaaServiceRegisteration;

    public AaaCertProvider() {
        LOG.info("aaa Certificate Service Initalized");
    }

    @Override
    public void close() throws Exception {
        LOG.info("aaa Certificate Service Closed");
        aaaServiceRegisteration.unregister();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("aaa Certificate Service Session Initiated");

        // Register the aaa OSGi CLI
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        aaaServiceRegisteration = context.registerService(IAaaCertProvider.class, this, null);
    }

    @Override
    public void AddCertificateKeyStore(String keyStore) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void CreateODLTrustKeyStore() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String CreateODLKeyStore(String keyStore, String storePasswd, String keyPasswd, String alias, String dName, String validity) {
        CtlKeyStoreProvider ctlKeyStore = new CtlKeyStoreProvider();
        KeyToolResult result = ctlKeyStore.createCtlKeyStore(keyStore, storePasswd, keyPasswd, alias, dName, validity);
        if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty())
            return result.getMessage();
        else
            return result.getErrorMessage();
    }

    @Override
    public String getODLKeyStorCertificate(String keyStore, String storePasswd, String keyPasswd, String alias) {
        CtlKeyStoreProvider ctlKeyStore = new CtlKeyStoreProvider();
        KeyToolResult result = ctlKeyStore.getCtlCert(keyStore, storePasswd, keyPasswd, alias);
        if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty())
            return result.getMessage();
        else
            return result.getErrorMessage();
    }

    @Override
    public String getODLKeyStorCertificateReq() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCertificateKeyStore(String keyStore, String certificateAliase) {
        // TODO Auto-generated method stub
        return null;
    }

}
