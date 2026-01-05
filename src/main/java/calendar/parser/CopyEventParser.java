package calendar.parser;

import calendar.command.CopyEventCommand;
import calendar.dto.CopyEventDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser implementation for handling event copying commands.
 * Identifies and parses commands to copy events between calendars.
 */
public class CopyEventParser implements Iparser {

  private final Map<CopyEventDto.CopyType, Pattern> patterns;

  /**
   * Constructs a CopyEventParser and initializes the regex patterns.
   * Defines patterns for single event copy, date-based copy, and interval-based copy.
   */
  public CopyEventParser() {
    this.patterns = new LinkedHashMap<>();

    String eventName = "(?<eventName>\\\"(.*?)\\\"|\\S+)";
    String dateTime = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}";
    String date = "\\d{4}-\\d{2}-\\d{2}";
    String targetCalendar = "(?<targetCalendar>\\S+)";

    patterns.put(CopyEventDto.CopyType.COPY_SINGLE_EVENT,
            Pattern.compile(
                    String.format("^copy event %s on (?<sourceStartDateTime>%s) "
                                    + "--target %s to (?<targetStartDateTime>%s)$",
                            eventName, dateTime, targetCalendar, dateTime),
                    Pattern.CASE_INSENSITIVE
            ));

    patterns.put(CopyEventDto.CopyType.COPY_EVENTS_ON_DATE,
            Pattern.compile(
                    String.format("^copy events on (?<sourceDate>%s) --target"
                                    +
                                    " %s to (?<targetDate>%s)$",
                            date, targetCalendar, date),
                    Pattern.CASE_INSENSITIVE
            ));

    patterns.put(CopyEventDto.CopyType.COPY_EVENTS_BETWEEN_DATES,
            Pattern.compile(
                    String.format("^copy events between (?<intervalStartDate>%s) and "
                                    + "(?<intervalEndDate>%s) --target %s "
                                    +
                                    "to (?<targetStartDate>%s)$",
                            date, date, targetCalendar, date),
                    Pattern.CASE_INSENSITIVE
            ));
  }

  /**
   * Checks if the parser can handle the given command string.
   *
   * @param commandString the user input command
   * @return true if the command starts with "copy event" or "copy events", false otherwise
   */
  @Override
  public boolean canHandle(String commandString) {
    if (commandString == null) {
      return false;
    }
    String trimmed = commandString.trim().toLowerCase();
    return trimmed.startsWith("copy event") || trimmed.startsWith("copy events");
  }

  /**
   * Parses the user input into a CopyEventCommand.
   * Matches the input against known copy patterns to extract arguments.
   *
   * @param userInput the full command string entered by the user
   * @param service   the calendar model service acting as the receiver
   * @return a configured Icommand ready for execution
   * @throws Exception if the input does not match any valid copy command format
   */
  @Override
  public Icommand parse(String userInput, CalendarModel service) throws Exception {
    String trimmed = userInput.trim();
    for (Map.Entry<CopyEventDto.CopyType, Pattern> entry : patterns.entrySet()) {
      Matcher matcher = entry.getValue().matcher(trimmed);
      if (matcher.matches()) {
        CopyEventDto dto = new CopyEventDto(entry.getKey(), matcher);
        return new CopyEventCommand(dto, service);
      }
    }
    throw new Exception("Invalid copy command format: " + userInput);
  }
}