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
 * Strategy implementation for checking availability status at a specific instant in time.
 * This class queries the calendar to find all events that are active at the specified
 * date-time instant. The result set is used to determine if the user is busy (events found)
 * or available (no events found) at that moment.
 */
public class ShowStatusAt implements Iquery {

  /**
   * Finds all events that are active at the specified instant in time.
   * Returns events whose time span includes the query instant - that is, events where
   * the instant falls between (or at) their start and end times. An empty set indicates
   * availability, while a non-empty set indicates the user is busy at that time.
   *
   * @param dto the query data transfer object containing the instant to check
   * @param allEvents the complete set of calendar events to search through
   * @return a set of events active at the specified instant, or an empty set if none are active
   * @throws Exception if the query parameters are invalid or if an error occurs during the search
   */
  @Override
  public Set<Event> find(QueryEventDto dto, Set<Event> allEvents,
                         ZoneId timezone) throws Exception {
    String dtStr = dto.getAtInstant();
    if (dtStr == null) {
      throw new Exception("Invalid query: missing date-time.");
    }
    ZonedDateTime atInstant = ZonedDateTime.of(
            LocalDateTime.parse(dtStr, DATETIME_FORMATTER),
            timezone
    );
    List<Event> activeAt = EventFinder.findActiveAt(atInstant, allEvents);
    return new HashSet<>(activeAt);
  }
}

