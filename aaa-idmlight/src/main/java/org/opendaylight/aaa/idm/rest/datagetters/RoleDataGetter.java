/*
 * Copyright (c) 2017 Ericsson Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.datagetters;


import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.idm.rest.HandlerConstants;
import org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RoleDataGetter {
    private static final Logger LOG = LoggerFactory.getLogger(RoleDataGetter.class);

    private IDMError theError;

    public Optional<Role> get(String roleId) {
        Role role;
        try {
            role = AAAIDMLightModule.getStore().readRole(roleId);
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
            theError = new IDMError(500,
                    HandlerConstants.INTERNAL_ERROR_DOMAIN,
                    se.getMessage());
            return Optional.empty();
        }
        return Optional.ofNullable(role);
    }

    public Optional<IDMError> getError() {
        return Optional.ofNullable(theError);
    }
}
