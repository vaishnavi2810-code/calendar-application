package calendar.service;

import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import calendar.parser.CalendarParser;
import calendar.parser.CopyEventParser;
import calendar.parser.CreateEventParser;
import calendar.parser.EditEventParser;
import calendar.parser.ExportEventParser;
import calendar.parser.QueryEventParser;
import java.util.ArrayList;
import java.util.List;


/**
 * Service for parsing command strings into executable command objects.
 * Maintains a collection of parser implementations and delegates parsing to the appropriate parser
 * based on the command string. Supports calendar operations, event creation, queries, edits,
 * exports, and copy operations.
 */
public class CommandParserService {
  private final List<Iparser> parsers;

  /**
  * Constructs a new CommandParserService with all available parsers.
  * Initializes parsers for calendar, create, query, edit, export, and copy commands.
  */
  public CommandParserService() {
    this.parsers = new ArrayList<>();
    this.parsers.add(new CalendarParser());
    this.parsers.add(new CreateEventParser());
    this.parsers.add(new QueryEventParser());
    this.parsers.add(new EditEventParser());
    this.parsers.add(new ExportEventParser());
    this.parsers.add(new CopyEventParser());
  }

  /**
   * Parses a command string and returns the appropriate command object.
   * Iterates through available parsers until one can handle the command.
   *
   * @param command the command string to parse
   * @param service the calendar service to inject into commands
   * @return the parsed command object ready for execution
   * @throws Exception if the command is unknown or invalid
   */
  public Icommand parse(String command, CalendarModel service) throws Exception {
    for (Iparser parser : parsers) {
      if (parser.canHandle(command)) {
        return parser.parse(command, service);
      }
    }
    throw new Exception("Error: Unknown command.");
  }
}