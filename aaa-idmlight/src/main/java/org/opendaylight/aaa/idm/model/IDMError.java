/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.model;

/**
 *
 * @author peter.mellquist@hp.com 
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.annotation.XmlRootElement;
import javax.ws.rs.core.Response;

@XmlRootElement(name = "idmerror")
public class IDMError{
   private static Logger logger = LoggerFactory.getLogger(IDMError.class);

   private String message;
   private String details;
   private int code=500;

   public IDMError() {
   };

   public IDMError(int statusCode, String msg, String msgDetails) {
      code=statusCode;
      message=msg;
      details=msgDetails;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String msg) {
      this.message=msg;
   }

   public String getDetails() {
      return details;
   }

   public void setDetails(String details) {
      this.details=details;
   }

   public Response response() { 
      logger.error("error: " + this.message + " details: " + this.details + " status: " + code);
      return Response.status(this.code).entity(this).build();
   }
	
}
