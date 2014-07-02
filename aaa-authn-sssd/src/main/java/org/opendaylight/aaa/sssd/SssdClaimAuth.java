/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sssd;

import java.util.Map;

import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SSSD {@link ClaimAuth} implementation.
 *
 */
public class SssdClaimAuth implements ClaimAuth {
    private static final Logger logger = LoggerFactory
            .getLogger(SssdClaimAuth.class);

    @Override
    public Claim transform(Map<String, Object> sssdClaims) {
        // TODO: Transform/overlay the claims from SSSD into Claim object. If
        // there is no SSSD claim signature, this method should return a null
        // per contract.
        ClaimBuilder cb = new ClaimBuilder().setUser("sssd");
        Claim claim = cb.build();
        logger.info(claim.toString());
        return claim;
    }

}
