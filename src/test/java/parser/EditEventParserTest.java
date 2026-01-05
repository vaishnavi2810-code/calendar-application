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
import calendar.parser.EditEventParser;
import calendar.service.CommandParserService;
import org.junit.Before;
import org.junit.Test;


/**
 * Test class for EditEventParser.
 * Tests parsing of edit commands and pattern matching.
 */
public class EditEventParserTest {

  private CalendarModel calendarModel;
  private CommandParserService commandParserService;
  private Icalendarcollection repository;
  private EditEventParser parser;
  private EventController eventController;
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
    // Create controller
    eventController = new EventController(mockInputSource, calendarModel,
        commandParserService, mockView);
    // Set up test calendar
    eventController.processCommand("create calendar --name \"Test Calendar\""
        + " --timezone America/Los_Angeles");
    eventController.processCommand("use calendar --name \"Test Calendar\"");
    parser = new EditEventParser();
  }

  @Test
  public void testCanHandleValidEditCommands() {
    assertTrue(parser.canHandle("edit event location \"Test\" from 2025-12-01T10:00 "
        + "to 2025-12-01T11:00 with \"Room\""));
    assertTrue(parser.canHandle("edit events subject \"Test\" from 2025-12-01T10:00 with \"New\""));
    assertTrue(parser.canHandle("edit series description \"Test\" from 2025-12-01T10:00 "
        + "with \"Desc\""));
    assertFalse(parser.canHandle("create event Test"));
    assertFalse(parser.canHandle("print events on 2025-12-01"));
    assertFalse(parser.canHandle(null));
  }

  @Test
  public void testParseEditSingleEvent() throws Exception {
    String command = "edit event location \"Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00 "
        + "with \"Room 5\"";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  @Test
  public void testParseEditForwardEvents() throws Exception {
    String command = "edit events subject \"Standup\" from 2025-12-01T09:00 with \"Daily Standup\"";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  @Test
  public void testParseEditSeries() throws Exception {
    String command = "edit series description \"Review\" from 2025-12-01T15:00 with \"Updated\"";
    Icommand cmd = parser.parse(command, calendarModel);
    assertNotNull(cmd);
  }

  @Test
  public void testParseInvalidCommandThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("edit event invalid format", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Invalid edit command format"));
  }
}