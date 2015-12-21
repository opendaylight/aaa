/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.keystone;

import java.util.List;
import java.util.Map;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Keystone {@link TokenAuth} filter.
 *
 * @author liemmn
 */
public class KeystoneTokenAuth implements TokenAuth {
    private static final Logger LOG = LoggerFactory.getLogger(KeystoneTokenAuth.class);

    static final String TOKEN = "X-Auth-Token";

    @Override
    public Authentication validate(Map<String, List<String>> headers) {
        if (!headers.containsKey(TOKEN)) {
            return null; // Not a Keystone token
        }

        // TODO: Call into Keystone to get security context...
        LOG.info("Not yet validating token {}", headers.get(TOKEN).get(0));
        return null;
    }

}
