/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.clustering;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class AAAClusterConnection extends Thread{

    private static final Logger LOGGER = LoggerFactory.getLogger(AAAClusterConnection.class);
    private final Socket socket;
    private final AAAClusterNode node;
    private final List<byte[]> incomingQueue = new LinkedList<byte[]>();
    private final AAAClusterListener listener;
    private boolean isUp = false;
    private final int port;
    private final IncomingQueueProcessor queueProcessor;

    public AAAClusterConnection(final Socket s,final AAAClusterNode n,final AAAClusterListener l) throws UnknownHostException {
        this.socket = s;
        this.node = n;
        this.listener = l;
        this.port = -1;
        this.setName(getConnectionKey());
        this.setDaemon(true);
        this.queueProcessor = new IncomingQueueProcessor();
        this.queueProcessor.start();
    }

    public AAAClusterConnection(final String host,final int p, final AAAClusterNode n, final AAAClusterListener l) throws IOException{
        this.node = n;
        this.listener = l;
        this.port = p;
        this.socket = new Socket(InetAddress.getByName(host),this.port);
        this.setName(getConnectionKey());
        this.setDaemon(true);
        this.queueProcessor = new IncomingQueueProcessor();
        this.queueProcessor.start();
    }

    public String getConnectionKey() throws UnknownHostException {
        String myAddr = InetAddress.getLocalHost().getHostAddress();
        String otherAddr = socket.getInetAddress().getHostAddress();
        return getConnectionKey(myAddr,otherAddr);
    }

    public static final String getConnectionKey(String aSide,String zSide){
        if(aSide.hashCode()<zSide.hashCode()){
            return aSide+"<->"+zSide;
        }else{
            return zSide+"<->"+aSide;
        }
    }

    public void shutdown(){
        try {
            this.socket.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close the socket");
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

    public void writeObject(Object o){
        final byte data[] = AAAObjectEncoder.encodeOperation(AAAObjectEncoder.OPERATION_WRITE,o);
        synchronized(this.socket){
            try {
                this.socket.getOutputStream().write(data.length);
                this.socket.getOutputStream().write(data);
                this.socket.getOutputStream().flush();
            } catch (IOException e) {
                LOGGER.error("Failed to write object",e);
            }
        }
    }

    public void updateObject(Object o){
        final byte data[] = AAAObjectEncoder.encodeOperation(AAAObjectEncoder.OPERATION_UPDATE,o);
        synchronized(this.socket){
            try {
                this.socket.getOutputStream().write(data.length);
                this.socket.getOutputStream().write(data);
                this.socket.getOutputStream().flush();
            } catch (IOException e) {
                LOGGER.error("Failed to update object",e);
            }
        }
    }

    public void deleteObject(Object o){
        final byte data[] = AAAObjectEncoder.encodeOperation(AAAObjectEncoder.OPERATION_DELETE,o);
        synchronized(this.socket){
            try {
                this.socket.getOutputStream().write(data.length);
                this.socket.getOutputStream().write(data);
                this.socket.getOutputStream().flush();
            } catch (IOException e) {
                LOGGER.error("Failed to delete object",e);
            }
        }
    }

    public void run(){
        LOGGER.info("Started connection "+this.getName());
        isUp = true;
        while(node.isRunning()){
            try {
                int dataSize = socket.getInputStream().read();
                //Socket was probably closed
                if(dataSize==-1){
                    break;
                }
                final byte[] data = new byte[dataSize];
                socket.getInputStream().read(data);
                enqueue(data);
            } catch (IOException e) {
                if(node.isRunning()) {
                    LOGGER.error("Failed to read from socket", e);
                }
            }
        }
        LOGGER.info("Connection "+this.getName()+" was shutdown.");
    }

    private void enqueue(byte data[]){
        synchronized(incomingQueue){
            incomingQueue.add(data);
            incomingQueue.notifyAll();
        }
    }

    private class IncomingQueueProcessor extends Thread {
        public IncomingQueueProcessor(){
            this.setDaemon(true);
        }
        public void run(){
            while(node.isRunning()){
                byte []data = null;
                synchronized(incomingQueue){
                    if(incomingQueue.isEmpty()){
                        try {
                            incomingQueue.wait(2000);
                        } catch (InterruptedException e) {
                            LOGGER.error("Interrupted",e);
                        }
                    }
                    if(!incomingQueue.isEmpty()){
                        data = incomingQueue.remove(0);
                    }
                }

                if(data!=null){
                    AAAByteArrayWrapper w = new AAAByteArrayWrapper(data);
                    int operation = AAAObjectEncoder.getOperation(w);
                    Object object = AAAObjectEncoder.decodeObject(w);
                    listener.receivedObject(object,operation);
                }
            }
            LOGGER.info("Qeuue Processor for "+AAAClusterConnection.this.getName()+" was shutdown.");
        }
    }
}
