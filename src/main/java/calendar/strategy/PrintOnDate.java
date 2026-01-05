package calendar.strategy;

import calendar.dto.QueryEventDto;
import calendar.interfacetypes.Iquery;
import calendar.model.Event;
import calendar.util.EventFinder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Strategy implementation for finding events occurring on a specific date.
 * This class queries the calendar for all events that occur on the specified date,
 * regardless of their start or end times, and returns them as a set for display.
 */
public class PrintOnDate implements Iquery {

  /**
   * Finds all events that occur on the specified date.
   * Returns events that have any part of their duration falling on the query date,
   * using the configured timezone for date calculations. Events spanning multiple days
   * will be included if the query date falls within their duration.
   *
   * @param dto the query data transfer object containing the target date
   * @param allEvents the complete set of calendar events to search through
   * @return a set of events occurring on the specified date, or an empty set if none match
   * @throws Exception if the query parameters are invalid or if an error occurs during the search
   */
  @Override
  public Set<Event> find(QueryEventDto dto, Set<Event> allEvents,
                         ZoneId timezone) throws Exception {
    String dateStr = dto.getOnDate();
    if (dateStr == null) {
      throw new Exception("Invalid query: missing date.");
    }
    LocalDate onDate = LocalDate.parse(dateStr);
    List<Event> onDateEvents = EventFinder.findOnDate(onDate, timezone, allEvents);
    return new HashSet<>(onDateEvents);
  }
}
