/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saichler@gmail.com on 12/1/15.
 */
@Deprecated
public class CassandraConfig implements ManagedService {

    private static CassandraConfig instance = new CassandraConfig();
    private Dictionary<String, Object> configurations = new Hashtable<>();
    private static final Logger LOG = LoggerFactory.getLogger(CassandraConfig.class);

    public static final String P_HOST = "host";
    public static final String P_MAIN = "main-node";
    public static final String P_FACTOR = "replication_factor";
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final String DEFAULT_MAIN = "true";
    public static final String DEFAULT_REPLICATION_FACTOR = "3";

    private CassandraConfig() {
        File file = new File("./etc/aaacassandra.cfg");
        if(file.exists()){
            Properties p = new Properties();
            try {
                FileInputStream in = new FileInputStream(file);
                p.load(in);
                in.close();
                this.configurations.put(P_HOST, p.get(P_HOST));
                this.configurations.put(P_MAIN, p.get(P_MAIN));
                this.configurations.put(P_FACTOR, p.get(P_FACTOR));
                return;
            } catch (IOException e) {
                LOG.error("Failed to load cassandra config file, will use defaults",e);
            }
        }
        this.configurations.put(P_HOST, DEFAULT_HOST);
        this.configurations.put(P_MAIN, DEFAULT_MAIN);
        this.configurations.put(P_FACTOR, DEFAULT_REPLICATION_FACTOR);
    }

    public static CassandraConfig getInstance() {
        return instance;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties != null && !properties.isEmpty()) {
            Enumeration<String> k = properties.keys();
            while (k.hasMoreElements()) {
                String key = k.nextElement();
                String value = (String) properties.get(key);
                configurations.put(key, value);
            }
        }
    }

    public String getHost(){
        return (String)this.configurations.get(P_HOST);
    }

    public boolean isMainNode(){
        String mainnode = (String)this.configurations.get(P_MAIN);
        if(mainnode!=null){
            return Boolean.parseBoolean(mainnode);
        }
        return false;
    }

    public int getReplicationFactor(){
        String f = (String)this.configurations.get(P_FACTOR);
        if(f!=null){
            return Integer.parseInt(f);
        }
        return 1;
    }
}