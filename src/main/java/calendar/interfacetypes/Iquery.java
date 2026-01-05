package calendar.interfacetypes;

import calendar.dto.QueryEventDto;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.Set;

/**
 * Strategy interface for querying and filtering calendar events.
 * Implementations define different query types such as finding events on a specific date,
 * within a date range, or checking availability at a given time.
 */
public interface Iquery {

  /**
   * Finds and returns events that match the criteria specified in the query DTO.
   * The search logic depends on the query type: date-based queries return events
   * occurring on that date, range queries return events within the time span,
   * and status queries return events overlapping with the specified instant.
   *
   * @param dto the data transfer object containing query parameters and type
   * @param allEvents the complete set of calendar events to search through
   * @return a set of events matching the query criteria, or an empty set if none match
   * @throws Exception if the query execution fails due to invalid parameters
   */
  Set<Event> find(QueryEventDto dto, Set<Event> allEvents, ZoneId timezone) throws Exception;
}