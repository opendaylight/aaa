/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Port.
 * 
 * @author Fabiel Zuniga
 */
public class Port extends SerializableValueType<Integer> implements Comparable<Port> {
    private static final long serialVersionUID = 1L;

    static final int MAX_VALUE = 65535;

    /**
     * Creates a new port.
     *
     * @param value port value
     */
    protected Port(Integer value) {
        super(value);

        if (value.intValue() < 0 || value.intValue() > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid port: " + value + ". Must be in the range [0, " + MAX_VALUE
                    + "]");
        }
    }

    /**
     * Creates a port from its numeric value.
     *
     * @param value value
     * @return a port
     */
    public static Port valueOf(int value) {
        return new Port(Integer.valueOf(value));
    }

    /**
     * Checks whether the port is available locally.
     *
     * @return {@code true} if the port is available, {@code false} if it is in use
     */
    public boolean isAvailable() {
        try (ServerSocket serverSocket = new ServerSocket(getValue().intValue())) {
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    @Override
    public int compareTo(Port o) {
        return getValue().compareTo(o.getValue());
    }
}
