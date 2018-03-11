/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

/**
 * A catch-all authentication exception.
 *
 * @author liemmn
 *
 */
public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = -187422301135305719L;

    public AuthenticationException() {
    }

    public AuthenticationException(String msg) {
        super(msg);
    }

    public AuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AuthenticationException(Throwable throwable) {
        super(throwable);
    }
}
