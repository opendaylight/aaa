/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

import java.util.List;

/**
 *
 * @author saichler@gmail.com
 *
 */
@Deprecated
public class AAATokens {
    private List<AAAToken> tokens = null;

    public void setTokens(List<AAAToken> tokens){
        this.tokens = tokens;
    }

    public List<AAAToken> getTokens(){
        return this.tokens;
    }
}
