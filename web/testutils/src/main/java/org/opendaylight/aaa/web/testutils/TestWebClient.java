/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.testutils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.aaa.web.WebServer;

/**
 * HTTP Client.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class TestWebClient {
    private final HttpClient webClient = HttpClient.newBuilder().build();
    private final String baseUrl;

    @Inject
    public TestWebClient(final WebServer webServer) {
        final String wsUrl = webServer.getBaseURL();
        baseUrl = wsUrl.endsWith("/") ? wsUrl : wsUrl + "/";
    }

    public HttpResponse<String> request(final String httpMethod, final String path)
                throws InterruptedException, IOException, URISyntaxException {
        final URL url = new URL(baseUrl + (path.startsWith("/") ? path.substring(1) : path));
        return webClient.send(HttpRequest.newBuilder(url.toURI()).method(httpMethod, BodyPublishers.noBody()).build(),
            BodyHandlers.ofString());
    }
}
