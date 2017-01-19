/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api.shiro.principal;

import org.opendaylight.aaa.api.Authentication;

import java.util.Set;

/**
 * Created by ryan on 1/19/17.
 */
public interface ODLPrincipal {

    String getUsername();

    String getDomain();

    String getUserId();

    Set<String> getRoles();
}
