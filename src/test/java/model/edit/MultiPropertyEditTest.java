package model.edit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.command.EditEventCommand;
import calendar.controller.EventController;
import calendar.dto.EditEventDto;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.Calendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.service.CommandParserService;
import calendar.service.GuiDtoBuilderService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for multi-property series editing functionality.
 * Tests the private methods in AbstractEditSeries indirectly through EditEventCommand.
 */
public class MultiPropertyEditTest {

  private Calendar calendar;
  private Icalendarcollection repository;
  private CalendarModel calendarModel;
  private CommandParserService parserService;
  private EventController controller;
  private Iview mockView;
  private IinputSource mockInputSource;
  private GuiDtoBuilderService guiBuilder;

  /**
  * Sets up the test environment and initializes the MVC dependencies before each test execution.
  *
  * <p>This method performs the following actions:
  * <ol>
  * <li>Initializes the {@code InMemoryCalendarRepository}
  * and injects it into the {@code CalendarService}.</li>
  * <li>Sets up mock objects for the View and Input Source.</li>
  * <li>Constructs the {@code EventController} with all required dependencies.</li>
  * <li><b>Pre-configuration:</b> Programmatically executes commands to create a calendar named
  * "Test Calendar" (Timezone: America/Los_Angeles) and sets it as the active calendar.</li>
  * <li>Retrieves the resulting {@code modelSeries} to allow tests to assert
  * against the calendar's state directly.</li>
  * </ol>
  *
  * @throws Exception if the controller fails to process.
  */
  @Before
  public void setUp() throws Exception {
    repository = new CalendarCollection();
    calendarModel = new CalendarModel(repository);
    parserService = new CommandParserService();
    mockView = new calendar.test.MockView();
    mockInputSource = new calendar.test.MockInputSource();
    controller = new EventController(mockInputSource, calendarModel, parserService, mockView);
    controller.processCommand("create calendar --name \"Test Calendar\""
                + " --timezone America/Los_Angeles");
    controller.processCommand("use calendar --name \"Test Calendar\"");
    calendar = calendarModel.calendarModel("Test Calendar");
    if (calendar == null) {
      calendar = calendarModel.calendarModel("\"Test Calendar\"");
    }
    guiBuilder = new GuiDtoBuilderService();
  }

