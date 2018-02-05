/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.shiro.realm.util.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;

/**
 * Container for an SSL context that allows untrusted access and a hostname
 * verifier that accepts any hostname.
 */
public class UntrustedSSL {

    private UntrustedSSL() {}

    private static class InsecureHostnameVerifier {
        private static final HostnameVerifier INSTANCE = (hostname, session) -> true;

        private InsecureHostnameVerifier() {}
    }

    private static class InsecureTrustManager {
        private static final TrustManager[] INSTANCE = new TrustManager[] {
            new X509TrustManager() {
                private final X509Certificate[] empty = new X509Certificate[] {};
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
                        throws CertificateException {
                    // always trusted
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
                        throws CertificateException {
                    // always trusted
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                        return empty;
                }
            }
        };

        private InsecureTrustManager() {}
    }

    private static class InsecureSSLContext {
        private static final SSLContext INSTANCE = buildSSLContext();

        private InsecureSSLContext() {}

        @SuppressWarnings("illegalcatch")
        private static SSLContext buildSSLContext() {
            try {
                SSLContext sslContext = SSLContext.getInstance(KeyStoreConstant.TLS_PROTOCOL);
                sslContext.init(null, InsecureTrustManager.INSTANCE, null);
                return sslContext;
            } catch (Exception e) {
                // should not happen and we cannot fail on this static initialization
                return null;
            }
        }
    }

    /**
     * Get a hostname verifier that accepts all hostnames.
     *
     * @return the hostname verifier.
     */
    public static HostnameVerifier getHostnameVerifier() {
        return InsecureHostnameVerifier.INSTANCE;
    }

    /**
     * Get an SSL context that allows untrusted access.
     *
     * @return the SSL context.
     */
    public static SSLContext getSSLContext() {
        return InsecureSSLContext.INSTANCE;
    }

}
