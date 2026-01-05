package calendar.view;

import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;

/**
 * Console-based view implementation for displaying calendar application output.
 * This view handles all output to the console including messages, errors, and formatted results.
 * It uses a ResultFormatter to convert result objects into displayable strings.
 * Note: This view only handles output, not input.
 */
public class ConsoleView implements Iview {

  private final ResultFormatter resultFormatter;

  /**
   * Constructs a new ConsoleView with a default ResultFormatter.
   */
  public ConsoleView() {
    this.resultFormatter = new ResultFormatter();
  }

  /**
   * Displays a message to standard output.
   * Null or empty messages are ignored.
   *
   * @param message the message to display
   */
  @Override
  public void display(String message) {
    if (message != null && !message.isEmpty()) {
      System.out.println(message);
    }
  }

  /**
   * Displays an error message to standard error stream.
   * Error messages are prefixed with "Error: ".
   *
   * @param errorMessage the error message to display
   */
  @Override
  public void displayError(String errorMessage) {
    System.err.println("Error: " + errorMessage);
  }

  /**
   * Displays a formatted result object to the console.
   * The result is first formatted using ResultFormatter, then displayed.
   *
   * @param result the result object to format and display
   */
  @Override
  public void displayResult(IresultDto result) {
    String output = resultFormatter.format(result);
    display(output);
  }
}