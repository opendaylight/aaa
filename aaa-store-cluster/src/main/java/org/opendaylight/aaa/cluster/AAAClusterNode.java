/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cluster;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    public AAAClusterNode() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
    }

    public final boolean isRunning() {
        return running;
    }

    public void run(){
        while(this.isRunning()){
            try {
                final Socket s = serverSocket.accept();
                AAAClusterConnection conn = new AAAClusterConnection(s);
                synchronized (this.connections){
                    if(!this.connections.containsKey(conn.getConnectionKey())){
                        this.connections.put(conn.getConnectionKey(),conn);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
