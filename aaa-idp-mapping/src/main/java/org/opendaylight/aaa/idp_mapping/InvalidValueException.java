package org.opendaylight.aaa.idp_mapping;

public class InvalidValueException extends RuntimeException {
  
  private static final long serialVersionUID = -2351651535772692180L;

  public InvalidValueException() {}

  public InvalidValueException(String message) {
    super(message);
  }

  public InvalidValueException(Throwable cause) {
    super(cause);
  }

  public InvalidValueException(String message, Throwable cause) {
    super(message, cause);
  }
}
