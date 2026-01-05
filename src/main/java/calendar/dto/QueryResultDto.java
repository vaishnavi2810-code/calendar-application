package calendar.dto;

import calendar.interfacetypes.IresultDto;
import calendar.model.Event;
import java.util.Set;

/**
 * A DTO for returning the results of a query.
 * It contains the raw set of events found, plus the original
 * QueryType, so the Formatter knows how to interpret this data.
 */
public class QueryResultDto implements IresultDto {

  private final Set<Event> events;
  private final QueryEventDto.QueryType queryType;

  /**
  * Creates a response DTO for a query.
  *
  * @param events    The raw set of events found.
  * @param queryType The original query type (e.g., PRINT_ON_DATE).
  */
  public QueryResultDto(Set<Event> events, QueryEventDto.QueryType queryType) {
    this.events = events;
    this.queryType = queryType;
  }

  public Set<Event> getEvents() {
    return events;
  }

  public QueryEventDto.QueryType getQueryType() {
    return queryType;
  }
}