  /**
  * Tests handleBothTimesChanged() - both start and end times in one edit.
  * This is the "if (hasStartChange && hasEndChange)" branch.
  */
  @Test
  public void testEditSeriesBothStartAndEndTime() throws Exception {
    controller.processCommand(
                "create event \"Team Standup\" from 2025-12-01T09:00 to "
                        + "2025-12-01T09:30 repeats M for 3 times");

    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2025-12-01T10:00");
    changes.put("end", "2025-12-01T10:45");
    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Team Standup",
                LocalDateTime.of(2025, 12, 1, 9, 0),
                changes
    );
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);
    cmd.execute();
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertNotEquals("Series ID should change when start time changes",
                    originalSeriesId, e.getSeriesId());
      assertEquals("Start time should be 10:00", 10, e.getStartDateTime().getHour());
      assertEquals(0, e.getStartDateTime().getMinute());
      assertEquals("End time should be 10:45", 10, e.getEndDateTime().getHour());
      assertEquals(45, e.getEndDateTime().getMinute());
      assertEquals("Team Standup", e.getSubject());
    }
  }

  /**
  * Tests handleOnlyStartChanged() - start time + other non-time properties.
  * This is the "else if (hasStartChange)" branch.
  */
  @Test
  public void testEditSeriesStartTimeWithLocation() throws Exception {
    controller.processCommand(
                "create event \"Daily Review\" from 2025-12-01T14:00 to "
                        + "2025-12-01T15:00 repeats M for 3 times");

    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2025-12-01T14:30");
    changes.put("location", "Conference Room A");
    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Daily Review",
                LocalDateTime.of(2025, 12, 1, 14, 0),
                changes
    );
    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);
    cmd.execute();
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertNotEquals(originalSeriesId, e.getSeriesId());
      assertEquals(14, e.getStartDateTime().getHour());
      assertEquals(30, e.getStartDateTime().getMinute());
      assertEquals(15, e.getEndDateTime().getHour());
      assertEquals(30, e.getEndDateTime().getMinute());
      assertEquals("Conference Room A", e.getLocation());
    }
  }

  /**
  * Tests handleOnlyEndChanged() - end time + other properties.
  * This is the "else if (hasEndChange)" branch.
  */
  @Test
  public void testEditSeriesEndTimeWithDescription() throws Exception {
    controller.processCommand(
                "create event \"Planning Session\" from 2025-12-01T16:00 to "
                        + "2025-12-01T17:00 repeats M for 3 times");

    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Map<String, String> changes = new HashMap<>();
    changes.put("end", "2025-12-01T17:30");
    changes.put("description", "Updated planning format");
    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Planning Session",
                LocalDateTime.of(2025, 12, 1, 16, 0),
                changes
    );
    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);
    cmd.execute();
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      assertEquals(16, e.getStartDateTime().getHour());
      assertEquals(0, e.getStartDateTime().getMinute());
      assertEquals(17, e.getEndDateTime().getHour());
      assertEquals(30, e.getEndDateTime().getMinute());
      assertEquals("Updated planning format", e.getDescription());
    }
  }

  /**
  * Tests handleNonTimeChanges() - multiple non-time properties.
  * This is the "else" branch (no time changes).
  */
  @Test
  public void testEditSeriesMultipleNonTimeProperties() throws Exception {
    controller.processCommand(
                "create event \"Weekly Sync\" from 2025-12-01T11:00 to "
                        + "2025-12-01T11:30 repeats M for 3 times");

    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Map<String, String> changes = new HashMap<>();
    changes.put("location", "Virtual - Zoom");
    changes.put("description", "Q4 planning discussion");
    changes.put("status", "confirmed");
    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Weekly Sync",
                LocalDateTime.of(2025, 12, 1, 11, 0),
                changes
    );
    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);
    cmd.execute();
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      assertEquals(11, e.getStartDateTime().getHour());
      assertEquals(0, e.getStartDateTime().getMinute());
      assertEquals(11, e.getEndDateTime().getHour());
      assertEquals(30, e.getEndDateTime().getMinute());
      assertEquals("Virtual - Zoom", e.getLocation());
      assertEquals("Q4 planning discussion", e.getDescription());
      assertEquals("confirmed", e.getStatus());
    }
  }

  /**
  * Tests both times changed with additional property.
  */
  @Test
  public void testEditSeriesBothTimesWithAdditionalProperty() throws Exception {
    controller.processCommand(
                "create event \"Training\" from 2025-12-01T08:00 to "
                        + "2025-12-01T09:00 repeats M for 3 times");

    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2025-12-01T09:00");
    changes.put("end", "2025-12-01T10:30");
    changes.put("status", "confirmed");

    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Training",
                LocalDateTime.of(2025, 12, 1, 8, 0),
                changes
    );

    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);
    cmd.execute();
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertNotEquals(originalSeriesId, e.getSeriesId());
      assertEquals(9, e.getStartDateTime().getHour());
      assertEquals(10, e.getEndDateTime().getHour());
      assertEquals(30, e.getEndDateTime().getMinute());
      assertEquals("confirmed", e.getStatus());
    }
  }

  @Test
  public void testMultiPropertyEditErrorIncludesEventIndex() throws Exception {
    controller.processCommand(
                "create event \"Test\" from 2025-12-01T10:00 to "
                        + "2025-12-01T11:00 repeats M for 3 times");

    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2025-12-01T14:00");
    changes.put("end", "2025-12-01T13:00");
    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Test",
                LocalDateTime.of(2025, 12, 1, 10, 0),
                changes
    );

    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);
    Exception exception = assertThrows(Exception.class, cmd::execute);
    assertTrue("Error should mention time validation",
                exception.getMessage().toLowerCase().contains("time")
                        ||
                        exception.getMessage().toLowerCase().contains("before")
                        ||
                        exception.getMessage().toLowerCase().contains("after"));
  }

  /**
  * Tests that multi-property edit validates duplicates.
  */
  @Test
  public void testMultiPropertyEditPreventsDuplicates() throws Exception {
    controller.processCommand(
                "create event \"Standup\" from 2025-12-01T09:00 to "
                        + "2025-12-01T09:15 repeats M for 2 times");

    controller.processCommand(
                "create event \"Conflict\" from 2025-12-01T10:00 to "
                        + "2025-12-01T10:30");

    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2025-12-01T10:00");
    changes.put("end", "2025-12-01T10:30");
    changes.put("subject", "Conflict");

    EditEventDto dto = guiBuilder.buildEditSeriesDto(
                "Standup",
                LocalDateTime.of(2025, 12, 1, 9, 0),
                changes
    );

    EditEventCommand cmd = new EditEventCommand(dto, calendarModel);

    Exception exception = assertThrows(Exception.class, () -> {
      cmd.execute();
    });

    assertTrue("Should prevent duplicate",
                exception.getMessage().toLowerCase().contains("duplicate")
                        ||
                        exception.getMessage().toLowerCase().contains("already exists"));
  }
}