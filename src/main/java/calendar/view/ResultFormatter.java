package calendar.view;

import calendar.dto.QueryEventDto;
import calendar.dto.QueryResultDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.IresultDto;
import calendar.model.Event;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * A service dedicated to formatting "response DTOs" (IResultDto)
 * into human-readable strings for the view (console).
 * This class contains all the "fat view" logic for display.
 */
public class ResultFormatter {

  private static final DateTimeFormatter PRINT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter PRINT_DATE_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
  * Main formatting method. It inspects the DTO type and delegates
  * to the correct private formatting method.
  *
  * @param result The response DTO from a command.
  * @return A formatted, human-readable string.
  */
  public String format(IresultDto result) {
    if (result == null) {
      return "";
    }


    if (result instanceof SimpleMessageDto) {
      return ((SimpleMessageDto) result).getMessage();
    }


    if (result instanceof QueryResultDto) {
      return formatQuery((QueryResultDto) result);
    }

    return "Error: Unrecognized result type.";
  }

  /**
  * Formats the response from a QueryEventCommand.
  * This contains all the logic that used to be in
  * QueryEventCommand.formatAndPrint().
  */
  private String formatQuery(QueryResultDto queryResult) {
    Set<Event> events = queryResult.getEvents();
    QueryEventDto.QueryType type = queryResult.getQueryType();

    if (type == QueryEventDto.QueryType.SHOW_STATUS_AT) {
      return events.isEmpty() ? "available" : "busy";
    }

    if (events.isEmpty()) {
      return "No events found.";
    }

    StringBuilder output = new StringBuilder();
    output.append("Query results:\n");
    int i = 1;

    for (Event event : events) {
      if (type == QueryEventDto.QueryType.PRINT_ON_DATE) {
        String location = (event.getLocation() != null && !event.getLocation().isEmpty())
            ? " at " + event.getLocation() : "";
        output.append(String.format("- %s: %s (from %s to %s)%s\n",
            i++,
            event.getSubject(),
            event.getStartDateTime().format(PRINT_TIME_FORMAT),
            event.getEndDateTime().format(PRINT_TIME_FORMAT),
            location
        ));
      } else if (type == QueryEventDto.QueryType.PRINT_IN_RANGE) {
        String location = (event.getLocation() != null && !event.getLocation().isEmpty())
                        ? " at " + event.getLocation() : "";
        output.append(String.format("- %s starting on %s at %s, ending on %s at %s%s\n",
                        event.getSubject(),
                        event.getStartDateTime().format(PRINT_DATE_FORMAT),
                        event.getStartDateTime().format(PRINT_TIME_FORMAT),
                        event.getEndDateTime().format(PRINT_DATE_FORMAT),
                        event.getEndDateTime().format(PRINT_TIME_FORMAT),
                        location
        ));
      }
    }
    return output.toString().trim();
  }
}