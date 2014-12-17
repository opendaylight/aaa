/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import java.util.Collection;

import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;

/**
 * Column family handler.
 * 
 * @author Fabiel Zuniga
 */
public interface ColumnFamilyHandler {

    /**
     * Gets all the column families this handler reads from and writes into.
     * 
     * @return column family definitions
     */
    public abstract Collection<ColumnFamily<?, ?>> getColumnFamilies();
}
