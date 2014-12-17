/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import com.hp.util.model.persistence.cassandra.CassandraClient;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;

/**
 * Class that will allow creating instances of {@link CassandraContext} only by friends inside this
 * package.
 * 
 * @author Fabiel Zuniga
 */
public abstract class AstyanaxCassandraContextAccessor {

    private static volatile AstyanaxCassandraContextAccessor defaultAccessor;

    /**
     * Gets the default accessor.
     * 
     * @return the default accessor
     */
    public static AstyanaxCassandraContextAccessor getDefault() {
        if (defaultAccessor != null) {
            return defaultAccessor;
        }

        try {
            Class.forName(CassandraContext.class.getName(), true, CassandraContext.class.getClassLoader());
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return defaultAccessor;
    }

    /**
     * Sets the default accessor.
     * 
     * @param accessor the default accessor
     */
    public static void setDefault(AstyanaxCassandraContextAccessor accessor) {
        if (defaultAccessor != null) {
            throw new IllegalStateException("default accessor has already been set");
        }
        defaultAccessor = accessor;
    }

    /**
     * Creates a Cassandra context.
     * 
     * @param keyspace keyspace
     * @param cassandraClient cassandra client
     * @param nativeClient native cassandra client
     * @return a cassandra context
     */
    protected abstract <N> CassandraContext<N> createCassandraContext(Keyspace keyspace,
            CassandraClient<N> cassandraClient, N nativeClient);
}
