package mocks;

/**
 * Mock controller for testing views.
 */
public class MockController {

  private int processCommandCallCount = 0;
  private String lastCommand;
  private Exception exceptionToThrow = null;

  /**
   * Sets exception to throw when processCommand is called.
   */
  public void setExceptionToThrow(Exception e) {
    this.exceptionToThrow = e;
  }

  /**
   * Simulates processing a command.
   */
  public void processCommand(String command) throws Exception {
    processCommandCallCount++;
    lastCommand = command;

    if (exceptionToThrow != null) {
      throw exceptionToThrow;
    }
  }

  /**
   * Gets number of times processCommand was called.
   */
  public int getProcessCommandCallCount() {
    return processCommandCallCount;
  }

  /**
   * Gets the last command processed.
   */
  public String getLastCommand() {
    return lastCommand;
  }
}