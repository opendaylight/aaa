/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import javax.servlet.http.HttpServletRequest;
import org.apache.oltu.oauth2.as.request.AbstractOAuthTokenRequest;
import org.apache.oltu.oauth2.as.validator.UnauthenticatedAuthorizationCodeValidator;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.validators.OAuthValidator;

/**
 * OAuth request wrapper.
 *
 * @author liemmn
 *
 */
public class OAuthRequest extends AbstractOAuthTokenRequest {

    public OAuthRequest(HttpServletRequest request) throws OAuthSystemException,
            OAuthProblemException {
        super(request);
    }

    @Override
    public OAuthValidator<HttpServletRequest> initValidator() throws OAuthProblemException,
            OAuthSystemException {
        validators.put(GrantType.PASSWORD.toString(), AnonymousPasswordValidator.class);
        validators.put(GrantType.REFRESH_TOKEN.toString(), AnonymousRefreshTokenValidator.class);
        validators.put(GrantType.AUTHORIZATION_CODE.toString(),
                UnauthenticatedAuthorizationCodeValidator.class);
        return super.initValidator();
    }

}
