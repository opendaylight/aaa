/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

public interface IAaaCertProvider extends java.lang.AutoCloseable {

    void AddCertificateKeyStore(String keyStore);

    void CreateODLTrustKeyStore();

    String CreateODLKeyStore(String keyStore, String storePasswd, String keyPasswd, String alias, String dName, String validity);

    String getODLKeyStorCertificate(String keyStore, String storePasswd, String keyPasswd, String alias);

    String getODLKeyStorCertificateReq();

    String getCertificateKeyStore(String keyStore, String certificateAliase);
}
