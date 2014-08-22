package org.opendaylight.aaa.idp_mapping;

public class UndefinedValueException extends RuntimeException {
  
  private static final long serialVersionUID = -1607453931670834435L;

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
