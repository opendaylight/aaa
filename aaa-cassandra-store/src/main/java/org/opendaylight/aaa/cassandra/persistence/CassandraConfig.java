/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by saichler@gmail.com on 12/1/15.
 */
public class CassandraConfig implements ManagedService {

    private static CassandraConfig instance = new CassandraConfig();
    private Dictionary<String, Object> configurations = new Hashtable<>();

    public static final String P_HOST = "host";
    public static final String P_MAIN = "main-node";
    public static final String P_FACTOR = "replication_factor";

    private CassandraConfig() {
        this.configurations.put(P_HOST, "127.0.0.1");
        this.configurations.put(P_MAIN, "true");
        this.configurations.put(P_FACTOR, "3");
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
