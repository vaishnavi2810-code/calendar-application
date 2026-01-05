package calendar.interfacetypes;

import calendar.dto.CopyEventDto;
import calendar.model.Calendar;
import calendar.model.Event;
import java.util.Set;

/**
 * Strategy interface for copying calendar events.
 */
public interface Icopy {

  /**
   * Copies one or more events from source calendar to target calendar.
   *
   * @param dto the copy parameters
   * @param sourceCalendar the calendar to copy from
   * @param targetCalendar the calendar to copy to
   * @return the updated set of events for the target calendar
   * @throws Exception if copy fails
   */
  Set<Event> copy(CopyEventDto dto, Calendar sourceCalendar, Calendar targetCalendar)
      throws Exception;
}