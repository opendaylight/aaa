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

/**
 *
 * @author mserngawy
 * TlsConfigurationImp has configurations of the TLS connection
 */
public class TlsConfigurationImp implements TlsConfiguration{

    private final String certPwd;
    private final String tlsKeyStore;
    private final PathType tlsKeystorePathType;
    private final String tlsKeyStorePwd;
    private final KeystoreType tlsKeyStoreType;
    private final String trustKeyStore;
    private final PathType trustKeystorePathType;
    private final String trustKeyStorePwd;
    private final KeystoreType trustKeyStoreType;

    public TlsConfigurationImp(final String tlsKeyStore, final String trustKeyStore, final String tlsKeyStorePwd,
            final String trustKeyStorePwd, final String certPwd, final KeystoreType tlsKeyStoreType,final KeystoreType trustKeyStoreTy,
            final PathType tlsKeystorePathType, final PathType trustKeystorePathType) {
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
    public String getCertificatePassword() {
        return certPwd;
    }

    @Override
    public String getKeystorePassword() {
        return tlsKeyStorePwd;
    }

    @Override
    public String getTlsKeystore() {
        return tlsKeyStore;
    }

    @Override
    public PathType getTlsKeystorePathType() {
        return tlsKeystorePathType;
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
    public PathType getTlsTruststorePathType() {
        return trustKeystorePathType;
    }

    @Override
    public KeystoreType getTlsTruststoreType() {
        return trustKeyStoreType;
    }

    @Override
    public String getTruststorePassword() {
        return trustKeyStorePwd;
    }
}
