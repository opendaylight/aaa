/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;

public interface IAaaCertProvider extends java.lang.AutoCloseable {

    String createTrustKeyStore(String keyStore, String storePasswd, String alias);

    boolean addCertificateTrustStore(String storePasswd, String alias, String certificate);

    String getCertificateTrustStore(String storePasswd, String aliase);

    String createODLKeyStore(String keyStore, String storePasswd, String alias, String dName, int validity);

    String getODLKeyStorCertificate(String storePasswd, String alias);

    String genODLKeyStorCertificateReq(String storePasswd, String alias);

    boolean addCertificateODLKeyStore(String storePasswd, String alias, String certificate);

    KeyStore getODLKeyStore();

    KeyStore getTrustKeyStore();
}
