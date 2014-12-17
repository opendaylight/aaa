/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import com.hp.util.model.persistence.cassandra.client.astyanax.AstyanaxCassandraContextAccessor;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;

/**
 * @author Fabiel Zuniga
 */
final class CassandraContextAccessorAstyanaxImpl extends AstyanaxCassandraContextAccessor {

    @Override
    protected <N> CassandraContext<N> createCassandraContext(Keyspace keyspace, CassandraClient<N> cassandraClient,
            N nativeClient) {
        return new CassandraContext<N>(keyspace, cassandraClient, nativeClient);
    }
}
