/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Keyspace.
 * 
 * @author Fabiel Zuniga
 */
public final class Keyspace {

    private final String name;
    private final String clusterName;

    /**
     * Creates a Keyspace.
     *
     * @param name key space name
     * @param clusterName Cassandra cluster name
     */
    public Keyspace(String name, String clusterName) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        if (clusterName == null) {
            throw new NullPointerException("clusterName cannot be null");
        }

        this.name = name;
        this.clusterName = clusterName;
    }

    /**
     * Gets the keyspace name.
     *
     * @return the keyspace name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the Cassandra cluster name.
     *
     * @return the Cassandra cluster name
     */
    public String getClusterName() {
        return this.clusterName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * this.clusterName.hashCode();
        result = prime * this.name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Keyspace other = (Keyspace) obj;

        if (!this.clusterName.equals(other.clusterName)) {
            return false;
        }

        if (!this.name.equals(other.name)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("name", this.name),
                Property.valueOf("clusterName", this.clusterName)
        );
    }
}
