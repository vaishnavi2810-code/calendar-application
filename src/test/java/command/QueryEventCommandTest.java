package command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.service.CommandParserService;
import calendar.view.ResultFormatter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests Query Event functionality using the new architecture.
 * This is an integration test that verifies the full flow from
 * controller -> command -> DTO -> formatter -> final string output.
 */
public class QueryEventCommandTest {

  private PrintStream originalOut;
  private ByteArrayOutputStream setupOutput;

  private CalendarModel service;
  private EventController controller;
  private CommandParserService parserService;
  private ResultFormatter formatter;


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * ok.
   *
   * @throws Exception ok.
   */
  @Before
  public void setUp() throws Exception {
    originalOut = System.out;
    setupOutput = new ByteArrayOutputStream();
    System.setOut(new PrintStream(setupOutput));
    Iview mockView = new calendar.test.MockView();
    IinputSource mockInputSource = new calendar.test.MockInputSource();
    parserService = new CommandParserService();
    service = new CalendarModel(new CalendarCollection());
    controller = new EventController(mockInputSource, service, parserService, mockView);
    formatter = new ResultFormatter();
    controller.processCommand("create calendar --name \"Test Calendar\" "
        + "--timezone America/New_York");
    controller.processCommand("use calendar --name \"Test Calendar\"");
    System.setOut(originalOut);
  }

  /**
   * ok.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
  }


  @Test
  public void testExecutePrintOnDateWithEvents() throws Exception {
    controller.processCommand("create event \"Morning Meeting\""
            +
            " from 2025-11-15T09:00 to 2025-11-15T10:00");
    controller.processCommand("create event \"Lunch Break\" "
            +
            "from 2025-11-15T12:00 to 2025-11-15T13:00");

    IresultDto resultDto = controller.processCommand("print events on 2025-11-15");

    String output = formatter.format(resultDto);

    assertTrue(output.contains("Query results:"));
    assertTrue(output.contains("Morning Meeting"));
    assertTrue(output.contains("Lunch Break"));
  }

  @Test
  public void testExecutePrintOnDateNoEvents() throws Exception {
    IresultDto resultDto = controller.processCommand("print events on 2025-11-15");
    String output = formatter.format(resultDto);
    assertEquals("No events found.", output.trim());
  }

  @Test
  public void testExecutePrintInRangeWithEvents() throws Exception {
    controller.processCommand("create event \"Multi-day Conference\" "
            +
            "from 2025-11-15T09:00 to 2025-11-17T17:00");

    IresultDto resultDto = controller.processCommand("print events from "
            +
            "2025-11-14T00:00 to 2025-11-18T23:59");

    String output = formatter.format(resultDto);

    assertTrue(output.contains("Query results:"));
    assertTrue(output.contains("Multi-day Conference"));
    assertTrue(output.contains("starting on"));
    assertTrue(output.contains("ending on"));
  }

  @Test
  public void testExecutePrintInRangeNoEvents() throws Exception {

    IresultDto resultDto = controller.processCommand("print events from 2025-11-14T00:00 "
            +
            "to 2025-11-18T23:59");
    String output = formatter.format(resultDto);
    assertEquals("No events found.", output.trim());
  }

  @Test
  public void testExecuteShowStatusAtBusy() throws Exception {
    controller.processCommand("create event \"Important Meeting\" "
            +
            "from 2025-11-15T14:00 to 2025-11-15T15:00");
    IresultDto resultDto = controller.processCommand("show status on 2025-11-15T14:30");
    String output = formatter.format(resultDto);
    assertEquals("busy", output.trim());
  }

  @Test
  public void testExecuteShowStatusAtAvailable() throws Exception {
    controller.processCommand("create event \"Meeting\" from "
            +
            "2025-11-15T14:00 to 2025-11-15T15:00");
    IresultDto resultDto = controller.processCommand("show status on 2025-11-15T16:00");
    String output = formatter.format(resultDto);
    assertEquals("available", output.trim());
  }

  @Test
  public void testExecutePrintOnDateNullLocation() throws Exception {
    controller.processCommand("create event \"Phone Call\" "
            +
            "from 2025-11-15T10:00 to 2025-11-15T10:30");
    IresultDto resultDto = controller.processCommand("print events on 2025-11-15");
    String output = formatter.format(resultDto);
    assertTrue(output.contains("Phone Call"));
    assertFalse(output.contains(" at "));
  }
}