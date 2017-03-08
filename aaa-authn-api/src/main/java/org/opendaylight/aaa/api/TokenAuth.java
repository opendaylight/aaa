/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

import java.util.List;
import java.util.Map;

/**
 * An interface for in-bound token authentication.
 *
 * @author liemmn
 */
public interface TokenAuth {

    /**
     * Validate the given token contained in the in-bound headers.
     *
     * <p>
     * If there is no token signature in the given headers for this
     * implementation, this method should return a null. If there is an
     * applicable token signature, but the token validation fails, this method
     * should throw an {@link AuthenticationException}.
     *
     * @param headers
     *            headers containing token to validate
     * @return authenticated context, or null if not applicable
     * @throws AuthenticationException
     *             if authentication fails
     */
    Authentication validate(Map<String, List<String>> headers) throws AuthenticationException;

}
