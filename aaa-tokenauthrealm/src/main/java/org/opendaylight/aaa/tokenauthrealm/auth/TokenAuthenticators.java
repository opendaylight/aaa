/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.tokenauthrealm.auth;

import java.util.List;
import org.opendaylight.aaa.api.TokenAuth;

/**
 * Holds TokenAuth instances.
 *
 * @author Thomas Pantelis
 */
public final class TokenAuthenticators {
    private final List<TokenAuth> tokenAuthCollection;

    public TokenAuthenticators(TokenAuth... tokenAuths) {
        tokenAuthCollection = List.of(tokenAuths);
    }

    public List<TokenAuth> getTokenAuthCollection() {
        return tokenAuthCollection;
    }
}
