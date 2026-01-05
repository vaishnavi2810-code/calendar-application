package calendar.parser;

import calendar.command.CreateEventCommand;
import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser implementation for handling event creation commands.
 * This class uses regex patterns to parse user input for creating different types of events
 * including timed and all-day events, with support for single occurrences and recurring patterns
 * (with fixed repetitions or end dates). The parser validates command syntax and extracts
 * parameters into a CreateEventDto which is then wrapped in a CreateEventCommand for execution.
 */
public class CreateEventParser implements Iparser {
  private final Map<CreateEventDto.CommandType, Pattern> patterns;

  /**
   * Creates a CreateEventParser with the specified set of events.
   * Initializes regex patterns for all supported event creation command formats.
   * The patterns are stored in a LinkedHashMap to ensure they are checked in order
   * from most specific to least specific, preventing shorter patterns from incorrectly
   * matching longer command formats.
   */
  public CreateEventParser() {
    this.patterns = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order!
    String subject = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to"
            +
            " (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String repeatsFor = "repeats (?<weekdays>[MTWRFSU]+) for (?<N>\\d+) times";
    String repeatsUntil = "repeats (?<weekdays>[MTWRFSU]+) until (?<until>\\d{4}-\\d{2}-\\d{2})";
    patterns.put(CreateEventDto.CommandType.TIMED_RECURRING_UNTIL,
             Pattern.compile(String.format("^create event %s %s %s$", subject, fromTo,
                             repeatsUntil), Pattern.CASE_INSENSITIVE));
    patterns.put(CreateEventDto.CommandType.TIMED_RECURRING_FOR,
             Pattern.compile(String.format("^create event %s %s %s$", subject, fromTo, repeatsFor),
                     Pattern.CASE_INSENSITIVE));
    patterns.put(CreateEventDto.CommandType.TIMED_SINGLE,
             Pattern.compile(String.format("^create event %s %s$", subject, fromTo),
                     Pattern.CASE_INSENSITIVE));
    patterns.put(CreateEventDto.CommandType.ALL_DAY_RECURRING_UNTIL,
             Pattern.compile(String.format("^create event %s %s %s$", subject, onDate,
                             repeatsUntil), Pattern.CASE_INSENSITIVE));
    patterns.put(CreateEventDto.CommandType.ALL_DAY_RECURRING_FOR,
             Pattern.compile(String.format("^create event %s %s %s$", subject, onDate, repeatsFor),
                     Pattern.CASE_INSENSITIVE));
    patterns.put(CreateEventDto.CommandType.ALL_DAY_SINGLE,
             Pattern.compile(String.format("^create event %s %s$", subject, onDate),
                     Pattern.CASE_INSENSITIVE));
  }

  @Override
  public boolean canHandle(String commandString) {
    return commandString.startsWith("create event");
  }

  @Override
  public Icommand parse(String userInput, CalendarModel service) throws Exception {
    String trimmed = userInput.trim();
    for (Map.Entry<CreateEventDto.CommandType, Pattern> entry : patterns.entrySet()) {
      Matcher matcher = entry.getValue().matcher(trimmed);
      if (matcher.matches()) {
        CreateEventDto.CommandType type = entry.getKey();
        CreateEventDto dto = new CreateEventDto(type, matcher);
        return new CreateEventCommand(dto, service);
      }
    }
    throw new Exception("Invalid create command format.");
  }
}
