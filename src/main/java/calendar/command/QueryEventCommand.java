package calendar.command;

import calendar.dto.QueryEventDto;
import calendar.dto.QueryResultDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarModel;
import calendar.model.Event;
import java.util.Set;

/**
 * Command for querying calendar events.
 * This class supports multiple query types and returns a QueryResultDto
 * containing the raw event data for the formatter.
 */
public class QueryEventCommand implements Icommand {

  private final CalendarModel queryservice;
  private final QueryEventDto dto;

  /**
   * Creates a query command.
   *
   * @param service the calendar service
   * @param dto the data transfer object containing query parameters and type
   */
  public QueryEventCommand(CalendarModel service, QueryEventDto dto) {
    this.queryservice = service;
    this.dto = dto;
  }

  @Override
  public IresultDto execute() throws Exception {
    Set<Event> result = queryservice.queryEvent(dto);
    return new QueryResultDto(result, dto.getType());
  }
}