/*
 * Copyright (C) 2014 Red Hat
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idp_mapping;

public class InvalidRuleException extends Exception {
  public InvalidRuleException() {}

  public InvalidRuleException(String message) {
    super(message);
  }

  public InvalidRuleException(Throwable cause) {
    super(cause);
  }

  public InvalidRuleException(String message, Throwable cause) {
    super(message, cause);
  }
}
