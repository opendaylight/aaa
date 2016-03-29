/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.opendaylight.aaa.idm.rest.DomainHandler;
import org.opendaylight.aaa.idm.rest.RoleHandler;
import org.opendaylight.aaa.idm.rest.UserHandler;

/**
 * A JAX-RS application for IdmLight.  The REST endpoints delivered by this
 * application are in the form:
 * <code>http://{HOST}:{PORT}/auth/v1/</code>
 *
 * For example, the users REST endpoint is:
 * <code>http://{HOST}:{PORT}/auth/v1/users</code>
 *
 * This application is responsible for interaction with the backing h2
 * database store.
 *
 * @author liemmn
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <code>org.opendaylight.aaa.idm.rest.DomainHandler</code>
 * @see <code>org.opendaylight.aaa.idm.rest.UserHandler</code>
 * @see <code>org.opendaylight.aaa.idm.rest.RoleHandler</code>
 */
public class IdmLightApplication extends Application {

    //TODO create a bug to address the fact that the implementation assumes 128
    // as the max length, even though this claims 256.
    /**
     * The maximum field length for identity fields.
     */
    public static final int MAX_FIELD_LEN = 256;
    public IdmLightApplication() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(DomainHandler.class,
                                                   RoleHandler.class,
                                                   UserHandler.class));
    }
}
