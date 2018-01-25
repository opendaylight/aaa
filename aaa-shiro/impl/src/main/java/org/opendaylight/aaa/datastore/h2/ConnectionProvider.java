/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import java.sql.Connection;
import javax.sql.DataSource;

/**
 * Provider of JDBC Connections.
 * Essentially a much simplified {@link DataSource}.
 *
 * @author Michael Vorburger
 */
public interface ConnectionProvider {

    /**
     * Get a Connection.
     *
     * @return a connection from this Factory; it may be a brand new one
     *         obtained from the JDBC Driver, or an existing open one, if it
     *         hasn't previously been closed
     * @throws StoreException
     *             if no Connection could be obtained
     */
    Connection getConnection() throws StoreException;

}
