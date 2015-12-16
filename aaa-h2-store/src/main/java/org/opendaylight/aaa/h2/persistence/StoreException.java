/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */
import java.sql.SQLException;

public class StoreException  extends Exception{

   public StoreException(String msg) {
      super(msg);
   }

    public StoreException(SQLException s) {
        super(s);
    }
}
