/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.rest;

/**
 *
 * @author peter.mellquist@hp.com 
 *
 */

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.aaa.idm.model.Version;
	
@Path("/")
public class VersionHandler {
   private static Logger logger = LoggerFactory.getLogger(VersionHandler.class);;	
   
   protected static String CURRENT_VERSION      = "v1";
   protected static String LAST_UPDATED         = "2014-04-18T18:30:02.25Z";
   protected static String CURRENT_STATUS       = "CURRENT"; 
   
   @GET
   @Produces("application/json")
   public Version getVersion(@Context HttpServletRequest request) {
      logger.info("Get /");
      Version version = new Version();
      version.setId(CURRENT_VERSION);
      version.setUpdated(LAST_UPDATED);
      version.setStatus(CURRENT_STATUS);	   
      return version; 
   }
   
}
