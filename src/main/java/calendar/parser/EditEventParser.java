package calendar.parser;

import calendar.command.EditEventCommand;
import calendar.dto.EditEventDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser implementation for handling event editing commands.
 * This class uses regex patterns to parse user input for editing existing events
 * with three different scopes: editing a single event occurrence, editing the target
 * and all forward occurrences, or editing the entire event series. The parser validates
 * command syntax and extracts parameters including the property to modify, target event
 * identification, and the new value.
 */
public class EditEventParser implements Iparser {

  private final Map<EditEventDto.EditType, Pattern> patterns;


  /**
   * Creates an EditEventParser with the specified set of events.
   * Initializes regex patterns for all supported edit command formats:
   * "edit series" for editing entire series, "edit events" for editing forward occurrences,
   * and "edit event" for editing single occurrences. The patterns are stored in a LinkedHashMap
   * to ensure they are checked in order from most specific to least specific.
   */
  public EditEventParser() {
    this.patterns = new LinkedHashMap<>();

    String property = "(?<property>subject|start|end|description|location|status)";
    String subject = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to "
        +
        "(?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String from = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String with = "with (?<newvalue>.+)";

    patterns.put(EditEventDto.EditType.EDIT_SERIES,
        Pattern.compile(
            String.format("^edit series %s %s %s %s$", property, subject, from, with),
            Pattern.CASE_INSENSITIVE
        ));

    patterns.put(EditEventDto.EditType.EDIT_FORWARD,
        Pattern.compile(
            String.format("^edit events %s %s %s %s$", property, subject, from, with),
            Pattern.CASE_INSENSITIVE
        ));

    patterns.put(EditEventDto.EditType.EDIT_SINGLE,
        Pattern.compile(
            String.format("^edit event %s %s %s %s$", property, subject, fromTo, with),
            Pattern.CASE_INSENSITIVE
        ));
  }

  @Override
  public boolean canHandle(String commandString) {
    if (commandString == null) {
      return false;
    }
    String trimmed = commandString.trim().toLowerCase();
    return trimmed.startsWith("edit event")
        || trimmed.startsWith("edit events")
        || trimmed.startsWith("edit series");
  }

  @Override
  public Icommand parse(String userInput, CalendarModel service) throws Exception {
    String trimmed = userInput.trim();
    for (Map.Entry<EditEventDto.EditType, Pattern> entry : patterns.entrySet()) {
      Matcher matcher = entry.getValue().matcher(trimmed);
      if (matcher.matches()) {
        EditEventDto dto = new EditEventDto(entry.getKey(), matcher);
        return new EditEventCommand(dto, service);
      }
    }
    throw new Exception("Invalid edit command format: " + userInput);
  }
}