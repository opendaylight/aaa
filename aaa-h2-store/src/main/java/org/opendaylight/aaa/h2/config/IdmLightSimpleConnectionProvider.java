/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.h2.config;

import java.sql.Connection;
import java.sql.DriverManager;
import org.opendaylight.aaa.h2.persistence.StoreException;

/**
 * Simple Provider of JDBC Connections, based on an {@link IdmLightConfig} and {@link DriverManager}.
 *
 * @author Michael Vorburger
 */
public class IdmLightSimpleConnectionProvider implements ConnectionProvider {

    private final IdmLightConfig config;
    private volatile Connection existingConnection;

    public IdmLightSimpleConnectionProvider(IdmLightConfig config) {
        new org.h2.Driver();
        this.config = config;
    }

    @Override
    public Connection getConnection() throws StoreException {
        try {
            if (existingConnection == null || existingConnection.isClosed()) {
                existingConnection = DriverManager.getConnection(config.getDbConnectionString(), config.getDbUser(), config.getDbPwd());
            }
        } catch (Exception e) {
            throw new StoreException("Cannot connect to database server", e);
        }

        return existingConnection;
    }

}
