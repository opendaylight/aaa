/*
 * Copyright (C) 2014 Red Hat
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idp_mapping;

public class StatementErrorException extends Exception {
  public StatementErrorException() {}

  public StatementErrorException(String message) {
    super(message);
  }

  public StatementErrorException(Throwable cause) {
    super(cause);
  }

  public StatementErrorException(String message, Throwable cause) {
    super(message, cause);
  }
}
