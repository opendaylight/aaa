/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.net.InetAddress;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;

public class ConnectionConfigurationImp implements ConnectionConfiguration{

    private ConnectionConfiguration connConfig;
    private TlsConfiguration tlsConfig;

    public ConnectionConfigurationImp(ConnectionConfiguration baseConnConfig, TlsConfiguration tlsConfig) {
        connConfig = baseConnConfig;
        this.tlsConfig = tlsConfig;
    }

    @Override
    public InetAddress getAddress() {
        return connConfig.getAddress();
    }

    @Override
    public int getPort() {
        return connConfig.getPort();
    }

    @Override
    public Object getTransferProtocol() {
        return TransportProtocol.TLS;
    }

    @Override
    public TlsConfiguration getTlsConfiguration() {
        return tlsConfig;
    }

    @Override
    public long getSwitchIdleTimeout() {
        return connConfig.getSwitchIdleTimeout();
    }

    @Override
    public Object getSslContext() {
        return connConfig.getSslContext();
    }

    @Override
    public ThreadConfiguration getThreadConfiguration() {
        return connConfig.getThreadConfiguration();
    }

    @Override
    public boolean useBarrier() {
        return connConfig.useBarrier();
    }

}
