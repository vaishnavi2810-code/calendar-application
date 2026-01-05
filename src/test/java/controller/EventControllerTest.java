package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import mocks.MockCommand;
import mocks.MockCommandParserService;
import mocks.MockParser;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the EventController class for command processing,
 * parser selection, and error handling using mocks.
 */
public class EventControllerTest {

  private EventController controller;
  private Icalendarcollection repository;
  private CalendarModel calendarModel;
  private MockCommandParserService mockParserService;

  /**
   * Initializes the test environment before each test execution.
   * Sets up the repository, calendar model, mock parser service, and
   * the event controller to ensure a clean state.
   *
   * @throws Exception if initialization of dependencies fails
   */
  @Before
  public void setUp() throws Exception {
    repository = new CalendarCollection();
    calendarModel = new CalendarModel(repository);
    mockParserService = new MockCommandParserService();
    Iview mockView = new calendar.test.MockView();
    IinputSource mockInputSource = new calendar.test.MockInputSource();
    controller = new EventController(mockInputSource, calendarModel, mockParserService, mockView);
  }

  /**
   * Verifies that `processCommand` correctly identifies a matching parser
   * and executes the resulting command.
   *
   * @throws Exception if parsing or execution fails
   */
  @Test
  public void testProcessCommandFindsParserAndExecutes() throws Exception {
    MockCommand mockCommand = new MockCommand();
    MockParser mockParser = new MockParser("create event", mockCommand);

    mockParserService.addParser(mockParser);

    controller.processCommand("create event test");

    assertEquals(1, mockParser.getParseCallCount());
    assertEquals("create event test", mockParser.getLastParsedCommand());
    assertTrue(mockCommand.wasExecuted());
    assertEquals(1, mockCommand.getExecuteCallCount());
  }

  /**
   * Verifies that an exception is thrown when no parser can handle
   * the given command string.
   */
  @Test
  public void testProcessCommandThrowsExceptionWhenNoParserFound() {
    MockCommand mockCommand = new MockCommand();
    MockParser mockParser = new MockParser("create event", mockCommand);
    mockParser.setShouldHandle(false);

    mockParserService.clearParsers();
    mockParserService.addParser(mockParser);

    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("invalid command");
    });

    assertTrue(exception.getMessage().contains("Unknown command"));
  }

  /**
   * Verifies that the controller stops checking parsers after finding the first match.
   * This ensures priority order in the parser list is respected.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testProcessCommandStopsAtFirstMatch() throws Exception {
    MockCommand command1 = new MockCommand();
    MockCommand command2 = new MockCommand();
    MockParser parser1 = new MockParser("create event", command1);
    MockParser parser2 = new MockParser("create event", command2);

    mockParserService.clearParsers();
    mockParserService.addParser(parser1);
    mockParserService.addParser(parser2);

    controller.processCommand("create event test");

    assertEquals(1, parser1.getParseCallCount());
    assertEquals(0, parser2.getParseCallCount());
    assertTrue(command1.wasExecuted());
    assertFalse(command2.wasExecuted());
  }

  /**
   * Verifies that exceptions thrown during command execution are correctly
   * propagated up to the caller.
   *
   * @throws Exception to verify exception propagation
   */
  @Test
  public void testProcessCommandHandlesExecutionException() throws Exception {
    MockCommand mockCommand = new MockCommand();
    mockCommand.setExceptionToThrow(new Exception("Execution failed"));
    MockParser mockParser = new MockParser("create event", mockCommand);

    mockParserService.addParser(mockParser);

    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("create event test");
    });

    assertEquals("Execution failed", exception.getMessage());
  }

  /**
   * Verifies that the controller passes the correct `CalendarModel` service instance
   * to the parser during the parsing phase.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testProcessCommandPassesCorrectService() throws Exception {
    MockCommand mockCommand = new MockCommand();
    MockParser mockParser = new MockParser("create event", mockCommand);

    mockParserService.addParser(mockParser);

    controller.processCommand("create event test");

    // Verify the parser received the correct service
    assertEquals(calendarModel, mockParser.getLastServicePassed());
  }

  /**
   * Verifies that the controller can handle multiple distinct commands in sequence
   * using different parsers.
   *
   * @throws Exception if processing fails
   */
  @Test
  public void testProcessMultipleCommandsWithDifferentParsers() throws Exception {
    MockCommand createCommand = new MockCommand();
    MockCommand editCommand = new MockCommand();
    MockParser createParser = new MockParser("create", createCommand);
    MockParser editParser = new MockParser("edit", editCommand);

    mockParserService.addParser(createParser);
    mockParserService.addParser(editParser);

    controller.processCommand("create event test");
    controller.processCommand("edit event test");

    assertTrue(createCommand.wasExecuted());
    assertTrue(editCommand.wasExecuted());
    assertEquals(1, createParser.getParseCallCount());
    assertEquals(1, editParser.getParseCallCount());
  }

  /**
   * Verifies that an exception is thrown when the parser list is empty
   * and a command is issued.
   */
  @Test
  public void testProcessCommandWithEmptyParserList() {
    mockParserService.clearParsers();

    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("any command");
    });

    assertTrue(exception.getMessage().contains("Unknown command"));
  }

  /**
   * Verifies that the controller correctly delegates the parsing logic
   * to the `CommandParserService`.
   *
   * @throws Exception if processing fails
   */
  @Test
  public void testControllerDelegatesToParserService() throws Exception {
    MockCommand mockCommand = new MockCommand();
    MockParser mockParser = new MockParser("test", mockCommand);

    mockParserService.addParser(mockParser);

    controller.processCommand("test command");

    assertEquals(1, mockParser.getParseCallCount());
    assertTrue(mockCommand.wasExecuted());
  }

  /**
   * Verifies that the same command type can be executed multiple times in succession,
   * ensuring statelessness where appropriate.
   *
   * @throws Exception if processing fails
   */
  @Test
  public void testMultipleExecutionsOfSameCommand() throws Exception {
    MockCommand mockCommand = new MockCommand();
    MockParser mockParser = new MockParser("create", mockCommand);

    mockParserService.addParser(mockParser);

    controller.processCommand("create event 1");
    controller.processCommand("create event 2");
    controller.processCommand("create event 3");

    assertEquals(3, mockParser.getParseCallCount());
    assertEquals(3, mockCommand.getExecuteCallCount());
  }

  /**
   * Verifies that commands are executed synchronously in the exact order they are received.
   *
   * @throws Exception if processing fails
   */
  @Test
  public void testCommandExecutionOrderIsCorrect() throws Exception {
    StringBuilder executionOrder = new StringBuilder();

    MockCommand command1 = new MockCommand() {
      @Override
      public IresultDto execute() throws Exception {
        executionOrder.append("1");
        return super.execute();
      }
    };

    MockCommand command2 = new MockCommand() {
      @Override
      public IresultDto execute() throws Exception {
        executionOrder.append("2");
        return super.execute();
      }
    };

    MockParser parser1 = new MockParser("cmd1", command1);
    MockParser parser2 = new MockParser("cmd2", command2);

    mockParserService.addParser(parser1);
    mockParserService.addParser(parser2);

    controller.processCommand("cmd1");
    controller.processCommand("cmd2");

    assertEquals("12", executionOrder.toString());
  }
}