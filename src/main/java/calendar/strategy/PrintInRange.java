package calendar.strategy;

import static calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER;

import calendar.dto.QueryEventDto;
import calendar.interfacetypes.Iquery;
import calendar.model.Event;
import calendar.util.EventFinder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Strategy implementation for finding events within a specified date-time range.
 * This class queries the calendar for all events that occur between the specified
 * start and end date-times (inclusive), and returns them as a set for display.
 */
public class PrintInRange implements Iquery {

  /**
   * Finds all events that occur within the specified date-time range.
   * Returns events that have any overlap with the time period between the range
   * start and range end specified in the query DTO. The search is inclusive of
   * both boundary times.
   *
   * @param dto the query data transfer object containing the range start and end date-times
   * @param allEvents the complete set of calendar events to search through
   * @return a set of events occurring within the specified range, or an empty set if none match
   * @throws Exception if the query parameters are invalid or if an error occurs during the search
   */
  @Override
  public Set<Event> find(QueryEventDto dto, Set<Event> allEvents,
                         ZoneId timezone) throws Exception {
    String startStr = dto.getRangeStart();
    String endStr = dto.getRangeEnd();
    if (startStr == null || endStr == null) {
      throw new Exception("Invalid query: missing start or end time.");
    }
    ZonedDateTime rangeStart = ZonedDateTime.of(
            LocalDateTime.parse(startStr, DATETIME_FORMATTER),
            timezone
    );
    ZonedDateTime rangeEnd = ZonedDateTime.of(
            LocalDateTime.parse(endStr, DATETIME_FORMATTER),
            timezone
    );
    if (rangeStart.isAfter(rangeEnd)) {
      throw new Exception("Error: Start time must be before end time.");
    }
    List<Event> inRange = EventFinder.findInRange(rangeStart, rangeEnd, allEvents);
    return new HashSet<>(inRange);
  }
}
