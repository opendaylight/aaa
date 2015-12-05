/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.clustering;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author Sharon Aicler (saichelr@gmail.com)
 */
public class AAAClusterNode extends Thread{

    public static final int OPERATION_WRITE = 1;
    public static final int OPERATION_UPDATE = 2;
    public static final int OPERATION_DELETE = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(AAAClusterNode.class);
    private static final byte[] DISCOVERY_PACKET_PATTERN_HEADER = new byte[]{'p','o','r','t'};
    private static final int DISCOVERY_PULSE_INTERVAL = 10000;
    private static final int DEFAULT_SERVER_PORT = 32110;
    private static final int DEFAULT_DISCOVERY_PORT = 32111;

    private final ServerSocket serverSocket;
    private boolean running = true;
    private Map<String,AAAClusterConnection> connections = new HashMap<>();
    private final AAAClusterListener listener;
    private final int serverPort;
    private final int discoveryPort;
    private volatile boolean isUp = false;

    public AAAClusterNode(final AAAClusterListener l, final int p, final int dp) throws IOException {
        if(p==-1) {
            this.serverPort = DEFAULT_SERVER_PORT;
        }else{
            this.serverPort = p;
        }
        if(dp==-1){
            this.discoveryPort = DEFAULT_DISCOVERY_PORT;
        }else {
            this.discoveryPort = dp;
        }
        serverSocket = new ServerSocket(this.serverPort);
        this.listener = l;
    }

    public final boolean isRunning() {
        return running;
    }

    public void run(){
        while(this.isRunning()){
            try {
                isUp = true;
                final Socket s = serverSocket.accept();
                AAAClusterConnection conn = new AAAClusterConnection(s,this,this.listener);
                synchronized (this.connections){
                    if(!this.connections.containsKey(conn.getConnectionKey())){
                        this.connections.put(conn.getConnectionKey(),conn);
                        conn.start();
                    }
                }
            } catch (IOException e) {
                if(this.isRunning()){
                    LOGGER.error("Socket was closed unexpectly");
                }
            }
        }
        LOGGER.info("Node was shutdown");
    }

    public void start(){
        super.start();
        while(!isUp){
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                LOGGER.error("Interrupted",e);
            }
        }
    }

    public boolean connectTo(String host,int port) throws IOException {
        AAAClusterConnection conn = new AAAClusterConnection(host,port, AAAClusterNode.this, AAAClusterNode.this.listener);
        synchronized (connections){
            if(!connections.containsKey(conn.getConnectionKey())){
                connections.put(conn.getConnectionKey(),conn);
                conn.start();
                return true;
            }
        }
        return false;
    }

    private class DiscoverNetworkAdjacentsPulse extends Thread{

        private DiscoverNetworkAdjacentsPulse(){
            this.setName("DiscoveryPluse");
        }

        public void run(){
            while(isRunning()){
                DatagramSocket s = null;

                try {
                    final AAAByteArrayWrapper w = new AAAByteArrayWrapper(8);
                    System.arraycopy(DISCOVERY_PACKET_PATTERN_HEADER,0,w.getBytes(),0,DISCOVERY_PACKET_PATTERN_HEADER.length);
                    w.advance(4);
                    AAAObjectEncoder.encodeInt32(serverPort,w);
                    final DatagramPacket packet = new DatagramPacket(w.getBytes(),w.getBytes().length, InetAddress.getByName("255.255.255.255"),discoveryPort);
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
            LOGGER.info("Discover Pluse was shutdown.");
        }
    }

    private class DiscoverNetworkAdjacentsListener extends Thread{

        private final DatagramSocket datagramSocket;

        public DiscoverNetworkAdjacentsListener() throws IOException {
            this.setName("DiscoveryListener");
            this.datagramSocket = new DatagramSocket(discoveryPort);
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
            LOGGER.info("Discovery Listener was shutdown");
        }

        private void processIncomingPacket(DatagramPacket p){
            try {
                boolean fitPattern = true;
                for(int i=0;i<DISCOVERY_PACKET_PATTERN_HEADER.length;i++){
                    if(p.getData()[i]!=DISCOVERY_PACKET_PATTERN_HEADER[i]){
                        fitPattern = false;
                        break;
                    }
                }
                if(fitPattern) {
                    AAAByteArrayWrapper w = new AAAByteArrayWrapper(p.getData());
                    w.advance(4);
                    int otherPort = AAAObjectEncoder.decodeInt32(w);
                    AAAClusterConnection conn = new AAAClusterConnection(p.getAddress().getHostAddress(),otherPort, AAAClusterNode.this, AAAClusterNode.this.listener);
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

    public void shutdown(){
        this.running = false;
        for(AAAClusterConnection c:this.connections.values()){
            c.shutdown();
        }
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close the server socket");
        }
    }

    public void writeObject(Object object){
        for(AAAClusterConnection c:this.connections.values()){
            c.writeObject(object);
        }
    }
    public void updateObject(Object object){
        for(AAAClusterConnection c:this.connections.values()){
            c.updateObject(object);
        }
    }
    public void deleteObject(Object object){
        for(AAAClusterConnection c:this.connections.values()){
            c.deleteObject(object);
        }
    }
}
