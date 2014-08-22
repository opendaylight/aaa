package org.opendaylight.aaa.idp_mapping;

public class StatementErrorException extends RuntimeException {
  
  private static final long serialVersionUID = 8312665727576018327L;

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
