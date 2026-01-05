package calendar.interfacetypes;

/**
 * Interface representing a command in the Command pattern.
 * Implementations encapsulate specific calendar operations (create, edit, query, export)
 * as executable commands that can be invoked uniformly through the execute method.
 */
public interface Icommand {

  /**
   * Executes the encapsulated command operation and returns a "Response DTO".
   *
   * @return An IResultDto containing the result of the execution.
   * @throws Exception if the command execution fails for any reason
   */
  IresultDto execute() throws Exception;
}