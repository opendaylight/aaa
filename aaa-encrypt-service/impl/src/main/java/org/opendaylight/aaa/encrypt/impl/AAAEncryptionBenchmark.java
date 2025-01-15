/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import java.util.concurrent.TimeUnit;
import javax.crypto.IllegalBlockSizeException;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(8)
public class AAAEncryptionBenchmark {
    private static final AAAEncryptionServiceImpl SERVICE = createEncryptionService();

    @Param({"100", "1000"})
    public int iterations;

    @Benchmark
    @Warmup(iterations = 2, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
    public void decryptionAfterExceptionThrowBenchmark() throws Exception {
        // Verify successful encryption/decryption.
        final var before = "shortone".getBytes();
        final var encrypt = SERVICE.encrypt(before);
        SERVICE.decrypt(encrypt);

        // Throw exception in the Cipher.
        try {
            SERVICE.decrypt("admin".getBytes());
        } catch (IllegalBlockSizeException e) {
            // Correct state
        }

        // Verify that Cipher decrypt work after previous failure.
        final var encrypt2 = SERVICE.encrypt(before);
        SERVICE.decrypt(encrypt2);
    }

    @Benchmark
    @Warmup(iterations = 2, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
    public void shortStringBenchmark() throws Exception {
        final var before = "shortone".getBytes();
        final var encrypt = SERVICE.encrypt(before);
        SERVICE.decrypt(encrypt);
    }

    private static AAAEncryptionServiceImpl createEncryptionService() {
        return new AAAEncryptionServiceImpl(new EncryptServiceConfigImpl(
            OSGiEncryptionServiceConfigurator.generateConfig(new AaaEncryptServiceConfigBuilder()
                .setCipherTransforms("AES/CBC/PKCS5Padding")
                .setEncryptIterationCount(32768)
                .setEncryptKey("")
                .setEncryptKeyLength(128)
                .setEncryptMethod("PBKDF2WithHmacSHA1")
                .setEncryptSalt("")
                .setEncryptType("AES")
                .setPasswordLength(12)
                .build())));
    }
}
