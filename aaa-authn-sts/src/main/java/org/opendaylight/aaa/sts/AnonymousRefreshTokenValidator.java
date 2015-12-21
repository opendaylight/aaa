/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import javax.servlet.http.HttpServletRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.validators.AbstractValidator;

/**
 * A refresh token validator that does not enforce client identification.
 *
 * @author liemmn
 *
 */
public class AnonymousRefreshTokenValidator extends AbstractValidator<HttpServletRequest> {

    public AnonymousRefreshTokenValidator() {
        requiredParams.add(OAuth.OAUTH_GRANT_TYPE);
        requiredParams.add(OAuth.OAUTH_REFRESH_TOKEN);

        enforceClientAuthentication = false;
    }
}
