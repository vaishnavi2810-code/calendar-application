package calendar.interfacetypes;

import calendar.dto.CalendarDto;
import calendar.model.CalendarModel;

/**
 * Strategy interface for executing all "calendar-level" commands.
 * this is just poc in the future we must refactor .
 * this into interface segregation to imporve code.
 */
public interface IcalendarStrategy {

  /**
  * Executes a calendar management action.
  *
  * @param dto The DTO containing all parsed data for the command.
  * @throws Exception if the operation fails.
  */
  void execute(CalendarDto dto,
               CalendarModel service) throws Exception;
}
