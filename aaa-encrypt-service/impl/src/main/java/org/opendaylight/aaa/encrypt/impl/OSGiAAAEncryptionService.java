/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Deprecated
@Component(immediate = true,
           configurationPid = "org.opendaylight.aaa.encrypt",
           service = AAAEncryptionService.class, property = "type=default")
@Designate(ocd = OSGiAAAEncryptionService.Configuration.class)
public final class OSGiAAAEncryptionService implements AAAEncryptionService {

    @ObjectClassDefinition(description = "Configuration for AAA Encryption Service.")
    public @interface Configuration {
        @AttributeDefinition(description = "Encryption key")
        String encryptKey() default "V1S1ED4OMeEh";

        @AttributeDefinition(description = "Encryption key password length")
        int passwordLength() default 12;

        @AttributeDefinition(description = "Encryption key salt")
        String encryptSalt() default "TdtWeHbch/7xP52/rp3Usw==";

        @AttributeDefinition(description = "The encryption method to use")
        String encryptMethod() default "PBKDF2WithHmacSHA1";

        @AttributeDefinition(description = "The encryption type")
        String encryptType() default "AES";

        @AttributeDefinition(description = "Number of iterations that will be used by the key")
        int encryptIterationCount() default 32768;

        @AttributeDefinition(description = "Key length")
        int encryptKeyLength() default 128;

        @AttributeDefinition(description = "Cipher transformation type ex: AES/CBC/PKCS5Padding (128)")
        String cipherTransform() default "AES/CBC/PKCS5Padding";
    }

    private final AAAEncryptionServiceImpl delegate;

    @Activate
    public OSGiAAAEncryptionService(final @Reference DataBroker dataBroker, final Configuration config) {
        delegate = new AAAEncryptionServiceImpl(buildConfig(config), dataBroker);
    }

    @Override
    public String encrypt(final String data) {
        return delegate.encrypt(data);
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        return delegate.encrypt(data);
    }

    @Override
    public String decrypt(final String encryptedData) {
        return delegate.decrypt(encryptedData);
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) {
        return delegate.decrypt(encryptedData);
    }

    private static AaaEncryptServiceConfig buildConfig(final Configuration config) {
        return new AaaEncryptServiceConfigBuilder().setEncryptKey(config.encryptKey())
                .setPasswordLength(config.passwordLength())
                .setEncryptSalt(config.encryptSalt())
                .setEncryptMethod(config.encryptMethod())
                .setEncryptType(config.encryptType())
                .setEncryptIterationCount(config.encryptIterationCount())
                .setEncryptKeyLength(config.encryptKeyLength())
                .setCipherTransforms(config.cipherTransform())
                .build();
    }
}
