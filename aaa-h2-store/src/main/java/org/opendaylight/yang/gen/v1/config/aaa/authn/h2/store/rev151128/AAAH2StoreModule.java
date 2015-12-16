/*
 * Copyright (c) 2015 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.config.aaa.authn.h2.store.rev151128;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.h2.persistence.DomainStore;
import org.opendaylight.aaa.h2.persistence.GrantStore;
import org.opendaylight.aaa.h2.persistence.H2Store;
import org.opendaylight.aaa.h2.persistence.RoleStore;
import org.opendaylight.aaa.h2.persistence.StoreException;
import org.opendaylight.aaa.h2.persistence.UserStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAH2StoreModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.h2.store.rev151128.AbstractAAAH2StoreModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AAAH2StoreModule.class);

    private BundleContext bundleContext;
    private Connection connection;

    /**
     * Array containing TABLE type for extraction from the database
     */
    private static final String[] TABLE_TYPES = {"TABLE"};

    public AAAH2StoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAH2StoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.h2.store.rev151128.AAAH2StoreModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final H2Store h2Store = new H2Store();
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(IIDMStore.class.getName(), h2Store, null);

        try {
            connection = H2Store.getConnection(connection);
            init(connection);
        } catch(StoreException e) {
            LOGGER.error("Error connecting to the database", e);
        }

        LOGGER.info("AAA H2 Store Initialized");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                serviceRegistration.unregister();
            }
        };
    }

    /**
     * creates necessary tables
     *
     * @param connection
     */
    public static void init(final Connection connection) {
        initTable(connection, DomainStore.DOMAINS_TABLE, DomainStore.CREATE_DOMAINS_TABLE_SQL);
        initTable(connection, UserStore.USERS_TABLE, UserStore.CREATE_USERS_TABLE_SQL);
        initTable(connection, RoleStore.ROLES_TABLE, RoleStore.CREATE_ROLES_TABLE_SQL);
        initTable(connection, GrantStore.GRANTS_TABLE, GrantStore.CREATE_GRANTS_TABLE_SQL);
    }

    /**
     * creates a table if it doesn't exist
     *
     * @param connection
     * @param tableName
     * @param sqlCreationStatement
     */
    protected static void initTable(final Connection connection, final String tableName, final String sqlCreationStatement) {

        LOGGER.info("attempting to initialize the {} table", tableName);
        final DatabaseMetaData dbm;
        final ResultSet resultSet;
        try {
            dbm = connection.getMetaData();
            resultSet = dbm.getTables(null, null, tableName, TABLE_TYPES);
        } catch(SQLException e) {
            LOGGER.error("failed to query h2 for the {} table; the store may not be properly initialized", tableName, e);
            return;
        }

        final boolean tableExists = tableExists(resultSet, tableName);
        // The table already exists
        if (tableExists) {
           LOGGER.debug("{} table exists; utilizing the existing table", tableName);
        } else {
            // The table does not yet exist, and must be created
            LOGGER.info("{} table doesn't exist; attempting to create the table", tableName);
            boolean tableCreated = createTable(connection, tableName, sqlCreationStatement);
            if (tableCreated) {
                LOGGER.info("Successfully created the table {}", tableName);
            }
        }
    }

    /**
     * test whether the table is already in the database
     *
     * @param resultSet
     * @param tableName
     * @return
     */
    protected static boolean tableExists(final ResultSet resultSet, final String tableName) {
        boolean tableExists = false;
        try {
            tableExists = resultSet.next();
        } catch(SQLException e) {
            LOGGER.error("failed to query table existence for {}", tableName, e);
        } finally {
            return tableExists;
        }
    }

    /**
     * create a table by issuing the sql create command
     *
     * @param connection
     * @param tableName
     * @param sqlCreationStatement
     * @return
     */
    protected static boolean createTable(final Connection connection, final String tableName, final String sqlCreationStatement) {
        Statement statement = null;
        int numRowsAffected = 0;
        try {
            statement = connection.createStatement();
            numRowsAffected = statement.executeUpdate(sqlCreationStatement);
        } catch (SQLException e) {
            LOGGER.error("failed to create the {} table", tableName, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close the statement while creating {}", tableName, e);
                }
            }
        }
        return (numRowsAffected != 0);
    }

    /**
     * @param bundleContext
     */
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * @return the bundleContext
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }
}
