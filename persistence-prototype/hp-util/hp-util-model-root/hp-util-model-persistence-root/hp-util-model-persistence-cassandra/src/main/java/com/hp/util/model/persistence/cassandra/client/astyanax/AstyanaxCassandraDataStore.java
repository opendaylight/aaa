/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.util.List;

import com.hp.util.common.type.Port;
import com.hp.util.common.type.net.Host;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraClient;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.keyspace.ConnectionConfiguration;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Cluster;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.Slf4jConnectionPoolMonitorImpl;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * {@link DataStore} implementation for Cassandra using Astyanax as the native client.
 * 
 * @author Fabiel Zuniga
 */
public class AstyanaxCassandraDataStore implements DataStore<CassandraContext<Astyanax>> {

    private static final String CQL_VERSION = "3.0.0";
    private static final String CONNECTION_POOL_NAME = "CassandraConnectionPool";
    private static final int MAX_CONNECTIONS_PER_HOST = 1;

    /** Default Cassandra Port */
    public static final Port DEFAULT_CASSANDRA_PORT = Port.valueOf(9160);

    private final Keyspace keyspace;
    private final CassandraClient<Astyanax> cassandraClient;
    private final Astyanax naviteCassandraClient;
    private final AstyanaxContext<com.netflix.astyanax.Keyspace> keyspaceContext;
    private final AstyanaxContext<Cluster> clusterContext;

    /**
     * Creates a data store service implementation.
     * 
     * @param keyspace Cassandra keyspace
     * @param connectionConfiguration connection configuration
     */
    public AstyanaxCassandraDataStore(Keyspace keyspace, ConnectionConfiguration connectionConfiguration) {
        this.keyspace = keyspace;
        this.cassandraClient = new AstyanaxCassandraClient();
        AstyanaxContext.Builder builder = new AstyanaxContext.Builder();
        builder.forCluster(keyspace.getClusterName());
        builder.forKeyspace(keyspace.getName());

        AstyanaxConfigurationImpl astyanaxConfiguration = new AstyanaxConfigurationImpl();
        astyanaxConfiguration.setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE);
        astyanaxConfiguration.setCqlVersion(CQL_VERSION);
        builder.withAstyanaxConfiguration(astyanaxConfiguration);

        ConnectionPoolConfigurationImpl connectionPoolConfiguration = new ConnectionPoolConfigurationImpl(
                CONNECTION_POOL_NAME);
        connectionPoolConfiguration.setPort(connectionConfiguration.getPort().getValue().intValue());
        connectionPoolConfiguration.setMaxConnsPerHost(MAX_CONNECTIONS_PER_HOST);
        connectionPoolConfiguration.setSeeds(toString(connectionConfiguration.getSeeds()));
        builder.withConnectionPoolConfiguration(connectionPoolConfiguration);

        // builder.withConnectionPoolMonitor(new CountingConnectionPoolMonitor());
        builder.withConnectionPoolMonitor(new Slf4jConnectionPoolMonitorImpl());

        this.keyspaceContext = builder.buildKeyspace(ThriftFamilyFactory.getInstance());
        this.keyspaceContext.start();

        this.clusterContext = builder.buildCluster(ThriftFamilyFactory.getInstance());
        this.clusterContext.start();

        this.naviteCassandraClient = new Astyanax(this.keyspaceContext.getClient(), this.clusterContext.getClient());
    }

    @Override
    public <T> T execute(Query<T, CassandraContext<Astyanax>> query) throws PersistenceException {
        CassandraContext<Astyanax> context = AstyanaxCassandraContextAccessor.getDefault().createCassandraContext(
                this.keyspace, this.cassandraClient, this.naviteCassandraClient);
        return query.execute(context);
    }

    private static String toString(List<Host> seeds) {
        StringBuilder str = new StringBuilder(128);

        for (Host node : seeds) {
            str.append(node.getIpAddress().getValue());
            str.append(':');
            str.append(node.getPort().getValue().intValue());
            str.append(',');
        }

        if (seeds.size() > 0) {
            // Deletes last ','
            str.delete(str.length() - 1, str.length());
        }

        return str.toString();
    }
}
