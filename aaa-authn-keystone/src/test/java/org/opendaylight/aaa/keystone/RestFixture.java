/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.keystone;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.opendaylight.aaa.AuthConstants;

/**
 * Fixture for testing RESTful stuff.
 *
 * @author liemmn
 *
 */
@Path("test")
public class RestFixture {

    @Context
    private HttpServletRequest httpRequest;

    @GET
    @Produces("text/plain")
    public String msg() {
        return (httpRequest.getAttribute(AuthConstants.AUTH_IDENTITY_STATUS) == null) ? "failed"
                : "ok";
    }
}
