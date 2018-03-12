/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.it;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.opendaylight.infrautils.itestutils.AbstractIntegrationTest;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Karaf Integration Test (IT) for AAA.
 *
 * @author Michael Vorburger.ch
 */
@ExamReactorStrategy(PerClass.class) // no need to restart Karaf for each @Test
public class AaaIntegrationTest extends AbstractIntegrationTest {

    private final OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS).readTimeout(1, TimeUnit.SECONDS).build();

    /**
     * Test loading the AAA Shiro web feature. This is technically not required as
     * it does pretty much exactly the same as the automatic SingleFeatureTest
     * (SFT), but it's "handy" because if this passes and the other ones fail, it's
     * useful information (without having to also run the SFT).
     */
    @Test
    public void testFeatureLoaded() {
        // Intentionally empty.
    }

    /**
     * Test hitting http://localhost:8181/auth/v1/users and see it fail with a 503.
     */
    @Test
    public void testNoAuthentication() throws IOException {
        Request request = new Request.Builder().url("http://localhost:8181/auth/v1/users").build();
        try (Response response = httpClient.newCall(request).execute()) {
            assertEquals(503, response.code());
        }
    }

    /**
     * Test hitting http://localhost:8181/auth/v1/users as admin:admin and see it
     * return the expected JSON.
     */
    @Test
    public void testCorrectAuthentication() throws IOException {
        // TODO see https://github.com/square/okhttp/wiki/Recipes how to do authentication headers
    }

    /**
     * Test hitting http://localhost:8181/auth/v1/users as admin:badpass and see it
     * 503.
     */
    @Test
    public void testWrongAuthentication() throws IOException {
        // TODO implement me
    }

    @Override
    protected String featureName() {
        return "odl-aaa-shiro";
    }

    @Override
    protected UrlReference featureRepositoryURL() {
        return maven().groupId("org.opendaylight.aaa").artifactId("features-aaa").classifier("features").type("xml")
                .versionAsInProject();
    }

    @Override
    protected Option[] getAdditionalPaxExamOptions() {
        return new Option[] {
                features(maven("org.apache.karaf.features", "standard", getKarafVersion()).classifier("features")
                        .type("xml"), "wrap"),
                CoreOptions.wrappedBundle(maven("com.squareup.okhttp3", "okhttp").versionAsInProject()),
                CoreOptions.wrappedBundle(maven("com.squareup.okio", "okio").versionAsInProject()), };
    }

}
