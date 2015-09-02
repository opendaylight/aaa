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
import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.idm.rest.DomainHandler;
import org.opendaylight.aaa.idm.rest.RoleHandler;
import org.opendaylight.aaa.idm.rest.UserHandler;
import org.opendaylight.aaa.idm.rest.VersionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAX-RS application for IdmLight.
 *
 * @author liemmn
 *
 */
public class IdmLightApplication extends Application {
    private static Logger logger = LoggerFactory.getLogger(IdmLightApplication.class);

    public IdmLightApplication() {
        try {
            StoreBuilder.init();
        } catch (IDMStoreException e) {
            logger.error("Failed to populate the store with default values",e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(MOXyJsonProvider.class,
                                                   VersionHandler.class,
                                                   DomainHandler.class,
                                                   RoleHandler.class,
                                                   UserHandler.class));
    }
}
