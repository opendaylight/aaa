/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

/**
 * TokenStore custom exception.
 */
public class TokenStoreException extends Exception {
    public TokenStoreException(final String message) {
        super(message);
    }

    public TokenStoreException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
