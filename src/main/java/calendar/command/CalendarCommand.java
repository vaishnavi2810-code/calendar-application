package calendar.command;

import calendar.dto.CalendarDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.IcalendarStrategy;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarModel;
import calendar.strategy.CreateCalendar;
import calendar.strategy.EditCalendar;
import calendar.strategy.UseCalendar;
import java.util.HashMap;
import java.util.Map;
/**
 * Command for executing all "calendar-level" logic (create, edit, use, copy).
 * Returns a SimpleMessageDto on success, or throws an exception on failure.
 */

public class CalendarCommand implements Icommand {
  private final CalendarDto dto;
  private final CalendarModel service;
  private final Map<CalendarDto.CalendarCommandType, IcalendarStrategy> strategyMap;

  /**
   * Creates a calendar-level command.
   *
   * @param dto the DTO containing all parsed data for the command
   * @param service the calendar service
   */
  public CalendarCommand(CalendarDto dto, CalendarModel service) {
    this.dto = dto;
    this.service = service;
    this.strategyMap = new HashMap<>();
    this.strategyMap.put(CalendarDto.CalendarCommandType.CREATE_CALENDAR, new CreateCalendar());
    this.strategyMap.put(CalendarDto.CalendarCommandType.USE_CALENDAR, new UseCalendar());
    this.strategyMap.put(CalendarDto.CalendarCommandType.EDIT_CALENDAR, new EditCalendar());
  }

  @Override
  public IresultDto execute() throws Exception {
    IcalendarStrategy strategy = strategyMap.get(dto.getType());
    if (strategy == null) {
      throw new Exception("Error: No strategy found for command type " + dto.getType());
    }
    strategy.execute(dto, service);
    return new SimpleMessageDto(getSuccessMessage(dto));
  }

  /**
   * Creates a user-friendly success message based on the command type.
   */
  private String getSuccessMessage(CalendarDto dto) {
    switch (dto.getType()) {
      case CREATE_CALENDAR:
        return "Calendar '" + dto.getCalendarName() + "' created.";
      case EDIT_CALENDAR:
        return "Calendar '" + dto.getCalendarName() + "' updated.";
      case USE_CALENDAR:
        return "Active calendar set to '" + dto.getCalendarName() + "'.";
      default:
        return "Operation successful.";
    }
  }
}