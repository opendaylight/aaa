/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.db.server.embedded.cassandra;

import java.nio.file.Path;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import com.hp.util.common.Restartable;
import com.hp.util.common.io.FileUtil;
import com.hp.util.common.type.Port;

/**
 * Embedded Cassandra Server.
 * <p>
 * Only one instance of {@link EmbeddedCassandraServer} can be started at a time.
 * 
 * @author Fabiel Zuniga
 */
public class EmbeddedCassandraServer implements Restartable {
    private final Path yamlConfigurationFile;

    /*
     * TODO: Unfortunately EmbeddedCassandraServerHelper expects the yaml file to be part of the
     * resources. Thus the cluster and port cannot be received as parameter.
     */

    /**
     * Creates an embedded Cassandra server.
     */
    public EmbeddedCassandraServer() {
        this.yamlConfigurationFile = FileUtil.getPath("embedded-cassandra.yaml");
    }

    /*
    / **
     * Creates an embedded Cassandra server.
     * 
     * @param port port
     * @param clusterName cluster name
     * @param databaseFolder folder to store data and configuration files
     * @throws IOException if the Cassandra configuration file (YAML) cannot be created
     * /
    public EmbeddedCassandraServer(Port port, String clusterName, Path databaseFolder) throws IOException {
        if (port == null) {
            throw new NullPointerException("port cannot be null");
        }

        if (clusterName == null) {
            throw new NullPointerException("clusterName cannot be null");
        }

        if (clusterName.isEmpty()) {
            throw new IllegalArgumentException("clusterName cannot be empty");
        }

        if (databaseFolder == null) {
            throw new NullPointerException("databaseFolder cannot be null");
        }

        this.yamlConfigurationFile = createYamlConfigurationFile(port, clusterName, databaseFolder);
    }
    */

    @Override
    public void start() {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(this.yamlConfigurationFile.toString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void stop() {
        EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
    }

    /**
     * Clears the data.
     */
    @SuppressWarnings("static-method")
    public void clearData() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    /**
     * Gets the port the Embedded Cassandra servicer is bind to.
     * 
     * @return the port
     */
    @SuppressWarnings("static-method")
    public Port getPort() {
        /*
         * TODO: Value gotten from the yaml file in the resources folder. This method would not be
         * needed if the yaml file could be located anywhere (not necessarily in the resources
         * folder) and thus the port would be passed at construction time.
         */
        return Port.valueOf(9272);
    }

    /**
     * Gets the cluster name.
     * 
     * @return the cluster name.
     */
    @SuppressWarnings("static-method")
    public String getClusterName() {
        /*
         * TODO: Value gotten from the yaml file in the resources folder. This method would not be
         * needed if the yaml file could be located anywhere (not necessarily in the resources
         * folder) and thus the port would be passed at construction time.
         */
        return "Test Cluster";
    }

    /*
    private static Path createYamlConfigurationFile(Port port, String clusterName, Path databaseFolder)
            throws IOException {
        Path yamlPath = FileUtil.getPath(databaseFolder, "embedded-cassandra.yaml");
        Path dataFolder = FileUtil.getPath(databaseFolder, "data");

        try (PrintWriter yaml = new PrintWriter(Files.newOutputStream(yamlPath))) {
            yaml.println("cluster_name: '" + clusterName + "'");
            yaml.println("# Server is accessed on localhost");
            yaml.println("listen_address: 127.0.0.1");
            yaml.println("rpc_address: localhost");
            yaml.println("# You may want to make the port non-standard if it might conflict");
            yaml.println("# with an actual Cassandra server running on the same box.");
            yaml.println("rpc_port: " + port.getValue());
            yaml.println("# Data stored in the target directory for this project");
            yaml.println("data_file_directories: " + FileUtil.getPath(dataFolder, "data").toString());
            yaml.println("commitlog_directory: " + FileUtil.getPath(dataFolder, "commitlog").toString());
            yaml.println("saved_caches_directory: " + FileUtil.getPath(dataFolder, "saved_caches").toString());
            yaml.println("# The embedded node is the only node in the cluster");
            yaml.println("seed_provider:");
            yaml.println("    - class_name: org.apache.cassandra.locator.SimpleSeedProvider");
            yaml.println("      parameters:");
            yaml.println("          - seeds: \"127.0.0.1\"");
            yaml.println("# Leave initial_token blank for embedded Cassandra");
            yaml.println("initial_token:");
            yaml.println("auto_bootstrap: false");
            yaml.println("hinted_handoff_enabled: true");
            yaml.println("authenticator: org.apache.cassandra.auth.AllowAllAuthenticator");
            yaml.println("authority: org.apache.cassandra.auth.AllowAllAuthority");
            yaml.println("partitioner: org.apache.cassandra.dht.RandomPartitioner");
            yaml.println("commitlog_sync: periodic");
            yaml.println("commitlog_sync_period_in_ms: 10000");
            yaml.println("disk_access_mode: auto");
            yaml.println("concurrent_reads: 2");
            yaml.println("concurrent_writes: 4");
            yaml.println("# sliced_buffer_size_in_kb: 64");
            yaml.println("storage_port: 7000");
            yaml.println("rpc_keepalive: true");
            yaml.println("thrift_framed_transport_size_in_mb: 15");
            yaml.println("thrift_max_message_length_in_mb: 16");
            yaml.println("snapshot_before_compaction: false");
            yaml.println("column_index_size_in_kb: 64");
            yaml.println("in_memory_compaction_limit_in_mb: 16");
            yaml.println("# rpc_timeout_in_ms: 10000");
            yaml.println("endpoint_snitch: org.apache.cassandra.locator.SimpleSnitch");
            yaml.println("dynamic_snitch: true");
            yaml.println("dynamic_snitch_update_interval_in_ms: 100");
            yaml.println("dynamic_snitch_reset_interval_in_ms: 600000");
            yaml.println("dynamic_snitch_badness_threshold: 0.0");
            yaml.println("request_scheduler: org.apache.cassandra.scheduler.NoScheduler");
            yaml.println("encryption_options:");
            yaml.println("    internode_encryption: none");
            yaml.println("    keystore: conf/.keystore");
            yaml.println("    keystore_password: cassandra");
            yaml.println("    truststore: conf/.truststore");
            yaml.println("    truststore_password: cassandra");
            yaml.println("index_interval: 128");
        }

        return yamlPath;
    }
    */
}
