/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;


/**
 *
 * Various common auth constants.
 *
 * @author liemmn
 *
 */
public interface AuthConstants {
    /** 401 exception */
    static final WebApplicationException UNAUTHORIZED_EX =
            new WebApplicationException(Status.UNAUTHORIZED);

    /** Authentication status attribute. */
    static final String AUTH_IDENTITY_STATUS = "AAA-IDENTITY-STATUS";

    /** Authentication status value representing confirmed identity. */
    static final String AUTH_IDENTITY_CONFIRMED = "CONFIRMED";

    /** Authentication domain ID attribute. */
    static final String AUTH_DOMAIN_ID = "AAA-DOMAIN-ID";

    /** Authentication domain name attribute. */
    static final String AUTH_DOMAIN_NAME = "AAA-DOMAIN-NAME";

    /** Authentication user ID attribute. */
    static final String AUTH_USER_ID = "AAA-USER-ID";

    /** Authentication user name attribute. */
    static final String AUTH_USER_NAME = "AAA-USER-NAME";

    /** Authentication user roles attribute, comma-separated roles.  */
    static final String AUTH_ROLES = "AAA-ROLES";

    /** An in-bound authentication claim */
    static final String AUTH_CLAIM = "AAA-CLAIM";

}
