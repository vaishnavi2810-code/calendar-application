package calendar.command;

import calendar.dto.CreateEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarModel;

/**
 * Command that handles the creation of calendar events.
 * Returns a SimpleMessageDto on success, or throws an exception on failure.
 */
public class CreateEventCommand implements Icommand {

  private final CalendarModel createservice;
  private final CreateEventDto dto;

  /**
   * Constructs a CreateEventCommand.
   *
   * @param dto the data transfer object containing parsed event creation details
   * @param service the calendar service
   */
  public CreateEventCommand(CreateEventDto dto, CalendarModel service) {
    this.createservice = service;
    this.dto = dto;
  }

  @Override
  public IresultDto execute() throws Exception {
    createservice.createEvent(dto);
    return new SimpleMessageDto("Event '" + dto.getSubject() + "' created successfully.");
  }
}