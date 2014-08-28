/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.persistence;

/**
 *
 * @author peter.mellquist@hp.com 
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreException  extends Exception{
   private static Logger logger = LoggerFactory.getLogger(StoreException.class);

   public String message=null;
	
   public StoreException(String msg) {
      logger.error(msg);	
      message = new String(msg);
   }
}
