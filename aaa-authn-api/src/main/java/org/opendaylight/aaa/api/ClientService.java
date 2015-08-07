/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

/**
 * A service for managing authorized clients to the controller.
 *
 * @author liemmn
 *
 */
public interface ClientService {

    void validate(String clientId, String clientSecret) throws AuthenticationException;
}
