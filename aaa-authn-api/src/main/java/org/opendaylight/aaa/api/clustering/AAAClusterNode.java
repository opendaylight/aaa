/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.clustering;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sharon Aicler (saichelr@gmail.com)
 */
public class AAAClusterNode extends Thread{

    private static final Logger LOGGER = LoggerFactory.getLogger(AAAClusterNode.class);
    private static final byte[] DISCOVERY_PACKET_PATTERN_HEADER = new byte[]{'p','o','r','t'};
    private static final int DISCOVERY_PULSE_INTERVAL = 10000;
    private static final int DEFAULT_SERVER_PORT = 32110;
    private static final int DEFAULT_DISCOVERY_PORT = 32111;

    private final ServerSocket serverSocket;
    private boolean running = true;
    private Map<String,List<AAAClusterConnection>> connections = new HashMap<>();
    private final AAAClusterListener listener;
    private final int serverPort;
    private final int discoveryPort;
    private volatile boolean isUp = false;
    private final DiscoverNetworkAdjacentsPulse pulse;
    private final DiscoverNetworkAdjacentsListener pulseListener;

    public AAAClusterNode(final AAAClusterListener l) throws IOException {
        this(l,DEFAULT_SERVER_PORT,DEFAULT_DISCOVERY_PORT);
    }

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
        pulse = new DiscoverNetworkAdjacentsPulse();
        pulse.start();
        pulseListener = new DiscoverNetworkAdjacentsListener();
        pulseListener.start();
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
                addConnection(conn);
            } catch (IOException e) {
                if(this.isRunning()){
                    LOGGER.error("Socket was closed unexpectly");
                }
            }
        }
        LOGGER.info("Node was shutdown");
    }

    private void addConnection(AAAClusterConnection c) throws UnknownHostException {
        synchronized (this.connections){
            List<AAAClusterConnection> connList = connections.get(c.getConnectionKey());
            if(connList==null){
                connList = new LinkedList<>();
                connections.put(c.getConnectionKey(),connList);
            }
            for(Iterator<AAAClusterConnection> ec=connList.iterator();ec.hasNext();){
                AAAClusterConnection conn = ec.next();
                if(!conn.isAlive()){
                    ec.remove();
                }
            }
            connList.add(c);
            c.start();
        }
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

    public final void connectTo(String host,int port) throws IOException {
        AAAClusterConnection conn = new AAAClusterConnection(host,port, AAAClusterNode.this, AAAClusterNode.this.listener);
        addConnection(conn);
    }

    private class DiscoverNetworkAdjacentsPulse extends Thread{

        private DiscoverNetworkAdjacentsPulse(){
            this.setName("DiscoveryPluse");
            this.setDaemon(true);
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
            this.setDaemon(true);
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
                    if(isRunning()) {
                        LOGGER.error("Failed to receive packet", e);
                    }
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
                    String connectionKey = AAAClusterConnection.getConnectionKey(InetAddress.getLocalHost().getHostAddress(),p.getAddress().getHostAddress());
                    boolean shouldAddConnection = false;
                    synchronized(connections){
                        List<AAAClusterConnection> connList = connections.get(connectionKey);
                        if(connList==null){
                            shouldAddConnection = true;
                        }else{
                            for(AAAClusterConnection c:connList){
                                if(!c.isAlive()){
                                    shouldAddConnection = true;
                                }
                            }
                            if(!shouldAddConnection && connList.size()<2){
                                shouldAddConnection = true;
                            }
                        }
                    }
                    if(shouldAddConnection) {
                        AAAClusterConnection conn = new AAAClusterConnection(p.getAddress().getHostAddress(), otherPort, AAAClusterNode.this, AAAClusterNode.this.listener);
                        addConnection(conn);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error in discovery listener",e);
            }
        }

        public void shutdown(){
            this.datagramSocket.close();
        }
    }

    public void shutdown(){
        this.running = false;
        pulseListener.shutdown();
        for(List<AAAClusterConnection> connList:this.connections.values()){
            for(AAAClusterConnection c:connList){
                try{
                    c.shutdown();
                }catch(Exception e){
                    LOGGER.error("Failed to shutdown connection "+c.getName());
                }
            }
        }
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close the server socket");
        }
    }

    private void sendObject(Object o,int op){
        for(List<AAAClusterConnection> connList:this.connections.values()){
            int listSize = connList.size();
            int connIndex = 0;
            //The list is all connection to the same host,
            //Should be two.
            for(AAAClusterConnection c:connList) {
                try {
                    //Send to only one connection out of the list
                    switch(op){
                        case AAAObjectEncoder.OPERATION_WRITE:
                            c.writeObject(o);
                            break;
                        case AAAObjectEncoder.OPERATION_UPDATE:
                            c.updateObject(o);
                            break;
                        case AAAObjectEncoder.OPERATION_DELETE:
                            c.deleteObject(o);
                            break;
                    }
                    break;
                }catch(Exception e){
                    if(listSize>connIndex+1){
                        LOGGER.info("Failed to send to one of the connections.",e);
                    }else{
                        LOGGER.error("Failed to send to host "+c.getName(),e);
                    }
                }
            }
        }
    }

    public void writeObject(Object object){
        sendObject(object,AAAObjectEncoder.OPERATION_WRITE);
    }

    public void updateObject(Object object){
        sendObject(object,AAAObjectEncoder.OPERATION_UPDATE);
    }

    public void deleteObject(Object object){
        sendObject(object,AAAObjectEncoder.OPERATION_DELETE);
    }
}
