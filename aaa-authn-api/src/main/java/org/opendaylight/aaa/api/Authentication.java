/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

/**
 * An immutable authentication context.
 *
 * @author liemmn
 */
public interface Authentication extends Claim {

    /**
     * Get the authentication expiration date/time in number of milliseconds
     * since start of epoch.
     *
     * @return expiration milliseconds since start of UTC epoch
     */
    long expiration();

}
