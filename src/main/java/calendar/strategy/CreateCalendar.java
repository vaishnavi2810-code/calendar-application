package calendar.strategy;

import calendar.dto.CalendarDto;
import calendar.interfacetypes.IcalendarStrategy;
import calendar.model.CalendarModel;

/**
 * Strategy to create a new, empty calendar.
 */
public class CreateCalendar implements IcalendarStrategy {
  @Override
  public void execute(CalendarDto dto, CalendarModel service) throws Exception {
    service.createNewCalendar(dto.getCalendarName(), dto.getTimeZone());
  }
}
