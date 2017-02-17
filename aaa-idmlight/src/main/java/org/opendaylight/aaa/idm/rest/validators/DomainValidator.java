/*
 * Copyright (c) 2017 Ericsson Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.validators;

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.idm.rest.HandlerConstants;
import org.opendaylight.aaa.idm.rest.datagetters.DomainDataGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DomainValidator implements Validator {
    private static final Logger LOG = LoggerFactory.getLogger(DomainValidator.class);

    private IDMError theError;

    private final DomainDataGetter dataGetter;

    public DomainValidator(DomainDataGetter theDataGetter) {
        dataGetter = theDataGetter;
    }

    @Override
    public boolean validate(String domainId) {
        Optional<Domain> theDomain = dataGetter.get(domainId);

        if (!theDomain.isPresent()) {
            if (dataGetter.getError().isPresent()) {
                theError = dataGetter.getError().get();
            }
            else {
                theError = new IDMError(404,
                        HandlerConstants.DOMAIN_NOT_FOUND + domainId,
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
