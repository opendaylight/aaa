/*
 * Copyright (c) 2014 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

/**
 * Exception thrown when a mapping rule is improperly defined.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */

@Deprecated
public class InvalidRuleException extends RuntimeException {

    private static final long serialVersionUID = 1948891573270429630L;

    public InvalidRuleException() {
    }

    public InvalidRuleException(String message) {
        super(message);
    }

    public InvalidRuleException(Throwable cause) {
        super(cause);
    }

    public InvalidRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
