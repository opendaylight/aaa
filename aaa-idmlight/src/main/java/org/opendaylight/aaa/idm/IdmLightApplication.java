/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;

import org.opendaylight.aaa.idm.rest.DomainHandler;
import org.opendaylight.aaa.idm.rest.RoleHandler;
import org.opendaylight.aaa.idm.rest.UserHandler;
import org.opendaylight.aaa.idm.rest.VersionHandler;
import org.opendaylight.aaa.idm.config.IdmLightConfig;
import org.opendaylight.aaa.idm.persistence.StoreBuilder;

/**
 * A JAX-RS application for IdmLight.
 *
 * @author liemmn
 *
 */
public class IdmLightApplication extends Application {
    private static Logger logger = LoggerFactory.getLogger(IdmLightApplication.class);
    private IdmLightConfig config = null;
    
    private static IdmLightApplication INSTANCE = new IdmLightApplication();
    
    
    public static IdmLightApplication getInstance() {
       return INSTANCE;
    }
    
    private IdmLightApplication() {
        logger.info("starting idmlight .... ");
        StoreBuilder storeBuilder = new StoreBuilder();
        config = IdmLightConfig.getInstance();
        if (!storeBuilder.exists()) {
            storeBuilder.init();
        }
    }

    public IdmLightConfig getConfig() {
       return config;
    }
    

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(VersionHandler.class,
                                                   DomainHandler.class,
                                                   RoleHandler.class,
                                                   UserHandler.class));
    }

}
