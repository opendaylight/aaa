/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface BearerJwtRealmConfig {

    /**
     * {@return JWTProcessor if configured}
     */
    @Nullable JWTProcessor<SecurityContext> jwtProcessor();

    /**
     * {@return configured JWT claim name used to extract the username}
     */
    @NonNull String userClaim();

    /**
     * {@return configured claim name used to extract the list of roles}
     */
    @NonNull String roleClaim();
}
