/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Port;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.auth.Credentials;
import com.hp.util.common.type.net.Host;

/**
 * Keyspace connection configuration.
 * 
 * @author Fabiel Zuniga
 * @author Fabiel Zuniga
 */
public class ConnectionConfiguration {
    private Port port;
    private final List<Host> seeds;
    private final Credentials credentials;

    /**
     * Key space connection configuration.
     *
     * @param credentials authentication credentials
     * @param port Cassandra port
     * @param seeds seeds
     */
    public ConnectionConfiguration(Credentials credentials, Port port, Host... seeds) {
        if (seeds == null || seeds.length <= 0) {
            throw new IllegalArgumentException("seeds cannot be empty");
        }

        this.port = port;
        this.credentials = credentials;
        this.seeds = Collections.unmodifiableList(Arrays.asList(seeds));
    }

    /**
     * Gets Cassandra port.
     *
     * @return Cassandra port
     */
    public Port getPort() {
        return this.port;
    }

    /**
     * Gets the authentication credentials.
     *
     * @return the authentication credentials
     */
    public Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * Gets the Cassandra cluster seeds.
     *
     * @return the Cassandra cluster seeds
     */
    public List<Host> getSeeds() {
        return this.seeds;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("credentials", this.credentials),
                Property.valueOf("seeds", this.seeds),
                Property.valueOf("port", this.port)
        );
    }
}
