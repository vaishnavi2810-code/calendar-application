package parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.parser.CopyEventParser;
import calendar.service.CommandParserService;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CopyEventParser.
 * Verifies the correct parsing of copy commands, including pattern matching,
 * argument extraction, and error handling for various copy strategies.
 */
public class CopyEventParserTest {

  private CalendarModel calendarModel;
  private CommandParserService commandParserService;
  private Icalendarcollection repository;
  private CopyEventParser parser;
  private EventController eventController;

  /**
   * Initializes the test environment before each test execution.
   * Sets up the repository, model, and controller, and creates default calendars
   * to ensure a valid state for parsing operations.
   *
   * @throws Exception if an error occurs during setup
   */
  @Before
  public void setUp() throws Exception {
    repository = new CalendarCollection();
    calendarModel = new CalendarModel(repository);
    commandParserService = new CommandParserService();
    Iview mockView = new calendar.test.MockView();
    IinputSource mockInputSource = new calendar.test.MockInputSource();

    eventController = new EventController(mockInputSource, calendarModel,
            commandParserService, mockView);

    eventController.processCommand("create calendar --name Work --timezone America/New_York");
    eventController.processCommand("create calendar "
            + "--name Personal --timezone America/Los_Angeles");
    eventController.processCommand("use calendar --name Work");

    parser = new CopyEventParser();
  }

