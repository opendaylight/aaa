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
