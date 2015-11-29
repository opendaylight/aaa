/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cassandra.persistence;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreException  extends Exception{
    private static final Logger LOG = LoggerFactory.getLogger(StoreException.class);

    public final String message;

    public StoreException(String msg) {
        LOG.error(msg);
        message = new String(msg);
    }
}
