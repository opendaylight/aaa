/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import javax.inject.Inject;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;

/**
 * Karaf integration tests for AAA.
 *
 * @author liemmn
 *
 */
@RunWith(PaxExam.class)
public class AAATest {
    private static final String TOKEN_URL = "http://localhost:8181/oauth2/token";

    @Inject
    private CredentialAuth<PasswordCredentials> ca;

    @Test
    public void testAuthN() throws OAuthSystemException, OAuthProblemException {
        // Test create token
        /*
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(TOKEN_URL).setGrantType(GrantType.PASSWORD)
                .setClientId("dlux").setClientSecret("secrete")
                .setUsername("admin").setPassword("admin").setScope("sdn")
                .buildQueryMessage();
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        OAuthJSONAccessTokenResponse resp = client.accessToken(request);
        assertNotNull(resp.getAccessToken());
        assertEquals(Long.valueOf(3600), resp.getExpiresIn());
        */

        // Test credential auth
        PasswordCredentials creds = new PasswordCredentialBuilder()
                .setUserName("admin").setPassword("admin").build();
        Claim claim = ca.authenticate(creds, "sdn");
        assertEquals("admin", claim.user());
        assertFalse(claim.roles().isEmpty());
    }

    @Configuration
    public Option[] config() {
        return new Option[] {
                // Provision and launch a container based on a distribution of
                // Karaf (Apache ServiceMix).
                karafDistributionConfiguration()
                        .frameworkUrl(
                                maven().groupId("org.opendaylight.controller")
                                        .artifactId(
                                                "distribution.opendaylight-karaf")
                                        .type("zip").versionAsInProject())
                        .karafVersion("3.0.1").name("OpenDaylight")
                        .unpackDirectory(new File("target/pax"))
                        .useDeployFolder(false),
                // It is really nice if the container sticks around after the
                // test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up
                // cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during
                // the test. It defaults to WARN.
                logLevel(LogLevel.INFO),
                // Provision the feature exercised by this test
                features(
                        "mvn:org.opendaylight.aaa/features-aaa/0.1.0-SNAPSHOT/xml/features",
                        "odl-aaa-all"),
                provision(mavenBundle().groupId("org.apache.oltu.oauth2")
                        .artifactId("org.apache.oltu.oauth2.client")
                        .versionAsInProject()),
                //debugConfiguration("5000", true),
        };
    }
}
