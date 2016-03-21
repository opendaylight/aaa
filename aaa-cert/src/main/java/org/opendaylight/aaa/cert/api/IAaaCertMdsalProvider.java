/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;

import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;

/**
 * @author mserngawy
 *
 */
public interface IAaaCertMdsalProvider {

    SslData addSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, String odlKeystoreDname, String trustKeystoreName,
            String trustKeystorePwd, String[] cipherSuites);

    SslData addSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, String odlKeystoreDname, String odlKeystoreKeyAlg,
            String odlKeystoreSignAlg, int odlKeystoreKeysize, int odlKeystoreValidity,
            String trustKeystoreName, String trustKeystorePwd, String[] cipherSuites);

    boolean addODLStoreSignedCertificate(String bundleName, String alias, String certificate);

    boolean addTrustNodeCertificate(String bundleName, String alias, String certificate);

    String genODLStorCertificateReq(String bundleName);

    String getODLStoreCertificate(String bundleName);

    String getTrustStoreCertificate(String bundleName, String aliase);

    SslData getSslData(final String bundleName);

    KeyStore getODLKeyStore(final String bundleName);

    KeyStore getTrustKeyStore(final String bundleName);

    String[] getCipherSuites(final String bundleName);

    SslData importSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
                        String odlKeystoreAlias, KeyStore odlKeyStore, String trustKeystoreName,
                        String trustKeystorePwd, KeyStore trustKeyStore, String[] cipherSuites);

    boolean removeSslData(String bundleName);

    SslData updateSslData(final SslData sslData);

}
