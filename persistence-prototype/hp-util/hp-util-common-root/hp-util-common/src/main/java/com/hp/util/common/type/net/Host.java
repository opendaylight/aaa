/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Port;
import com.hp.util.common.type.Property;

/**
 * Host.
 * 
 * @author Fabiel Zuniga
 */
public final class Host implements Serializable {
    private static final long serialVersionUID = 1L;

    private final IpAddress ipAddress;
    private final Port port;

    /**
     * Creates a host.
     *
     * @param ipAddress host's IP address
     * @param port port the host is listening to
     */
    public Host(IpAddress ipAddress, Port port) {
        if (ipAddress == null) {
            throw new NullPointerException("ipAddress cannot be null");
        }

        if (port == null) {
            throw new NullPointerException("port cannot be null");
        }

        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Gets the host's IP address.
     *
     * @return the host's IP address
     */
    public IpAddress getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Gets the port the host is listening to.
     *
     * @return the host's port
     */
    public Port getPort() {
        return this.port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.ipAddress.hashCode();
        result = prime * result + this.port.hashCode();
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

        Host other = (Host) obj;

        if (!this.ipAddress.equals(other.ipAddress)) {
            return false;
        }

        if (!this.port.equals(other.port)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("ipAddress", this.getIpAddress()),
                Property.valueOf("port", this.port)
        );
    }
}
