/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import java.sql.Connection;
import java.sql.DriverManager;
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
import org.opendaylight.aaa.idm.persistence.StoreException;

/**
 * A JAX-RS application for IdmLight.
 *
 * @author liemmn
 *
 */
public class IdmLightApplication extends Application {
    private static Logger logger = LoggerFactory.getLogger(IdmLightApplication.class);
    private static IdmLightConfig config = new IdmLightConfig();

    public IdmLightApplication() {
        StoreBuilder storeBuilder = new StoreBuilder();
        if (!storeBuilder.exists()) {
            storeBuilder.init();
        }
    }

    public static IdmLightConfig getConfig() {
       return config;
    }

    public static Connection getConnection(Connection existingConnection)
          throws StoreException {
       Connection connection = existingConnection;
       try {
          if (existingConnection == null || existingConnection.isClosed()) {
             new org.h2.Driver();
             connection = DriverManager.getConnection(config.getDbPath(),
                   config.getDbUser(), config.getDbPwd());
          }
       } catch (Exception e) {
          throw new StoreException("Cannot connect to database server " + e);
       }

       return connection;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(VersionHandler.class,
                                                   DomainHandler.class,
                                                   RoleHandler.class,
                                                   UserHandler.class));
    }

}
