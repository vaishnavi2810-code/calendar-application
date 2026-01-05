package calendar.strategy;

import calendar.dto.CalendarDto;
import calendar.interfacetypes.IcalendarStrategy;
import calendar.model.CalendarModel;

/**
 * Strategy to set the active calendar in the controller.
 */
public class UseCalendar implements IcalendarStrategy {
  @Override
  public void execute(CalendarDto dto, CalendarModel calendarModel) throws Exception {
    calendarModel.setActiveCalendar(dto.getCalendarName());
  }
}
