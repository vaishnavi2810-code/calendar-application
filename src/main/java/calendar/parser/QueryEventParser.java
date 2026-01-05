package calendar.parser;

import calendar.command.QueryEventCommand;
import calendar.dto.QueryEventDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser implementation for handling event query commands.
 * This class uses regex patterns to parse user input for querying calendar events
 * with three different query types: printing events on a specific date, printing events
 * within a date-time range, or checking availability status at a specific instant.
 * The parser validates command syntax and extracts temporal parameters into a QueryEventDto
 * which is then wrapped in a QueryEventCommand for execution.
 */
public class QueryEventParser implements Iparser {

  private final Map<QueryEventDto.QueryType, Pattern> patterns;

  /**
   * Constructs a QueryEventParser and initializes the query patterns.
   * Defines regex patterns for printing events on a specific date,
   * printing events within a range, and showing status at a specific time.
   */
  public QueryEventParser() {
    this.patterns = new LinkedHashMap<>();
    String dateStr = "(?<date>\\d{4}-\\d{2}-\\d{2})";
    String dateTimeStr = "(?<datetime>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String dateTimeStart = "(?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String dateTimeEnd = "(?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";

    patterns.put(QueryEventDto.QueryType.PRINT_ON_DATE,
            Pattern.compile(String.format("^print events on %s$", dateStr),
                    Pattern.CASE_INSENSITIVE));
    patterns.put(QueryEventDto.QueryType.PRINT_IN_RANGE,
            Pattern.compile(String.format("^print events from %s to %s$",
                            dateTimeStart, dateTimeEnd),
                    Pattern.CASE_INSENSITIVE));
    patterns.put(QueryEventDto.QueryType.SHOW_STATUS_AT,
            Pattern.compile(String.format("^show status on %s$", dateTimeStr),
                    Pattern.CASE_INSENSITIVE));
  }

  /**
   * Determines whether this parser can handle the given command string.
   * Returns true if the command starts with "print events" or "show status",
   * indicating it is a query command that this parser should process.
   *
   * @param commandString the raw user input command
   * @return true if the command is a query command, false otherwise
   */
  public boolean canHandle(String commandString) {
    if (commandString == null) {
      return false;
    }
    String trimmedLower = commandString.trim().toLowerCase();
    return trimmedLower.startsWith("print events") || trimmedLower.startsWith("show status");
  }

  /**
   * Parses the query command string and creates a QueryEventCommand.
   * Iterates through the regex patterns to find a match, extracts the temporal
   * parameters (date, date-time range, or instant) into a QueryEventDto, and wraps
   * it in a QueryEventCommand ready for execution. The patterns are checked in order
   * to ensure correct matching.
   *
   * @param userInput the raw user command string to parse
   * @param service   the calendar model service acting as the receiver
   * @return a QueryEventCommand containing the parsed query parameters
   * @throws Exception if the command format is invalid or doesn't match any known pattern
   */
  @Override
  public Icommand parse(String userInput, CalendarModel service) throws Exception {
    String trimmedInput = userInput.trim();
    for (Map.Entry<QueryEventDto.QueryType, Pattern> entry : patterns.entrySet()) {
      Matcher matcher = entry.getValue().matcher(trimmedInput);
      if (matcher.matches()) {
        QueryEventDto obj = new QueryEventDto(entry.getKey(), matcher);
        return new QueryEventCommand(service, obj);
      }
    }
    throw new Exception("Invalid query command format: " + userInput);
  }
}