/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.testutils;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.infrautils.testutils.web.HttpResponse;
import org.opendaylight.infrautils.testutils.web.TestWebClient.Method;

/**
 * HTTP Client.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class TestWebClient {

    private final org.opendaylight.infrautils.testutils.web.TestWebClient webClient;

    @Inject
    public TestWebClient(WebServer webServer) {
        this.webClient = new org.opendaylight.infrautils.testutils.web.TestWebClient(webServer.getBaseURL());
    }

    public HttpResponse request(Method httpMethod, String path) throws IOException {
        return webClient.request(httpMethod, path);
    }
}
