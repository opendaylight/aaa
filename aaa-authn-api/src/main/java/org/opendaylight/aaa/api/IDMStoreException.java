/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

/*
 * @author - Sharon Aicler (saichler@cisco.com)
 */
public class IDMStoreException extends Exception {

    private static final long serialVersionUID = -7534127680943957878L;

    public IDMStoreException(Exception exception) {
        super(exception);
    }

    public IDMStoreException(String msg) {
        super(msg);
    }
}
