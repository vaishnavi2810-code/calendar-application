package calendar.interfacetypes;

/**
 * Interface for input sources in the calendar application.
 * Input sources are responsible for providing command strings to the controller.
 */
public interface IinputSource {

  /**
   * Gets the next command from the input source.
   *
   * @return the next command string, or null if no more input
   */
  String getNextCommand();

  /**
   * Checks if there are more commands available.
   *
   * @return true if more commands are available, false otherwise
   */
  boolean hasMoreCommands();

  /**
   * Closes the input source and releases any resources.
   */
  void close();
}