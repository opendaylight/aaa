/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cluster;

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

    public AAAClusterConnection(final Socket s,final AAAClusterNode n,final AAAClusterListener l) throws UnknownHostException {
        this.socket = s;
        this.node = n;
        this.listener = l;
        this.setName(getConnectionKey());
    }

    public AAAClusterConnection(final String host,final AAAClusterNode n, final AAAClusterListener l) throws IOException{
        this.socket = new Socket(host,AAAClusterNode.SERVER_PORT);
        this.node = n;
        this.listener = l;
        this.setName(getConnectionKey());
    }

    public String getConnectionKey() throws UnknownHostException {
        String myAddr = InetAddress.getLocalHost().getHostAddress();
        String otherAddr = socket.getInetAddress().getHostAddress();
        if(myAddr.hashCode()<otherAddr.hashCode()){
            return myAddr+"<->"+otherAddr;
        }else{
            return otherAddr+"<->"+myAddr;
        }
    }

    public void run(){
        while(node.isRunning()){
            try {
                int dataSize = socket.getInputStream().read();
                final byte[] data = new byte[dataSize];
                socket.getInputStream().read(data);
                enqueue(data);
            } catch (IOException e) {
                LOGGER.error("Failed to read from socket",e);
            }
        }
    }

    private void enqueue(byte data[]){
        synchronized(incomingQueue){
            incomingQueue.add(data);
            incomingQueue.notifyAll();
        }
    }

    private class IncomingQueueProcessor extends Thread {
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
        }
    }
}
