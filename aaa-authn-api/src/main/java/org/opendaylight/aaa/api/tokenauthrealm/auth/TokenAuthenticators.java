/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.tokenauthrealm.auth;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.aaa.api.TokenAuth;

/**
 * Holds TokenAuth instances.
 *
 * @author Thomas Pantelis
 */
public class TokenAuthenticators {
    private final Collection<TokenAuth> tokenAuthCollection;

    public TokenAuthenticators(TokenAuth... tokenAuths) {
        tokenAuthCollection = new ImmutableList.Builder<TokenAuth>().add(tokenAuths).build();
    }

    public Collection<TokenAuth> getTokenAuthCollection() {
        return tokenAuthCollection;
    }
}
