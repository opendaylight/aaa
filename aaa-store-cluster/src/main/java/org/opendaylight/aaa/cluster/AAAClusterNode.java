/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cluster;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sharon Aicler (saichelr@gmail.com)
 */
public class AAAClusterNode extends Thread{

    public static final int SERVER_PORT = 32100;
    private final ServerSocket serverSocket;
    private boolean running = true;
    public static final Logger LOGGER = LoggerFactory.getLogger(AAAClusterNode.class);
    private Map<String,AAAClusterConnection> connections = new HashMap<>();
    private final AAAClusterListener listener;
    private static final int DISCOVERY_BROADCAST_PORT = 32101;
    private static final byte[] DISCOVERY_PACKET_PATTERN = new byte[]{1,3,2,4,5,7,6,8};
    private static final int DISCOVERY_PULSE_INTERVAL = 10000;

    public AAAClusterNode(final AAAClusterListener l) throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        this.listener = l;
    }

    public final boolean isRunning() {
        return running;
    }

    public void run(){
        while(this.isRunning()){
            try {
                final Socket s = serverSocket.accept();
                AAAClusterConnection conn = new AAAClusterConnection(s,this,this.listener);
                synchronized (this.connections){
                    if(!this.connections.containsKey(conn.getConnectionKey())){
                        this.connections.put(conn.getConnectionKey(),conn);
                        conn.start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DiscoverNetworkAdjacentsPulse extends Thread{

        private DiscoverNetworkAdjacentsPulse(){
            this.setName("DiscoveryPluse");
        }

        public void run(){
            while(isRunning()){
                DatagramSocket s = null;

                try {
                    final DatagramPacket packet = new DatagramPacket(DISCOVERY_PACKET_PATTERN,DISCOVERY_PACKET_PATTERN.length, InetAddress.getByName("255.255.255.255"),DISCOVERY_BROADCAST_PORT);
                    s = new DatagramSocket();
                    s.send(packet);
                } catch (UnknownHostException e) {
                    LOGGER.error("Unknown host",e);
                } catch (SocketException e) {
                    LOGGER.error("Socket Exception",e);
                } catch (IOException e) {
                    LOGGER.error("Unable to send pulse",e);
                } finally {
                    if(s!=null){
                        s.close();
                    }
                }
                try {
                    Thread.sleep(DISCOVERY_PULSE_INTERVAL);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted",e);
                }
            }
        }
    }

    private class DiscoverNetworkAdjacentsListener extends Thread{

        private final DatagramSocket datagramSocket;

        public DiscoverNetworkAdjacentsListener() throws IOException {
            this.setName("DiscoveryListener");
            this.datagramSocket = new DatagramSocket(DISCOVERY_BROADCAST_PORT);
        }

        public void run(){
            while(isRunning()){
                try {
                    byte data[] = new byte[8];
                    final DatagramPacket packet = new DatagramPacket(data,data.length);
                    this.datagramSocket.receive(packet);
                    processIncomingPacket(packet);
                } catch (IOException e) {
                    LOGGER.error("Failed to receive packet",e);
                }
            }
            this.datagramSocket.close();
        }

        private void processIncomingPacket(DatagramPacket p){
            try {
                boolean fitPattern = true;
                for(int i=0;i<DISCOVERY_PACKET_PATTERN.length;i++){
                    if(p.getData()[i]!=DISCOVERY_PACKET_PATTERN[i]){
                        fitPattern = false;
                        break;
                    }
                }
                if(fitPattern) {
                    AAAClusterConnection conn = new AAAClusterConnection(p.getAddress().getHostAddress(), AAAClusterNode.this, AAAClusterNode.this.listener);
                    synchronized (connections){
                        if(!connections.containsKey(conn.getConnectionKey())){
                            connections.put(conn.getConnectionKey(),conn);
                            conn.start();
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error in discovery listener",e);
            }
        }
    }

}
