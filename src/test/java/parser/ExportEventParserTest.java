package parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.command.ExportEventCommand;
import calendar.controller.EventController;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.Calendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.parser.ExportEventParser;
import calendar.service.CommandParserService;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ExportEventParser.
 * Tests parsing of export commands and pattern matching.
 */
public class ExportEventParserTest {


  private ExportEventParser parser;
  EventController eventController;
  Icalendarcollection repository;
  CalendarModel calendarModel;
  CommandParserService commandParserService;
  Map<String, Calendar> modelSeriesMap;
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

    parser = new ExportEventParser();
  }

  @Test
  public void testCanHandleValidExportCommands() {
    assertTrue(parser.canHandle("export cal my_calendar.csv"));
    assertTrue(parser.canHandle("export cal output.csv"));
    assertFalse(parser.canHandle("create event Test"));
    assertFalse(parser.canHandle("print events on 2025-12-01"));
  }

  @Test
  public void testParseExportCommand() throws Exception {
    String command = "export cal my_calendar.csv";
    Icommand cmd = parser.parse(command, calendarModel);

    assertNotNull(cmd);
    assertTrue(cmd instanceof ExportEventCommand);
  }

  @Test
  public void testParseExportDifferentFilename() throws Exception {
    String command = "export cal december_schedule.csv";
    Icommand cmd = parser.parse(command, calendarModel);

    assertNotNull(cmd);
    assertTrue(cmd instanceof ExportEventCommand);
  }

  @Test
  public void testParseInvalidCommandThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("export invalid format", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Error: file type '.export invalid format' "
        + "is not supported."));
  }

  @Test
  public void testParseMissingFilenameThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      parser.parse("export cal", calendarModel);
    });
    assertTrue(exception.getMessage().contains("Error: file type '.export cal' is not supported."));
  }
}