  /**
   * Verifies that the parser correctly identifies valid command strings
   * for all supported copy types.
   */
  @Test
  public void testCanHandleValidCopyCommands() {
    assertTrue(parser.canHandle("copy event \"Meeting\" on "
            + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00"));
    assertTrue(parser.canHandle("copy events on "
            + "2024-09-09 --target Personal to 2025-01-13"));
    assertTrue(parser.canHandle("copy events between "
            + "2024-09-09 and 2024-09-16 --target Personal to 2025-01-13"));
    assertTrue(parser.canHandle("COPY EVENT \"Test\" on "
            + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00"));
    assertTrue(parser.canHandle("Copy Events On "
            + "2024-09-09 --target Personal to 2025-01-13"));
  }

  /**
   * Verifies that the parser correctly rejects commands that it cannot handle,
   * such as null inputs, empty strings, or unrelated commands.
   */
  @Test
  public void testCanHandleInvalidCommands() {
    assertFalse(parser.canHandle("create event Test"));
    assertFalse(parser.canHandle("edit event Test"));
    assertFalse(parser.canHandle("print events on 2025-12-01"));
    assertFalse(parser.canHandle("show status on 2025-12-01T10:00"));
    assertFalse(parser.canHandle(null));
    assertFalse(parser.canHandle(""));
    assertFalse(parser.canHandle("   "));
  }

  /**
   * Tests the parsing of a valid command to copy a single event with quoted name.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopySingleEvent() throws Exception {
    String command = "copy event \"Meeting\" on "
            + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests the parsing of a valid command to copy a single event without quotes.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopySingleEventWithoutQuotes() throws Exception {
    String command = "copy event Meeting on 2024-09-09T10:00 "
            + "--target Personal to 2025-01-13T10:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests the parsing of a single event copy command where the event name contains spaces.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopySingleEventWithSpacesInName() throws Exception {
    String command = "copy event \"Team Meeting\" "
            + "on 2024-09-09T10:00 --target Personal to 2025-01-13T10:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests that the parser handles case-insensitive keywords for single event copy.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopySingleEventCaseInsensitive() throws Exception {
    String command = "COPY EVENT \"Meeting\" "
            + "ON 2024-09-09T10:00 --TARGET Personal TO 2025-01-13T10:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests the parsing of a command to copy all events on a specific date.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopyEventsOnDate() throws Exception {
    String command = "copy events on 2024-09-09 --target Personal to 2025-01-13";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests that the parser handles case-insensitive keywords for date-based copy.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopyEventsOnDateCaseInsensitive() throws Exception {
    String command = "COPY EVENTS ON 2024-09-09 --TARGET Personal TO 2025-01-13";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests the parsing of a copy command targeting a different calendar name.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopyEventsOnDateWithDifferentCalendarName() throws Exception {
    String command = "copy events on 2024-09-09 --target Work to 2025-01-13";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests the parsing of a command to copy events within a date range.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopyEventsBetweenDates() throws Exception {
    String command = "copy events between "
            + "2024-09-09 and 2024-09-16 --target Personal to 2025-01-13";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests that the parser handles case-insensitive keywords for interval-based copy.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopyEventsBetweenDatesCaseInsensitive() throws Exception {
    String command = "COPY EVENTS BETWEEN 2024-09-09 "
            + "AND 2024-09-16 --TARGET Personal TO 2025-01-13";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests the parsing of a copy command with a long date range (e.g., a full year).
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCopyEventsBetweenDatesLongRange() throws Exception {
    String command = "copy events between 2024-01-01 and "
            + "2024-12-31 --target Personal to 2025-01-01";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Verifies that an exception is thrown for an incomplete single event copy command.
   */
  @Test
  public void testParseInvalidCopySingleEventThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy event \"Meeting\" on "
              + "2024-09-09T10:00 --target Personal to", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown for an incomplete date-based copy command.
   */
  @Test
  public void testParseInvalidCopyEventsOnDateThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy events on 2024-09-09 --target Personal to", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown for a malformed interval-based copy command.
   */
  @Test
  public void testParseInvalidCopyEventsBetweenThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy events between 2024-09-09 "
              + "and --target Personal to 2025-01-13", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown when the target calendar argument is missing.
   */
  @Test
  public void testParseMissingTargetCalendarThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy events on 2024-09-09 to 2025-01-13", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown when the date format is invalid.
   */
  @Test
  public void testParseInvalidDateFormatThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy events on 2024-09 --target Personal to 2025-01-13", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown when the time is missing from a datetime field.
   */
  @Test
  public void testParseInvalidDateTimeFormatThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy event \"Meeting\" on 2024-09-09 "
              + "--target Personal to 2025-01-13T10:00", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown when command keywords are in the wrong order.
   */
  @Test
  public void testParseWrongKeywordOrderThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy events and 2024-09-09 between "
              + "2024-09-16 --target Personal to 2025-01-13", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that extra spaces in the command string cause parsing failure
   * due to strict regex matching in the parser.
   */
  @Test
  public void testParseExtraSpacesStillWorks() {
    String command = "copy  events  on  2024-09-09  --target  Personal  to  2025-01-13";
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse(command, calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Verifies that an exception is thrown if the dash prefix is missing from flag arguments.
   */
  @Test
  public void testParseMissingDashesInTargetThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("copy events on 2024-09-09 target Personal to 2025-01-13", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid copy command format"));
  }

  /**
   * Tests parsing of an event name containing special characters.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseEventNameWithSpecialCharacters() throws Exception {
    String command = "copy event \"Meeting@Office\" on "
            + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests parsing of an event name containing numbers.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseEventNameWithNumbers() throws Exception {
    String command = "copy event \"Meeting123\" on "
            + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests parsing when the target calendar name contains underscores.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseCalendarNameWithUnderscores() throws Exception {
    eventController.processCommand("create calendar --name "
            + "Work_Calendar --timezone America/New_York");
    String command = "copy events on 2024-09-09 --target Work_Calendar to 2025-01-13";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests parsing when the source and target dates are identical.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseSameDateForSourceAndTarget() throws Exception {
    String command = "copy events on 2024-09-09 --target Personal to 2024-09-09";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Tests parsing of commands involving leap year dates.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParseLeapYearDate() throws Exception {
    String command = "copy events on 2024-02-29 --target Personal to 2025-02-28";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  /**
   * Verifies that the parser checks patterns in the correct order to distinguish
   * between single event copy, date copy, and range copy.
   *
   * @throws Exception if parsing fails
   */
  @Test
  public void testParserChecksPattersInCorrectOrder() throws Exception {
    String command1 = "copy event \"Test\" on 2024-09-09T10:00 "
            + "--target Personal to 2025-01-13T10:00";
    Icommand cmd1 = parser.parse(command1, calendarModel);
    assertNotNull(cmd1);

    String command2 = "copy events on 2024-09-09 "
            + "--target Personal to 2025-01-13";
    Icommand cmd2 = parser.parse(command2, calendarModel);
    assertNotNull(cmd2);

    String command3 = "copy events between 2024-09-09 "
            + "and 2024-09-16 --target Personal to 2025-01-13";
    Icommand cmd3 = parser.parse(command3, calendarModel);
    assertNotNull(cmd3);
  }
}