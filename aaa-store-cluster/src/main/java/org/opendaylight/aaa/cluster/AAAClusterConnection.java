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

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class AAAClusterConnection {

    private final Socket socket;

    public AAAClusterConnection(final Socket s) {
        this.socket = s;
    }

    public AAAClusterConnection(final String host) throws IOException{
        this.socket = new Socket(host,AAAClusterNode.SERVER_PORT);
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
}
