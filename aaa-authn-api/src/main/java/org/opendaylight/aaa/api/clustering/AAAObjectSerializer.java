/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.clustering;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public interface AAAObjectSerializer<T> {
    public void encode(T object, AAAByteArrayWrapper wrapper);
    public T decode(AAAByteArrayWrapper byteWrapper);
}
