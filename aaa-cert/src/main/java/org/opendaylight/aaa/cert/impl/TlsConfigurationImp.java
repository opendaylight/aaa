/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;

public class TlsConfigurationImp implements TlsConfiguration{

    private String tlsKeyStore;
    private String trustKeyStore;
    private String tlsKeyStorePwd;
    private String trustKeyStorePwd;
    private String certPwd;
    private KeystoreType tlsKeyStoreType;
    private KeystoreType trustKeyStoreType;
    private PathType tlsKeystorePathType;
    private PathType trustKeystorePathType;

    public TlsConfigurationImp(String tlsKeyStore, String trustKeyStore, String tlsKeyStorePwd,
            String trustKeyStorePwd, String certPwd, KeystoreType tlsKeyStoreType,KeystoreType trustKeyStoreTy,
            PathType tlsKeystorePathType, PathType trustKeystorePathType) {
        this.tlsKeyStore = tlsKeyStore;
        this.trustKeyStore = trustKeyStore;
        this.tlsKeyStorePwd = tlsKeyStorePwd;
        this.trustKeyStorePwd = trustKeyStorePwd;
        this.certPwd = certPwd;
        this.tlsKeyStoreType = tlsKeyStoreType;
        this.trustKeyStoreType = trustKeyStoreTy;
        this.tlsKeystorePathType = tlsKeystorePathType;
        this.trustKeystorePathType = trustKeystorePathType;
    }

    @Override
    public String getTlsKeystore() {
        return tlsKeyStore;
    }

    @Override
    public KeystoreType getTlsKeystoreType() {
        return tlsKeyStoreType;
    }

    @Override
    public String getTlsTruststore() {
        return trustKeyStore;
    }

    @Override
    public KeystoreType getTlsTruststoreType() {
        return trustKeyStoreType;
    }

    @Override
    public PathType getTlsKeystorePathType() {
        return tlsKeystorePathType;
    }

    @Override
    public PathType getTlsTruststorePathType() {
        return trustKeystorePathType;
    }

    @Override
    public String getKeystorePassword() {
        return tlsKeyStorePwd;
    }

    @Override
    public String getCertificatePassword() {
        return certPwd;
    }

    @Override
    public String getTruststorePassword() {
        return trustKeyStorePwd;
    }
}
