package calendar.command;

import calendar.dto.ExportEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarModel;

/**
 * Command for exporting calendar events to a file.
 * Returns a SimpleMessageDto containing the new file path on success.
 */
public class ExportEventCommand implements Icommand {
  ExportEventDto dto;
  CalendarModel exportService;

  /**
   * Creates an export command.
   *
   * @param dto the data transfer object containing export parameters
   * @param exportService the calendar service
   */
  public ExportEventCommand(ExportEventDto dto, CalendarModel exportService) {
    this.exportService = exportService;
    this.dto = dto;
  }

  @Override
  public IresultDto execute() throws Exception {
    String filePath = exportService.exportEvent(dto);
    return new SimpleMessageDto("File successfully created at " + filePath);
  }
}