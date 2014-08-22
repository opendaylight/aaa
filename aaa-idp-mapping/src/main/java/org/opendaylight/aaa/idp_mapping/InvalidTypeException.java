package org.opendaylight.aaa.idp_mapping;

public class InvalidTypeException extends RuntimeException {
  
  private static final long serialVersionUID = 4437011247503994368L;

  public InvalidTypeException() {}

  public InvalidTypeException(String message) {
    super(message);
  }

  public InvalidTypeException(Throwable cause) {
    super(cause);
  }

  public InvalidTypeException(String message, Throwable cause) {
    super(message, cause);
  }
}
