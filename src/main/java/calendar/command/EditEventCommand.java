package calendar.command;

import calendar.dto.EditEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarModel;

/**
 * Command for editing calendar events.
 * Returns a SimpleMessageDto on success, or throws an exception on failure.
 */
public class EditEventCommand implements Icommand {
  CalendarModel editservice;
  EditEventDto dto;

  /**
   * Creates an edit command.
   *
   * @param dto the edit parameters and type
   * @param service the calendar service
   */
  public EditEventCommand(EditEventDto dto, CalendarModel service) {
    this.editservice = service;
    this.dto = dto;
  }

  @Override
  public IresultDto execute() throws Exception {
    editservice.editEvent(dto);
    return new SimpleMessageDto("Event edited successfully.");
  }
}