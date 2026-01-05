package calendar.command;

import calendar.dto.CopyEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarModel;

/**
 * Command for copying calendar events from active calendar to target calendar.
 */
public class CopyEventCommand implements Icommand {
  private final CalendarModel service;
  private final CopyEventDto dto;

  /**
   * Constructs a CopyEventCommand with the specified DTO and service.
   *
   * @param dto     the data transfer object containing copy details and strategy
   * @param service the calendar model service to perform the operation
   */
  public CopyEventCommand(CopyEventDto dto, CalendarModel service) {
    this.service = service;
    this.dto = dto;
  }

  /**
   * Executes the copy event logic.
   * Delegates the operation to the service layer.
   *
   * @return a result DTO containing a success message
   * @throws Exception if the copy operation fails or validation errors occur
   */
  @Override
  public IresultDto execute() throws Exception {
    service.copyEvent(dto);
    return new SimpleMessageDto("Event(s) copied successfully.");
  }
}