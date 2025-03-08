/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.aaa.api.TokenAuth;

/**
 * {@link TokenAuth}s forming a realm.
 */
@FunctionalInterface
@NonNullByDefault
public interface RealmAuthProvider {
    /**
     * Returns the realm's token authenticators.
     *
     * @return the realm's token authenticators
     */
    List<TokenAuth> tokenAuthenticators();
}
