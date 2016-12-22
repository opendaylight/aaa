/*
 * Copyright (c) 2014 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idpmapping;

/**
 * Exception thrown when a statement references an undefined value.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */

@Deprecated
public class UndefinedValueException extends RuntimeException {

    private static final long serialVersionUID = -1607453931670834435L;

    public UndefinedValueException() {
    }

    public UndefinedValueException(String message) {
        super(message);
    }

    public UndefinedValueException(Throwable cause) {
        super(cause);
    }

    public UndefinedValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
