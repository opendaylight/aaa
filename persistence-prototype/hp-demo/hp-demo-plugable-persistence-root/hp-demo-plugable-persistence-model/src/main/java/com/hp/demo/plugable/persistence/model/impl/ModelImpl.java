/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import com.hp.demo.plugable.persistence.model.Model;
import com.hp.demo.plugable.persistence.model.NetworkDeviceService;
import com.hp.demo.plugable.persistence.model.PersistenceService;
import com.hp.demo.plugable.persistence.model.UserService;
import com.hp.util.common.log.LoggerProvider;
import com.hp.util.model.persistence.PersistenceException;

/**
 * @author Fabiel Zuniga
 */
class ModelImpl implements Model {

    private final PersistenceService persistenceService;
    private final NetworkDeviceService networkDeviceService;
    private final UserService userService;

    public ModelImpl(LoggerProvider<Class<?>> loggerProvider) {
        try {
            /*
             * TODO: The current version of Astyanax (Cassandra client) uses JPA 1.0, which has a
             * conflict with the version used by the JpaDataStore implementation (JPA 2.0). Thus,
             * when JPA is used remove the references from the Maven POM file. This is not a problem
             * in environments like OSGi where different bundles are loaded with different class
             * loaders (Different bundles can use different versions of the same third party API).
             */
            // this.persistenceService = new JpaPersistenceService(loggerProvider);
            this.persistenceService = new CassandraPersistenceService();
        }
        catch (PersistenceException e) {
            loggerProvider.getLogger(getClass()).error("Unable to create persistence service: ", e);
            throw new RuntimeException("Unable to create persistence service");
        }

        this.networkDeviceService = new NetworkDeviceServiceImpl(this.persistenceService, loggerProvider);
        this.userService = new UserServiceImpl(this.persistenceService, loggerProvider);
    }

    @Override
    public NetworkDeviceService getNetworkDeviceService() {
        return this.networkDeviceService;
    }

    @Override
    public UserService getUserService() {
        return this.userService;
    }
}
