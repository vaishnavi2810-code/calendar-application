package calendar.parser;

import calendar.command.ExportEventCommand;
import calendar.dto.ExportEventDto;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser implementation for handling event export commands.
 * This class uses a regex pattern to parse user input for exporting calendar events
 * to external file formats. The parser validates command syntax, extracts the filename,
 * uses the ExporterFactory to obtain the appropriate exporter strategy based on file type,
 * and creates an ExportEventCommand ready for execution.
 */
public class ExportEventParser implements Iparser {
  private final Pattern pattern;
  /**
   * Creates an ExportEventParser with the specified set of events.
   * Initializes the regex pattern to match export commands in the format
   * "export cal filename" where filename must be a non-whitespace string
   * containing the file extension.
   */

  public ExportEventParser() {
    pattern = Pattern.compile(
                "^export cal (?<filename>\\S+)$", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public boolean canHandle(String input) {
    return pattern.matcher(input).matches();
  }

  @Override
  public Icommand parse(String command, CalendarModel service) throws Exception {
    Matcher matcher = pattern.matcher(command.trim());
    if (matcher.matches()) {
      String filename = matcher.group("filename");
      ExportEventDto dto = new ExportEventDto(filename);
      try {
        return new ExportEventCommand(dto, service);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        return null;
      }
    } else {
      throw new Exception("Error: file type '." + command + "' is not supported.");
    }
  }
}
