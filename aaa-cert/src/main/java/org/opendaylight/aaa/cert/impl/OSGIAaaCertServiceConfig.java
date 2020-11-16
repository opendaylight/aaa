/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "aaa-cert",
                       description = "Default configuration for aaa certificate")
public @interface OSGIAaaCertServiceConfig {
    @AttributeDefinition(name = "use-config",
                         description = "Use the configuration data to create the keystores")
    boolean useConfig() default true;
    @AttributeDefinition(name = "use-mdsal",
                         description = "Use Mdsal as Data store for the keystore and certificates")
    boolean useMdsal() default true;
    @AttributeDefinition(name = "bundle-name",
                         description = "bundle name of the default TLS config in MdsaL")
    String bundleName() default "opendaylight";
    @AttributeDefinition(name = "ctlKeystore name",
                         description = "keystore name default is ctl")
    String ctlKeystoreName() default "ctl.jks";
    @AttributeDefinition(name = "ctlKeystore alias",
                         description = "key alias")
    String ctlKeystoreAlias() default "controller";
    @AttributeDefinition(name = "ctlKeystore store-password",
                         description = "keystore password")
    String ctlKeystoreStorePassword() default "";
    @AttributeDefinition(name = "ctlKeystore dname",
                         description = "X.500 Distinguished Names")
    String ctlKeystoreDname() default "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    @AttributeDefinition(name = "ctlKeystore validity",
                         description = "validity")
    int ctlKeystoreValidity() default 365;
    @AttributeDefinition(name = "ctlKeystore key-alg",
                         description = "The supported key generation algorithms i.e: DSA or RSA")
    String ctlKeystoreKeyAlg() default "RSA";
    @AttributeDefinition(name = "ctlKeystore sign-alg",
                         description = "The supported sign algorithmes i.e: SHA1withDSA or SHA1withRSA")
    String ctlKeystoreSignAlg() default "SHA1WithRSAEncryption";
    @AttributeDefinition(name = "ctlKeystore keysize",
                         description = "the key size i.e: 1024")
    int ctlKeystoreKeysize() default 1024;
    @AttributeDefinition(name = "ctlKeystore tls-protocols",
                         description = "the TLS supported protocols SSLv2Hello,TLSv1.1,TLSv1.2")
    String ctlKeystoreTlsProtocol() default "";
    @AttributeDefinition(name = "ctlKeystore cipher-suites",
                         description = "This message is displayed on startup of the component.")
    String[] ctlKeystoreCipherSuites() default {};
    @AttributeDefinition(name = "trustKeystore name",
                         description = "keystore name default is truststore")
    String trustKeystoreName() default "truststore.jks";
    @AttributeDefinition(name = "trustKeystore store-password",
                         description = "keystore password")
    String trustKeystoreStorePassword() default "";
}
