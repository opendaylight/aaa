/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;

public final class PooledConnectionProvider implements ConnectionProvider, AutoCloseable {
    private final JdbcConnectionPool pool;

    public PooledConnectionProvider(final IdmLightConfig config) {
        pool = JdbcConnectionPool.create(config.getDbConnectionString(), config.getDbUser(), config.getDbPwd());
    }

    @Override
    public void close() {
        pool.dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation attempts to maintain a pool of connections.
     */
    @Override
    public Connection getConnection() throws StoreException {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            throw new StoreException("Failed to acquire connection", e);
        }
    }
}
