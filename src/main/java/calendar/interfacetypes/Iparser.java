package calendar.interfacetypes;

import calendar.model.CalendarModel;

/**
 * Strategy interface for parsing user command strings into executable command objects.
 * Each parser implementation handles a specific category of commands (create, edit, query, export)
 * and uses regex patterns to extract parameters from the command string.
 */
public interface Iparser {

  /**
   * Determines whether this parser can handle the given command string.
   * Typically implemented by checking if the command matches the parser's regex patterns.
   *
   * @param commandString the raw user input command
   * @return true if this parser should handle the command, false otherwise
   */
  boolean canHandle(String commandString);

  /**
   * Parses the command string into a specific command object that can be executed.
   * This method should only be called after canHandle() returns true.
   * Creates an appropriate DTO from the parsed command parameters and wraps it
   * in a command object.
   *
   * @param commandString the raw user input command
   * @return a command object ready for execution
   * @throws Exception if parsing fails due to invalid syntax, missing parameters,
   *                   or malformed input
   */
  Icommand parse(String commandString, CalendarModel service) throws Exception;
}