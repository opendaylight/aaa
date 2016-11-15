/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.h2.config;

import java.sql.Connection;
import org.opendaylight.aaa.h2.persistence.StoreException;

/**
 * Factory of JDBC Connections.
 *
 * @author Michael Vorburger
 */
public interface ConnectionFactory {

    /**
     * Get a Connection.
     * @return a connection from the Factory, may be need or existing open one
     * @throws StoreException if no Connection could be obtained
     */
    Connection getConnection() throws StoreException;

}
