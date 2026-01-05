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
import calendar.parser.QueryEventParser;
import calendar.service.CommandParserService;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for QueryEventParser.
 * Tests parsing of query commands and pattern matching.
 */
public class QueryEventParserTest {

  private Icalendarcollection repository;
  private CalendarModel calendarModel;
  private QueryEventParser parser;
  private EventController eventController;
  private CommandParserService commandParserService;

  /**
   * Sets up test fixture before each test.
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
    eventController.processCommand("create calendar --name \"Test Calendar\""
        + " --timezone America/Los_Angeles");
    eventController.processCommand("use calendar --name \"Test Calendar\"");

    parser = new QueryEventParser();
  }

  @Test
  public void testCanHandleValidQueryCommands() {
    assertTrue(parser.canHandle("print events on 2025-12-01"));
    assertTrue(parser.canHandle("print events from 2025-12-01T10:00 to 2025-12-01T11:00"));
    assertTrue(parser.canHandle("show status on 2025-12-01T10:00"));
    assertFalse(parser.canHandle("create event Test"));
    assertFalse(parser.canHandle("edit event Test"));
    assertFalse(parser.canHandle(null));
  }

  @Test
  public void testParsePrintEventsOnDate() throws Exception {
    String command = "print events on 2025-12-01";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  @Test
  public void testParsePrintEventsInRange() throws Exception {
    String command = "print events from 2025-12-01T10:00 to 2025-12-01T17:00";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  @Test
  public void testParseShowStatus() throws Exception {
    String command = "show status on 2025-12-01T14:30";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  @Test
  public void testParseInvalidCommandThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("print events invalid format", calendarModel);
    });

    assertTrue(exception.getMessage().contains("Invalid query command format"));
  }
}