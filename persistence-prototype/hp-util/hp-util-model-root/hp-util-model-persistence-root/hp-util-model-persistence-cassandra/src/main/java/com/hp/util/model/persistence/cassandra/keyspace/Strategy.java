/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

/**
 * Keyspace strategy.
 * 
 * @author Fabiel Zuniga
 */
public enum Strategy {
    /**
     * Simple strategy,
     */
    SIMPLE{
        @Override
        public String getStrategyClass() {
            return "SimpleStrategy";
        }
    },

    ;

    /**
     * Gets the name of the strategy class to use in a Cassandra configuration command.
     * 
     * @return the strategy class
     */
    public abstract String getStrategyClass();
}
