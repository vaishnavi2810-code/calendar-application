package calendar.strategy;

import calendar.dto.CalendarDto;
import calendar.interfacetypes.IcalendarStrategy;
import calendar.model.CalendarModel;

/**
 * Strategy to edit the properties of an existing calendar.
 * It's a "thin" strategy that calls the correct "wither"
 * method on the "fat" model.
 */
public class EditCalendar implements IcalendarStrategy {

  @Override
  public void execute(CalendarDto dto, CalendarModel service) throws Exception {
    switch (dto.getPropertyName().toLowerCase()) {
      case "name":
        service.updateCalendarName(dto.getCalendarName(), dto.getPropertyValue());;
        break;
      case "timezone":
        service.updateCalendarTimezone(dto.getCalendarName(), dto.getPropertyValue());
        break;
      default:
        throw new Exception("Error: Unknown property");
    }
  }
}