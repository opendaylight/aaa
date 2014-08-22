/*
 * Copyright (C) 2014 Red Hat
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idp_mapping;

public class UndefinedValueException extends Exception {
  public UndefinedValueException() {}

  public UndefinedValueException(String message) {
    super(message);
  }

  public UndefinedValueException(Throwable cause) {
    super(cause);
  }

  public UndefinedValueException(String message, Throwable cause) {
    super(message, cause);
  }
}
