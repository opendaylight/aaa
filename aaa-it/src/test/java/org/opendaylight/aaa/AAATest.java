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
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;

import com.sun.jersey.core.util.Base64;

/**
 * Karaf integration tests for AAA.
 *
 * @author liemmn
 *
 */
@RunWith(PaxExam.class)
public class AAATest {
    private static final String BASIC_AUTH = "admin:admin";
    private static final String TOKEN_URL = "http://localhost:8181/oauth2/token";
    private static final String TOASTER_JSON = "{'toaster:toaster':{'toaster:toasterManufacturer':'GeneralElectric','toaster:toasterModelNumber':'123','toaster:toasterStatus':'up'}}";

    @Inject
    private CredentialAuth<PasswordCredentials> ca;

    @Test
    public void testAuthN() throws OAuthSystemException, OAuthProblemException,
            IOException {
        OAuthClient oauthClient = null;
        HttpURLConnection httpClient = null;
        try {
            // Test create token
            OAuthClientRequest oauthRequest = OAuthClientRequest
                    .tokenLocation(TOKEN_URL).setGrantType(GrantType.PASSWORD)
                    .setClientId("dlux").setClientSecret("secrete")
                    .setUsername("admin").setPassword("admin").setScope("sdn")
                    .buildQueryMessage();
            oauthClient = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse resp = oauthClient
                    .accessToken(oauthRequest);
            String token = resp.getAccessToken();
            assertNotNull(token);
            assertEquals(Long.valueOf(3600), resp.getExpiresIn());

            // Test credential auth
            PasswordCredentials creds = new PasswordCredentialBuilder()
                    .setUserName("admin").setPassword("admin").build();
            Claim claim = ca.authenticate(creds, "sdn");
            assertEquals("admin", claim.user());
            assertFalse(claim.roles().isEmpty());

            // Test basic auth
            // Create toaster with token
            httpClient = (HttpURLConnection) new URL(
                    "http://localhost:8181/restconf/config").openConnection();
            httpClient.setDoOutput(true);
            httpClient.setRequestProperty("Authorization", "Bearer " + token);
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.getOutputStream().write(TOASTER_JSON.getBytes());
            httpClient.connect(); // do it!
            assertEquals(204, httpClient.getResponseCode());
            // Now retrieve toaster with basic auth
            httpClient.disconnect();
            httpClient = (HttpURLConnection) new URL(
                    "http://localhost:8181/restconf/config/toaster:toaster")
                    .openConnection();
            httpClient.setRequestProperty("Authorization", "Basic "
                    + new String(Base64.encode(BASIC_AUTH.getBytes("utf-8"))));
            httpClient.connect();
            assertEquals(200, httpClient.getResponseCode());
        } finally {
            if (oauthClient != null)
                oauthClient.shutdown();
            if (httpClient != null)
                httpClient.disconnect();
        }
    }

    @Configuration
    public Option[] config() {
        return new Option[] {
                // Provision and launch a container based on a distribution of
                // Karaf (Apache ServiceMix).
                karafDistributionConfiguration()
                        .frameworkUrl(
                                maven().groupId("org.opendaylight.controller")
                                        .artifactId("opendaylight-karaf-empty")
                                        .type("zip").versionAsInProject())
                        .name("OpenDaylight")
                        .unpackDirectory(new File("target/pax"))
                        .useDeployFolder(false),
                // It is really nice if the container sticks around after the
                // test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up
                // cluttering the logs
                configureConsole().ignoreLocalConsole(),
                logLevel(LogLevel.WARN),
                features(
                        maven().groupId("org.opendaylight.aaa")
                                .artifactId("features-aaa").type("xml")
                                .classifier("features").versionAsInProject(),
                        "odl-aaa-all"),
                features(
                        maven().groupId("org.opendaylight.controller")
                                .artifactId("features-mdsal").type("xml")
                                .classifier("features").versionAsInProject(),
                        "odl-toaster", "odl-restconf"),
                mavenBundle().groupId("org.apache.oltu.oauth2")
                        .artifactId("org.apache.oltu.oauth2.client")
                        .versionAsInProject() };
    }
}
