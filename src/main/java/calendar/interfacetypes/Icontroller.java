package calendar.interfacetypes;

/**
 * Interface for the controller component that manages calendar operations.
 * The controller is responsible for receiving user commands, delegating them to
 * appropriate parsers, and coordinating command execution.
 */
public interface Icontroller {

  /**
   * Runs the controller main loop.
   */
  void run();

  /**
   * Processes a user command string by finding the appropriate parser, executing
   * the resulting command, and returning its result.
   *
   * @param command the raw user command string to be processed
   * @return An IResultDto containing the result of the execution.
   * @throws Exception if the command is invalid, cannot be parsed, or execution fails
   */
  IresultDto processCommand(String command) throws Exception;
}