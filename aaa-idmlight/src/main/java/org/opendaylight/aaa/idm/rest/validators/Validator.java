/*
 * Copyright (c) 2017 Ericsson Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.validators;


import org.opendaylight.aaa.api.model.IDMError;

/**
 * Interface that checks if a given model entity exists or not.
 */
public interface Validator {
    /**
     * Check if a given model entity exists in the data-store or not. Each concrete implementation of
     * this interface validates a different data-type.
     *
     * @param id    the id of the object we're looking for.
     * @return True if the object was found in the data-store; false otherwise.
     */
    boolean validate(String id);

    /**
     * Return an {@link IDMError} structure indicating the reasons why the desired object was
     * not found in the data-store. Mutually exclusive with the validate method - i.e. when validate
     * returns true, there can be no IDMError returned by getError. The exact opposite is
     * also true - i.e. when there's an IDMError object returned, validate returns false.
     *
     * @return The {@link IDMError} object indicating the reasons why the object was not found
     *         in the data-store.
     */
    IDMError getError();
}
