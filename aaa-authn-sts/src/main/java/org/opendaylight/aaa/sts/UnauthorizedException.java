/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A custom 401 web exception that handles http basic RESPONSE as well.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public final class UnauthorizedException extends WebApplicationException {

    private static final long serialVersionUID = -1732363804773027793L;

    static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    static final Object OPENDAYLIGHT = "Basic realm=\"opendaylight\"";

    private static final Response RESPONSE = Response.status(Response.Status.UNAUTHORIZED)
            .header(WWW_AUTHENTICATE, OPENDAYLIGHT).build();

    UnauthorizedException() {
        super(RESPONSE);
    }
}
