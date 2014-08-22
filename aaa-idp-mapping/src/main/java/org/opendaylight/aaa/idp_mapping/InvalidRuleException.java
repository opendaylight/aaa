package org.opendaylight.aaa.idp_mapping;

public class InvalidRuleException extends RuntimeException {
  
  private static final long serialVersionUID = 1948891573270429630L;

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
