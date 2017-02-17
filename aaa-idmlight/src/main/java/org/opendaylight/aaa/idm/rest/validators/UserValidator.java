/*
 * Copyright (c) 2017 Ericsson Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.validators;

import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.idm.rest.HandlerConstants;
import org.opendaylight.aaa.idm.rest.datagetters.UserDataGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UserValidator implements Validator {
    private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

    private IDMError theError;

    private final UserDataGetter dataGetter;

    public UserValidator(UserDataGetter theDataGetter) {
        dataGetter = theDataGetter;
    }

    @Override
    public boolean validate(String userId) {
        Optional<User> user = dataGetter.get(userId);
        if (!user.isPresent()) {
            if (dataGetter.getError().isPresent()) {
                theError = dataGetter.getError().get();
            }
            else {
                theError = new IDMError(404,
                        HandlerConstants.ROLE_NOT_FOUND + userId,
                        "");
            }
            return false;
        }
        return true;
    }

    @Override
    public IDMError getError() {
        return theError;
    }
}
