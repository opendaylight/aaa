/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.password.service;

import com.google.common.annotations.Beta;

/**
 * Four-tuple representing a <code>PasswordHash</code>.
 */
@Beta
public interface PasswordHash {

    /**
     * The algorithm name used to generate this hash.
     *
     * @return algorithm name used to generate this hash
     */
    String getAlgorithmName();

    /**
     * The salt used to generate this hash.
     *
     * @return salt used to generate this hash
     */
    String getSalt();

    /**
     * The number of iterations used to generate this hash.
     *
     * @return number of iterations used to generate this hash
     */
    int getIterations();

    /**
     * The hashed password.
     *
     * @return hashed password
     */
    String getHashedPassword();
}
