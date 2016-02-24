/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.thirdparty.authn;

import com.google.common.base.Preconditions;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import javax.net.SocketFactory;
import org.opendaylight.aaa.api.PasswordCredentials;
/**
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public class LDAPIntegration {

    public static boolean authenticateSSL(PasswordCredentials creds) throws Exception {
        Preconditions.checkNotNull(creds);
        LDAPConfiguration configuration = LDAPConfiguration.getInstance();
        SSLUtil util = new SSLUtil(null, new TrustAllTrustManager());
        SocketFactory sf;
        LDAPConnection conn = null;
        try {
            sf = util.createSSLSocketFactory();
            conn = new LDAPConnection(sf, configuration.getHost(), configuration.getSSLPort());
        } catch (Exception err) {
            throw new Exception(
                    "LDAP unavailable, Failed to Authenticate user "
                            + creds.username() + " via LDAP.", err.getCause());
        }

        BindResult result = null;
        StringBuffer userDN = new StringBuffer("cn=");
        userDN.append(creds.username()).append(",").append(configuration.getDN());
        BindRequest request = new SimpleBindRequest(userDN.toString(), creds.password());
        request.setResponseTimeoutMillis(configuration.getLDAPTimeout());

        try {
            result = conn.bind(request);
        } catch (Exception err) {
            throw new Exception("Failed to Authenticate user "
                    + creds.username() + " via LDAP.", err.getCause());
        } finally {
            if (conn != null)
                conn.close();
        }

        if (result!=null && ResultCode.SUCCESS.equals(result.getResultCode())) {
            return true;
        } else {
            throw new Exception("Failed to Authenticate user "
                    + creds.username() + " via LDAP.");
        }
    }
}
