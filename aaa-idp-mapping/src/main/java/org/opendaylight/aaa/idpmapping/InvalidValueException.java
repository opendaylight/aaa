/*
 * Copyright (c) 2014 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

/**
 * Exception thrown when a value cannot be used in a given context.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */

@Deprecated
public class InvalidValueException extends RuntimeException {

    private static final long serialVersionUID = -2351651535772692180L;

    public InvalidValueException() {
    }

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(Throwable cause) {
        super(cause);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
