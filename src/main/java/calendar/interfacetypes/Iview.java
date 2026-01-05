package calendar.interfacetypes;

/**
 * Interface for view components that manage user interaction with the calendar system.
 * Implementations define different modes of interaction, such as interactive console-based
 * views for real-time user input or headless views for batch processing from command files.
 */
public interface Iview {
  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void display(String message);

  /**
   * Displays an error message to the user.
   *
   * @param errorMessage the error message to display
   */
  void displayError(String errorMessage);

  /**
   * Displays formatted result data to the user.
   *
   * @param result the result DTO to format and display
   */
  void displayResult(IresultDto result);
}
