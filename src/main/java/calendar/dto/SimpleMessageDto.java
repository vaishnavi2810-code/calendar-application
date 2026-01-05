package calendar.dto;

import calendar.interfacetypes.IresultDto;

/**
 * A DTO for returning a simple, human-readable string message.
 * Used for commands that don't return complex data, such as
 * create, edit, or export confirmations.
 */
public class SimpleMessageDto implements IresultDto {

  private final String message;

  /**
  * ok.
  *
  * @param message ok.
  */
  public SimpleMessageDto(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}