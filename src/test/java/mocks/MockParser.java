package mocks;

import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;

/**
 * Mock parser for testing EventController.
 * Allows control over parser behavior and tracks method calls.
 * (Updated to support the stateless Iparser interface)
 */
public class MockParser implements Iparser {

  private String expectedCommand;
  private Icommand commandToReturn;
  private boolean shouldHandle;
  private int parseCallCount = 0;
  private String lastParsedCommand;
  private CalendarModel lastServicePassed;

  /**
   * Creates a mock parser.
   *
   * @param expectedCommand the command prefix this parser handles
   * @param commandToReturn the command to return when parsing
   */
  public MockParser(String expectedCommand, Icommand commandToReturn) {
    this.expectedCommand = expectedCommand;
    this.commandToReturn = commandToReturn;
    this.shouldHandle = true;
  }

  @Override
  public boolean canHandle(String commandString) {
    return shouldHandle && commandString.startsWith(expectedCommand);
  }

  /**
   * It matches the Iparser interface: parse(String, EventController).
   */
  @Override
  public Icommand parse(String userInput, CalendarModel service) throws Exception {
    parseCallCount++;
    lastParsedCommand = userInput;
    lastServicePassed = service; // Store the controller for assertions
    return commandToReturn;
  }

  public int getParseCallCount() {
    return parseCallCount;
  }

  public String getLastParsedCommand() {
    return lastParsedCommand;
  }

  /**
   * New getter to let you assert that the parser
   * received the correct controller instance.
   */
  public CalendarModel getLastServicePassed() {
    return lastServicePassed;
  }

  public void setShouldHandle(boolean shouldHandle) {
    this.shouldHandle = shouldHandle;
  }
}