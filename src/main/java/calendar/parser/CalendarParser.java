package calendar.parser;

import calendar.command.CalendarCommand;
import calendar.dto.CalendarDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses all "calendar-level" commands (create, edit, use, copy).
 * This parser identifies which command is being run, packages the
 * data into a CalendarDto, and creates a CalendarCommand to execute it.
 */
public class CalendarParser implements Iparser {

  private final Map<CalendarDto.CalendarCommandType, Pattern> patterns;

  /**
   * Constructs a CalendarParser and initializes the command patterns.
   * Defines regex patterns for creating, editing, and selecting calendars.
   */
  public CalendarParser() {
    this.patterns = new LinkedHashMap<>();

    String name = "--name (?<name>\\\"[^\"]+\\\"|\\S+)";
    String timezone = "--timezone (?<timezone>\\S+)";
    String property = "--property (?<property>name|timezone) (?<value>\\S+)";

    patterns.put(CalendarDto.CalendarCommandType.CREATE_CALENDAR,
            Pattern.compile(String.format("^create calendar %s %s$", name, timezone),
                    Pattern.CASE_INSENSITIVE));

    patterns.put(CalendarDto.CalendarCommandType.EDIT_CALENDAR,
            Pattern.compile(String.format("^edit calendar %s %s$", name, property),
                    Pattern.CASE_INSENSITIVE));

    patterns.put(CalendarDto.CalendarCommandType.USE_CALENDAR,
            Pattern.compile(String.format("^use calendar %s$", name),
                    Pattern.CASE_INSENSITIVE));
  }

  /**
   * Checks if the parser can handle the given command string.
   *
   * @param commandString the user input command
   * @return true if the command starts with "create calendar", "edit calendar"
   */
  @Override
  public boolean canHandle(String commandString) {
    String cmd = commandString.trim().toLowerCase();
    return cmd.startsWith("create calendar")
            ||
            cmd.startsWith("edit calendar")
            ||
            cmd.startsWith("use calendar");
  }

  /**
   * Parses the user input into a specific CalendarCommand.
   * Matches the input against pre-defined regex patterns to extract arguments.
   *
   * @param userInput the full command string entered by the user
   * @param service   the calendar model service acting as the receiver
   * @return a configured Icommand ready for execution
   * @throws Exception if the input does not match any valid calendar command format
   */
  @Override
  public Icommand parse(String userInput, CalendarModel service) throws Exception {
    String trimmed = userInput.trim();
    for (Map.Entry<CalendarDto.CalendarCommandType, Pattern> entry : patterns.entrySet()) {
      Matcher matcher = entry.getValue().matcher(trimmed);
      if (matcher.matches()) {
        CalendarDto dto = new CalendarDto(entry.getKey(), matcher);
        return new CalendarCommand(dto, service);
      }
    }
    throw new Exception("Invalid calendar command format: " + userInput);
  }
}