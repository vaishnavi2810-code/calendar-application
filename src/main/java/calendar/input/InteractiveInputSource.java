package calendar.input;

import calendar.interfacetypes.IinputSource;
import java.util.Scanner;

/**
 * Input source for interactive mode using Scanner.
 * Reads commands from System.in (console input).
 */
public class InteractiveInputSource implements IinputSource {

  private final Scanner scanner;

  /**
   * Constructs a new InteractiveInputSource that reads from standard input.
   */
  public InteractiveInputSource() {
    this.scanner = new Scanner(System.in);
  }

  /**
   * Retrieves the next command from the console input.
   * Prompts the user with a specific character before reading the line.
   *
   * @return the trimmed command string, or null if no more input is available
   */
  @Override
  public String getNextCommand() {
    if (!scanner.hasNextLine()) {
      return null;
    }
    System.out.print("> ");
    return scanner.nextLine().trim();
  }

  /**
   * Checks if there are more commands available to be read from the input source.
   *
   * @return true if there is another line of input, false otherwise
   */
  @Override
  public boolean hasMoreCommands() {
    return scanner.hasNextLine();
  }

  /**
   * Closes the input source and the underlying Scanner.
   */
  @Override
  public void close() {
    scanner.close();
  }
}