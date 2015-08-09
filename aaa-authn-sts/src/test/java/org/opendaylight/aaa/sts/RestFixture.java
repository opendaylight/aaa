/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

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
        return "ok";
    }
}
