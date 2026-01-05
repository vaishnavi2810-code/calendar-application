package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.Calendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.service.CommandParserService;
import calendar.view.ResultFormatter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import mocks.MockCommandParserService;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for EventController with real parsers and commands.
 * Tests end-to-end command processing with actual command syntax.
 */
public class EventControllerIntegrationTest {

  private EventController controller;
  private Icalendarcollection repository;
  private CalendarModel service;
  private MockCommandParserService mockParserService;
  private Iview mockView;
  private IinputSource mockInputSource;
  private ResultFormatter formatter;
  private CommandParserService parserService;
  private PrintStream originalOut;
  private ByteArrayOutputStream setupOutput;

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

    // Create mocks
    Iview mockView = new calendar.test.MockView();
    IinputSource mockInputSource = new calendar.test.MockInputSource();

    // Create components
    parserService = new CommandParserService();
    service = new CalendarModel(new CalendarCollection());
    controller = new EventController(mockInputSource, service, parserService, mockView);
    formatter = new ResultFormatter();
    controller.processCommand("create calendar --name \"Test Calendar\" "
        + "--timezone America/New_York");
    controller.processCommand("use calendar --name \"Test Calendar\"");

    System.setOut(originalOut);
  }

  @Test
  public void testCreateCalendar() throws Exception {
    controller.processCommand("create calendar --name work --timezone America/New_York");
    assertTrue(service.checkCalendarModel("work"));
  }

  @Test
  public void testUseCalendar() throws Exception {
    service.createNewCalendar("personal", "Europe/London");
    controller.processCommand("use calendar --name personal");
  }

  @Test
  public void testCreateSingleTimedEvent() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Meeting from 2024-01-15T10:00 to 2024-01-15T11:00");
  }

  @Test
  public void testCreateSingleAllDayEvent() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Conference on 2024-01-15");
  }

  @Test
  public void testCreateRecurringTimedEventForN() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Standup from 2024-01-15T09:00 "
            +
            "to 2024-01-15T09:30 repeats MWF for 5 times");
  }

  @Test
  public void testCreateRecurringTimedEventUntil() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event TeamMeeting from 2024-01-15T14:00 to "
            +
            "2024-01-15T15:00 repeats TR until 2024-02-15");
  }

  @Test
  public void testCreateRecurringAllDayEventForN() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Holiday on "
            +
            "2024-01-15 repeats F for 3 times");
  }

  @Test
  public void testCreateRecurringAllDayEventUntil() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Vacation on 2024-01-15 "
            +
            "repeats MTWRF until 2024-01-20");
  }

  @Test
  public void testQueryEventsOnDate() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Meeting from 2024-01-15T10:00 to "
            +
            "2024-01-15T11:00");
    controller.processCommand("print events on 2024-01-15");
  }

  @Test
  public void testQueryEventsInRange() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Meeting from 2024-01-15T10:00 to "
            +
            "2024-01-15T11:00");
    controller.processCommand("create event Lunch from 2024-01-16T12:00 to 2024-01-16T13:00");
    controller.processCommand("print events from 2024-01-15T00:00 to 2024-01-20T23:59");
  }

  @Test
  public void testCheckStatusAt() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Meeting from"
            +
            " 2024-01-15T10:00 to 2024-01-15T11:00");
    controller.processCommand("show status on 2024-01-15T10:30");
  }

  @Test
  public void testEditCalendarName() throws Exception {
    service.createNewCalendar("oldName", "America/New_York");
    controller.processCommand("edit calendar --name oldName --property name newName");
    assertTrue(service.checkCalendarModel("newName"));
    assertFalse(service.checkCalendarModel("oldName"));
  }

  @Test
  public void testEditCalendarTimezone() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    controller.processCommand("edit calendar --name test --property "
            +
            "timezone America/Los_Angeles");
  }

  @Test
  public void testExportToCsv() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Meeting from "
            +
            "2024-01-15T10:00 to 2024-01-15T11:00");
    controller.processCommand("export cal test.csv");
  }

  @Test
  public void testExportToIcal() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Meeting from "
            +
            "2024-01-15T10:00 to 2024-01-15T11:00");
    controller.processCommand("export cal test.ical");
  }

  @Test
  public void testUnknownCommandThrowsException() {
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("gibberish nonsense command");
    });
    assertTrue(ex.getMessage().contains("Unknown command"));
  }

  @Test
  public void testInvalidTimezoneThrowsException() {
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("create calendar --name test --timezone Invalid/Timezone");
    });
    assertTrue(ex.getMessage().contains("Invalid Time Zone"));
  }

  @Test
  public void testDuplicateCalendarThrowsException() throws Exception {
    controller.processCommand("create calendar --name duplicate"
            +
            " --timezone America/New_York");
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("create calendar --name duplicate"
              +
              " --timezone America/New_York");
    });
    assertTrue(ex.getMessage().contains("already exists"));
  }

  @Test
  public void testCreateEventWithoutActiveCalendarFails() throws Exception {
    // Create a fresh controller without setting an active calendar
    Icalendarcollection freshRepository = new CalendarCollection();
    CalendarModel freshService = new CalendarModel(freshRepository);
    CommandParserService freshParser = new CommandParserService();
    Iview freshMockView = new calendar.test.MockView();
    IinputSource freshMockInputSource = new calendar.test.MockInputSource();
    EventController freshController = new EventController(
        freshMockInputSource, freshService, freshParser, freshMockView);
    freshController.processCommand("create calendar --name Test --timezone America/New_York");
    Exception ex = assertThrows(Exception.class, () -> {
      freshController.processCommand("create event Meeting from 2024-01-15T10:00"
          + " to 2024-01-15T11:00");
    });

    assertNotNull(ex);
    assertTrue("Should indicate no active calendar",
        ex.getMessage().contains("No calendar")
            || ex.getMessage().contains("not selected")
            || ex.getMessage().contains("Calendar not found"));
  }

  @Test
  public void testUseNonExistentCalendarThrowsException() {
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("use calendar --name nonexistent");
    });
    assertTrue(ex.getMessage().contains("not found"));
  }

  /**
   * Verifies that updating the calendar's timezone correctly shifts the wall-clock time
   * of existing events to align with the new offset.
   *
   * <p>Test Scenario:
   * 1. Create a calendar in 'Europe/London' (UTC+0 in Jan).
   * 2. Create an event at 15:00 London time.
   * 3. Update the calendar timezone to 'America/New_York' (UTC-5 in Jan).
   * 4. Verify the calendar metadata is updated.
   * 5. Verify the event start time shifts -5 hours to 10:00.
   *
   * @throws Exception if command processing or service queries fail.
   */
  @Test
  public void testTimezoneUpdateShiftsEventTime() throws Exception {
    controller.processCommand("create calendar --name LondonCal --timezone Europe/London");
    controller.processCommand("use calendar --name LondonCal");
    controller.processCommand("create event TeaTime from 2024-01-15T15:00 to 2024-01-15T16:00");
    controller.processCommand("edit calendar --name LondonCal"
            +
            " --property timezone America/New_York");
    assertEquals("America/New_York", service.getCalendarTimezone("LondonCal"));
    calendar.dto.QueryEventDto queryDto = calendar.dto.QueryEventDto.forDate("2024-01-15");
    java.util.Set<calendar.model.Event> events = service.queryEvent(queryDto);
    calendar.model.Event event = events.iterator().next();
    assertEquals(java.time.LocalTime.of(10, 0), event.getStartDateTime().toLocalTime());
  }

  @Test
  public void testCompleteWorkflow() throws Exception {
    controller.processCommand("create calendar --name work --timezone America/New_York");
    controller.processCommand("use calendar --name work");
    controller.processCommand("create event Standup from 2024-01-15T09:00 to 2024-01-15T09:30");
    controller.processCommand("create event Lunch on 2024-01-15");
    controller.processCommand("create event TeamMeeting from 2024-01-15T14:00 "
            +
            "to 2024-01-15T15:00 repeats MWF for 3 times");
    controller.processCommand("print events on 2024-01-15");
    controller.processCommand("print events from 2024-01-15T00:00 to 2024-01-20T23:59");
    controller.processCommand("show status on 2024-01-15T14:30");
    controller.processCommand("show status on 2024-01-15T16:00");
    controller.processCommand("export cal work.csv");
    controller.processCommand("export cal work.ical");
  }

  @Test
  public void testWeekdaysParsing() throws Exception {
    service.createNewCalendar("test", "America/New_York");
    service.setActiveCalendar("test");
    controller.processCommand("create event Monday from 2024-01-15T10:00 to "
            +
            "2024-01-15T11:00 repeats M for 2 times");
    controller.processCommand("create event Tuesday from 2024-01-16T10:00 "
            +
            "to 2024-01-16T11:00 repeats T for 2 times");
    controller.processCommand("create event Wednesday from 2024-01-17T10:00 "
            +
            "to 2024-01-17T11:00 repeats W for 2 times");
    controller.processCommand("create event Thursday from 2024-01-18T10:00 "
            +
            "to 2024-01-18T11:00 repeats R for 2 times");
    controller.processCommand("create event Friday from 2024-01-19T10:00 "
            +
            "to 2024-01-19T11:00 repeats F for 2 times");
    controller.processCommand("create event Saturday from 2024-01-20T10:00 to"
            +
            " 2024-01-20T11:00 repeats S for 2 times");
    controller.processCommand("create event Sunday from 2024-01-21T10:00 "
            +
            "to 2024-01-21T11:00 repeats U for 2 times");
  }

  /**
   * Verifies that the system prevents a timezone update if it would cause an existing event
   * to shift to a different day of the week (e.g., Monday -> Tuesday).
   *
   * <p>Test Scenario:
   * 1. Create a calendar in 'America/New_York' (UTC-5).
   * 2. Create an event at 23:00 (11 PM) on a Monday.
   * 3. Attempt to update the timezone to 'Europe/London' (UTC+0).
   * Note: 23:00 NY time is 04:00 Tuesday in London.
   * 4. Verify that the operation fails with a specific error message about the day shift.
   */
  @Test
  public void testTimezoneUpdateFailsIfDayChanges() {
    try {
      controller.processCommand("create calendar --name LateNight --timezone America/New_York");
      controller.processCommand("use calendar --name LateNight");
      controller.processCommand("create event NightOwl from "
              +
              "2024-01-15T23:00 to 2024-01-15T23:59");
      Exception ex = assertThrows(Exception.class, () -> {
        controller.processCommand("edit calendar --name LateNight "
                +
                "--property timezone Europe/London");
      });
      assertTrue(ex.getMessage().contains("shift from MONDAY to TUESDAY"));
    } catch (Exception e) {
      e.printStackTrace();
      org.junit.Assert.fail("Setup failed: " + e.getMessage());
    }
  }

  /**
   * Verifies that the system prevents a timezone update if the time shift causes
   * an event's start date and end date to differ (i.e., it spans across midnight).
   *
   * <p>Test Scenario:
   * 1. Create a calendar in 'UTC'.
   * 2. Create an event from 22:00 to 23:00 (Same day: 2025-01-01).
   * 3. Attempt to switch timezone to 'Europe/Paris' (UTC+1).
   * - New Start: 23:00 (2025-01-01)
   * - New End:   00:00 (2025-01-02)
   * 4. Verify that the system throws the specific.
   * "Start Date and End Date should not differ" exception.
   */
  @Test
  public void testUpdateFailsIfEventSpansMidnight() throws Exception {
    controller.processCommand("create calendar --name MidnightCal --timezone UTC");
    controller.processCommand("use calendar --name MidnightCal");
    controller.processCommand("create event LateNight from "
            +
            "2025-01-01T22:00 to 2025-01-01T23:00");
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("edit calendar --name MidnightCal "
              +
              "--property timezone Europe/Paris");
    });
    assertTrue(ex.getMessage().contains("Start Date and End Date should not differ"));
  }

  /**
   * Verifies that attempting to update the timezone of a non-existent calendar
   * throws the correct "Calendar not found" exception.
   */
  @Test
  public void testUpdateFailsForNonExistentCalendar() {
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("edit calendar --name GhostCal --property timezone UTC");
    });

    assertTrue(ex.getMessage().contains("Calendar not found"));
  }

  /**
   * Verifies that attempting to update a calendar with an invalid timezone string
   * throws the correct "Invalid Time Zone" exception.
   */
  @Test
  public void testUpdateFailsForInvalidTimezone() throws Exception {
    controller.processCommand("create calendar --name BadZoneCal --timezone UTC");
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("edit calendar --name BadZoneCal --property timezone Mars/Phobos");
    });

    assertTrue(ex.getMessage().contains("Invalid Time Zone"));
    assertTrue(ex.getMessage().contains("Mars/Phobos"));
  }

  /**
   * Verifies that updating a calendar to its current timezone results in a "no-op" (no operation).
   *
   * <p>Test Strategy:
   * 1. Create a calendar with a specific timezone (UTC).
   * 2. Capture the memory reference of the ModelSeries object from the repository.
   * 3. execute the 'edit calendar' command using the SAME timezone (UTC).
   * 4. Verify that the ModelSeries object in the repository is the EXACT SAME instance (using ==).
   * This proves the code hit the 'return' statement.
   * and did not build/save a new model.
   * 5. Verify control case: Change the timezone to.
   * something else and prove the reference DOES change.
   *
   * @throws Exception if service calls fail.
   */
  @Test
  public void testTimezoneUpdateIgnoredIfSameAsCurrent() throws Exception {
    controller.processCommand("create calendar --name IdentityCal --timezone UTC");
    Calendar originalRef = service.calendarModel("IdentityCal");
    controller.processCommand("edit calendar --name IdentityCal --property timezone UTC");
    Calendar newRef = service.calendarModel("IdentityCal");
    assertTrue("The ModelSeries instance should remain "
                    +
                    "unchanged (same memory address)",
            originalRef == newRef);
    controller.processCommand("edit calendar --name IdentityCal "
            +
            "--property timezone Europe/London");
    Calendar changedRef = service.calendarModel("IdentityCal");
    assertFalse("The ModelSeries instance MUST"
                    +
                    " change when timezone is actually updated",
            newRef == changedRef);
    assertEquals("Europe/London", service.getCalendarTimezone("IdentityCal"));
  }

  /**
   * Verifies that exporting fails with "Calendar not found" if the currently active calendar
   * cannot be found in the repository (e.g., if it was renamed or deleted).
   *
   * <p>Test Scenario:
   * 1. Create a calendar named 'GhostCal'.
   * 2. Set 'GhostCal' as the active calendar.
   * 3. Rename 'GhostCal' to 'RealCal'.
   * - The Repository now contains 'RealCal'.
   * - The Service still thinks 'GhostCal' is the active calendar name.
   * 4. Attempt to export events.
   * 5. Verify that the service tries to look up 'GhostCal', gets null, and throws the exception.
   */
  @Test
  public void testExportFailsIfActiveCalendarIsMissing() throws Exception {
    controller.processCommand("create calendar --name GhostCal --timezone UTC");
    controller.processCommand("use calendar --name GhostCal");
    controller.processCommand("edit calendar --name GhostCal --property name RealCal");
    Exception ex = assertThrows(Exception.class, () -> {
      controller.processCommand("export cal out.csv");
    });
    assertTrue(ex.getMessage().contains("Calendar not found"));
  }
}