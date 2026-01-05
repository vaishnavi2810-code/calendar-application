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
import calendar.parser.CreateEventParser;
import calendar.service.CommandParserService;
import org.junit.Before;
import org.junit.Test;


/**
 * Test class for CreateEventParser.
 * Tests command parsing and pattern matching.
 */
public class CreateEventParserTest {

  Icalendarcollection repository;
  CalendarModel calendarModel;
  EventController eventController;
  private CreateEventParser parser;
  CommandParserService commandParserService;

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
    parser = new CreateEventParser();
  }

  @Test
  public void testParserCanHandleValidCommand() {
    CreateEventParser parser = new CreateEventParser();

    assertTrue(parser.canHandle("create event \"Test\" from 2025-12-01T10:00 to 2025-12-01T11:00"));
    assertFalse(parser.canHandle("edit event test"));
    assertFalse(parser.canHandle("invalid command"));
  }

  @Test
  public void testParseSingleTimedEvent() throws Exception {
    CreateEventParser parser = new CreateEventParser();

    String command = "create event \"Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    Icommand cmd = parser.parse(command, calendarModel);

    assertNotNull(cmd);
  }

  @Test
  public void testParseInvalidCommandThrowsException() {
    CreateEventParser parser = new CreateEventParser();

    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("create event invalid format", calendarModel);
    });

    assertTrue(exception.getMessage().contains("Invalid create command format"));
  }
}