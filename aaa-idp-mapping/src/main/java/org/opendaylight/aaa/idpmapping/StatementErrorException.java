/*
 * Copyright (c) 2014 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

/**
 * Exception thrown when a mapping rule statement fails.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */

@Deprecated
public class StatementErrorException extends RuntimeException {

    private static final long serialVersionUID = 8312665727576018327L;

    public StatementErrorException() {
    }

    public StatementErrorException(String message) {
        super(message);
    }

    public StatementErrorException(Throwable cause) {
        super(cause);
    }

    public StatementErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
