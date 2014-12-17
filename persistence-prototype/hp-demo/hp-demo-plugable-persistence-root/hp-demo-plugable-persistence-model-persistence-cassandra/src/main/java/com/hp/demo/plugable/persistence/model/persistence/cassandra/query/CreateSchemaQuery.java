/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.cassandra.query;

import java.util.ArrayList;
import java.util.Collection;

import com.hp.demo.plugable.persistence.model.persistence.cassandra.dao.NetworkDeviceDao;
import com.hp.demo.plugable.persistence.model.persistence.cassandra.dao.UserDao;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.KeyspaceConfiguration;
import com.hp.util.model.persistence.cassandra.keyspace.Strategy;

/**
 * @author Fabiel Zuniga
 */
class CreateSchemaQuery<N> implements Query<Void, CassandraContext<N>> {

    @Override
    public Void execute(CassandraContext<N> context) throws PersistenceException {
        KeyspaceConfiguration configuration = new KeyspaceConfiguration(Strategy.SIMPLE, 1);
        context.getCassandraClient().createKeyspace(context.getKeyspace(), configuration, context);

        for (ColumnFamilyHandler columnFamilyHandler : getColumnFamilyHandlers()) {
            for (ColumnFamily<?, ?> definition : columnFamilyHandler.getColumnFamilies()) {
                context.getCassandraClient().createColumnFamily(definition, context.getKeyspace(), context);
            }
        }

        return null;
    }

    private Collection<ColumnFamilyHandler> getColumnFamilyHandlers() {
        Collection<ColumnFamilyHandler> columnFamilyHandlers = new ArrayList<ColumnFamilyHandler>();
        columnFamilyHandlers.add(new NetworkDeviceDao<N>());
        columnFamilyHandlers.add(new UserDao<N>());
        return columnFamilyHandlers;
    }
}
