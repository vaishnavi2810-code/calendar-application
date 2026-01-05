package model.edit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.dto.EditEventDto;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.Calendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.service.CommandParserService;
import calendar.util.EditEvent;
import calendar.util.EventFinder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests event editing functionality including single event edits,
 * series edits, forward edits, and validation of time constraints.
 */

public class EditTest {

  private Calendar calendar;
  private Icalendarcollection repository;
  private CalendarModel calendarModel;
  private CommandParserService parserService;
  private EventController controller;
  private Iview mockView;
  private IinputSource mockInputSource;
  /**
   * Sets up test fixtures before each test.
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
      System.out.println("DEBUG: Calendar names in repository: "
          + repository.getAllCalendarNames());
      calendar = calendarModel.calendarModel("\"Test Calendar\"");
    }
  }

  @Test
  public void testDummyCalendarName() {
    calendar.DummyCalendar calendar = new calendar.DummyCalendar();
    assertEquals("DummyCalendar", calendar.getName());
  }

  @Test
  public void testEditForwardUpdatesFutureEventsOnly1() throws Exception {
    String createCommand =
        "create event \"Two week series\" from 2025-12-01T10:00 to "
            + "2025-12-01T11:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String editCommand =
        "edit events subject \"Two week series\" from 2025-12-08T10:00 with "
            +
            "\"Two week series v2\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int updatedCount = 0;
    int unchangedCount = 0;
    for (Event e : calendar.getEvents()) {
      String subject = e.getSubject();
      if ("Two week series v2".equals(subject)) {
        updatedCount++;
      } else if ("Two week series".equals(subject)) {
        unchangedCount++;
      }
    }
    assertEquals(2, updatedCount);
    assertEquals(1, unchangedCount);
  }

  /**
   * TEST: Edit start time to be >= end time should fail.
   */
  @Test
  public void testEditStartTimeGreaterThanEndTimeShouldFail() throws Exception {

    String createCommand = "create event \"Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand); // CORRECT

    String editCommand =
        "edit event start \"Meeting\" from 2025-12-01T10:00 to"
            +
            " 2025-12-01T11:00 with 2025-12-01T11:30";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("must be before end time"));
  }

  @Test
  public void testEditEndTimeLessThanStartTimeShouldFail() throws Exception {

    String createCommand = "create event \"Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand); // CORRECT

    String editCommand =
        "edit event end \"Meeting\" from 2025-12-01T10:00 to "
            +
            "2025-12-01T11:00 with 2025-12-01T09:30";

    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("must be after start time"));
  }

  @Test
  public void testEditCreatingDuplicateShouldFail() throws Exception {

    String create1 = "create event \"Meeting A\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    String create2 = "create event \"Meeting B\" from 2025-12-01T14:00 to 2025-12-01T15:00";

    controller.processCommand(create1); // CORRECT
    controller.processCommand(create2); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(2, calendar.getEventCount());

    String editCommand =
        "edit event subject \"Meeting B\" from 2025-12-01T14:00"
            +
            " to 2025-12-01T15:00 with \"Meeting A\"";
    controller.processCommand(editCommand); // CORRECT

    editCommand =
        "edit event start \"Meeting A\" from 2025-12-01T14:00 to "
            +
            "2025-12-01T15:00 with 2025-12-01T10:00";
    controller.processCommand(editCommand); // CORRECT

    editCommand =
        "edit event end \"Meeting A\" from 2025-12-01T10:00 "
            +
            "to 2025-12-01T15:00 with 2025-12-01T11:00";

    String finalEditCommand = editCommand;
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(finalEditCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("already exists")
        ||
        exception.getMessage().contains("duplicate"));
  }

  @Test
  public void testEditSingleEventSubject() throws Exception {
    String createCommand = "create event \"Old Name\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String editCommand =
        "edit event subject \"Old Name\" from "
            +
            "2025-12-01T10:00 to 2025-12-01T11:00 "
            +
            "with \"New Name\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(1, calendar.getEventCount());
    Event event = calendar.getEvents().iterator().next();
    assertEquals("New Name", event.getSubject());
  }

  @Test
  public void testEditSingleEventLocation() throws Exception {
    String createCommand = "create event \"Meeting\" from 2025-12-01T10:00 "
        +
        "to 2025-12-01T11:00";
    controller.processCommand(createCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String editCommand =
        "edit event location \"Meeting\" from 2025-12-01T10:00 "
            +
            "to 2025-12-01T11:00 with \"Room 301\"";
    controller.processCommand(editCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Room 301", event.getLocation());
    assertEquals("Meeting", event.getSubject());
  }

  @Test
  public void testEditNonExistentEventShouldFail() {
    String editCommand =
        "edit event subject \"Ghost Event\" from 2025-12-01T10:00 to"
            +
            " 2025-12-01T11:00 with \"New Name\"";

    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });

    assertTrue(exception.getMessage().contains("not found"));
  }


  /**
   * TEST: Edit series updates all events in series.
   */
  @Test
  public void testEditSeriesUpdatesAllEvents() throws Exception {
    String createCommand =
        "create event \"Daily Standup\" from 2025-12-01T09:00 to"
            +
            " 2025-12-01T09:15 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());

    String editCommand =
        "edit series location \"Daily Standup\" from 2025-12-01T09:00 with \"Virtual\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());

    for (Event e : calendar.getEvents()) {
      assertEquals("Virtual", e.getLocation());
      assertEquals("Daily Standup", e.getSubject());
    }
  }

  @Test
  public void testEditSeriesOnNonSeriesEventWorksLikeEditSingle() throws Exception {
    String createCommand = "create event \"One-off Meeting\" from 2025-12-01T10:00 "
        +
        "to 2025-12-01T11:00";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String editCommand =
        "edit series subject \"One-off Meeting\" from 2025-12-01T10:00 "
            +
            "with \"Updated Meeting\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(1, calendar.getEventCount());
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Updated Meeting", event.getSubject());
  }

  @Test
  public void testEditSeriesStartTimeBreaksAllFromSeries() throws Exception {
    String createCommand =
        "create event \"Team Sync\" from 2025-12-01T14:00 to"
            +
            " 2025-12-01T14:30 "
            +
            "repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit series start \"Team Sync\" from 2025-12-01T14:00 "
            +
            "with 2025-12-01T14:15";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Set<String> uniqueSeriesIds = new HashSet<>();
    for (Event e : calendar.getEvents()) {
      assertNotEquals(originalSeriesId, e.getSeriesId());
      uniqueSeriesIds.add(e.getSeriesId());
    }
    assertEquals(1, uniqueSeriesIds.size());
  }

  @Test
  public void testEditSeriesDescriptionKeepsSameSeriesId() throws Exception {
    String createCommand =
        "create event \"Project Review\" from 2025-12-01T15:00 to"
            +
            " 2025-12-01T16:00 repeats M for 2 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit series description \"Project Review\" from 2025-12-01T15:00 with \"Q4 Review\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      assertEquals("Q4 Review", e.getDescription());
    }
  }

  @Test
  public void testEditForwardUpdatesFutureEventsOnly() throws Exception {
    String createCommand =
        "create event \"Two week series\" from 2025-12-01T10:00 to "
            + "2025-12-01T11:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String editCommand =
        "edit events subject \"Two week series\" from 2025-12-08T10:00 with"
            +
            " \"Two week series v2\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int updatedCount = 0;
    int unchangedCount = 0;
    for (Event e : calendar.getEvents()) {
      String subject = e.getSubject();
      if ("Two week series v2".equals(subject)) {
        updatedCount++;
      } else if ("Two week series".equals(subject)) {
        unchangedCount++;
      }
    }
    assertEquals(2, updatedCount);
    assertEquals(1, unchangedCount);
  }

  @Test
  public void testEditForwardOnNonSeriesEventWorksLikeEditSingle() throws Exception {
    String createCommand = "create event \"Solo Event\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String editCommand =
        "edit events location \"Solo Event\" from 2025-12-01T10:00 with \"Building A\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(1, calendar.getEventCount());
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Building A", event.getLocation());
  }

  @Test
  public void testEditForwardStartTimeBreaksFutureEventsFromSeries() throws Exception {
    String createCommand =
        "create event \"Recurring Task\" from 2025-12-01T08:00 to"
            +
            " 2025-12-01T09:00 repeats M for 4 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(4, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit events start \"Recurring Task\" from 2025-12-08T08:00 with 2025-12-08T08:30";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int sameSeriesCount = 0;
    int differentSeriesCount = 0;

    for (Event e : calendar.getEvents()) {
      if (e.getStartDateTime().getDayOfMonth() == 1) {
        assertEquals(originalSeriesId, e.getSeriesId());
        sameSeriesCount++;
      } else {
        assertNotEquals(originalSeriesId, e.getSeriesId());
        differentSeriesCount++;
      }
    }

    assertEquals(1, sameSeriesCount);
    assertEquals(3, differentSeriesCount);
  }

  @Test
  public void testEditForwardPreservesSeriesIdForNonStartProperties() throws Exception {
    String createCommand =
        "create event \"Status Meeting\" from 2025-12-01T11:00"
            +
            " to 2025-12-01T11:30 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit events status \"Status Meeting\" from 2025-12-08T11:00 with \"confirmed\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      if (e.getStartDateTime().getDayOfMonth() >= 8) {
        assertEquals(originalSeriesId, e.getSeriesId());
        assertEquals("confirmed", e.getStatus());
      }
    }
  }


  @Test
  public void testMultipleEditsOnSameEvent() throws Exception {
    String createCommand = "create event \"Workshop\" from 2025-12-01T13:00 to 2025-12-01T15:00";
    controller.processCommand(createCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String edit1 =
        "edit event subject \"Workshop\" from 2025-12-01T13:00 "
            +
            "to 2025-12-01T15:00 with \"Advanced Workshop\"";
    controller.processCommand(edit1); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String edit2 =
        "edit event location \"Advanced Workshop\" from "
            +
            "2025-12-01T13:00 to 2025-12-01T15:00 with "
            +
            "\"Conference Room\"";
    controller.processCommand(edit2); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String edit3 =
        "edit event description \"Advanced Workshop\" "
            +
            "from 2025-12-01T13:00 to 2025-12-01T15:00 "
            +
            "with \"Java Deep Dive\"";
    controller.processCommand(edit3); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Advanced Workshop", event.getSubject());
    assertEquals("Conference Room", event.getLocation());
    assertEquals("Java Deep Dive", event.getDescription());
  }

  @Test
  public void testEditSeriesThenEditSingleInstance() throws Exception {
    String createCommand =
        "create event \"Training\" from 2025-12-01T14:00 to 2025-12-01T16:00"
            +
            " repeats M for 3 times";
    controller.processCommand(createCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String editSeries =
        "edit series location \"Training\" from 2025-12-01T14:00 with \"Online\"";
    controller.processCommand(editSeries); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertEquals("Online", e.getLocation());
    }

    String editSingle =
        "edit event location \"Training\" from 2025-12-08T14:00 to "
            +
            "2025-12-08T16:00 with \"In-Person\"";
    controller.processCommand(editSingle); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int onlineCount = 0;
    int inPersonCount = 0;
    for (Event e : calendar.getEvents()) {
      if ("In-Person".equals(e.getLocation())) {
        inPersonCount++;
      } else if ("Online".equals(e.getLocation())) {
        onlineCount++;
      }
    }

    assertEquals(1, inPersonCount);
    assertEquals(2, onlineCount);
  }

  @Test
  public void testValidTimeRangeEdit() throws Exception {
    String createCommand = "create event \"Seminar\" from 2025-12-01T09:00 to 2025-12-01T12:00";
    controller.processCommand(createCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String editCommand =
        "edit event end \"Seminar\" from 2025-12-01T09:00 to"
            +
            " 2025-12-01T12:00 with 2025-12-01T13:00";
    controller.processCommand(editCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Event event = calendar.getEvents().iterator().next();
    assertEquals(9, event.getStartDateTime().getHour());
    assertEquals(13, event.getEndDateTime().getHour());
  }

  @Test
  public void testEditAllPropertiesOfEvent() throws Exception {
    String createCommand = "create event \"Initial\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    controller.processCommand(
        "edit event subject \"Initial\" from 2025-12-01T10:00 to "
            +
            "2025-12-01T11:00 with \"Updated\""
    );
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    controller.processCommand(
        "edit event location \"Updated\" from 2025-12-01T10:00 to "
            +
            "2025-12-01T11:00 with \"Room 5\""
    );
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    controller.processCommand(
        "edit event description \"Updated\" from "
            +
            "2025-12-01T10:00 to 2025-12-01T11:00 with "
            +
            "\"Important\""
    );
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    controller.processCommand(
        "edit event status \"Updated\" from 2025-12-01T10:00 to "
            +
            "2025-12-01T11:00 with \"confirmed\""
    );
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Updated", event.getSubject());
    assertEquals("Room 5", event.getLocation());
    assertEquals("Important", event.getDescription());
    assertEquals("confirmed", event.getStatus());
  }

  @Test(expected = java.lang.Exception.class)
  public void testEditSingleStartPropertyBreaksFromSeries() throws Exception {
    String createCommand =
        "create event \"Team Meeting\" from 2025-12-01T10:00 "
            + "to 2025-12-01T11:00 repeats M for 3 times";
    controller.processCommand(createCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    assertNotNull(originalSeriesId);
    assertFalse(originalSeriesId.isEmpty());

    String editStartCommand =
        "edit event start \"Team Meeting\" from 2025-12-08T10:00 "
            + "to 2025-12-08T11:00 with 2025-12-08T12:00";
    controller.processCommand(editStartCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int sameSeriesCount = 0;
    int differentSeriesCount = 0;
    String newSeriesId = null;
    for (Event e : calendar.getEvents()) {
      if (e.getSeriesId().equals(originalSeriesId)) {
        sameSeriesCount++;
      } else {
        differentSeriesCount++;
        newSeriesId = e.getSeriesId();
      }
    }
    assertEquals(2, sameSeriesCount);
    assertEquals(1, differentSeriesCount);
    assertNotEquals(originalSeriesId, "New seriesId should differ from original", newSeriesId);

    String editLocationCommand =
        "edit event location \"Team Meeting\" from 2025-12-08T09:30 "
            + "to 2025-12-08T11:00 with \"Conference Room\"";
    controller.processCommand(editLocationCommand); // CORRECT
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      if (e.getLocation().equals("Conference Room")) {
        assertEquals(newSeriesId, e.getSeriesId());
      }
    }
  }

  @Test(expected = java.lang.Exception.class)
  public void testEditStartTimeCrossesDayBoundaryInSeries() throws Exception {
    String createCommand =
        "create event \"Team Meeting\" from 2025-12-01T10:00 "
            + "to 2025-12-01T11:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    assertNotNull(originalSeriesId);
    assertFalse(originalSeriesId.isEmpty());

    String editStartCommand =
        "edit event start \"Team Meeting\" from 2025-12-08T10:00 "
            + "to 2025-12-08T11:00 with 2025-12-07T10:00";
    controller.processCommand(editStartCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
  }

  @Test
  public void testToStringReturnsExpectedFormat() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, ZoneId.of("UTC"));

    Event event = new EventBuilder()
        .setSubject("Team Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("ABC123")
        .setDescription("Weekly sync")
        .setLocation("Conference Room")
        .setStatus("Confirmed")
        .build();

    String toString = event.toString();
    assertTrue(toString.contains("Event{"));
    assertTrue(toString.contains("subject='Team Meeting'"));
    assertTrue(toString.contains("startDateTime=" + start.toString()));
    assertTrue(toString.contains("endDateTime=" + end.toString()));
    assertTrue(toString.contains("seriesId='ABC123'"));
    assertTrue(toString.contains("description='Weekly sync'"));
    assertTrue(toString.contains("location='Conference Room'"));
    assertTrue(toString.contains("status='Confirmed'"));
    assertTrue(toString.endsWith("}"));
  }

  @Test
  public void testEventsEqualsMethod() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, ZoneId.of("UTC"));

    Event event1 = new EventBuilder()
        .setSubject("Team Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("ABC123")
        .setDescription("Weekly sync")
        .setLocation("Conference Room")
        .setStatus("Confirmed")
        .build();

    Event event2 = new EventBuilder()
        .setSubject("Team Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("ABC123")
        .setDescription("Weekly sync")
        .setLocation("Conference Room")
        .setStatus("Confirmed")
        .build();

    assertEquals(event1, event1);
    assertEquals(event1, event2);
  }

  @Test
  public void testEditSeriesEventNotFound() {
    String editCommand =
        "edit series location \"Ghost Event\" from 2025-12-01T10:00 with \"Room 5\"";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("Event not found"));
    System.out.println("✓ TEST PASSED: EditSeries throws exception when event not found");
  }

  @Test
  public void testEditSeriesOnNonSeriesEvent() throws Exception {
    String createCommand =
        "create event \"One-off Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(1, calendar.getEventCount());
    String editCommand =
        "edit series location \"One-off Meeting\" from 2025-12-01T10:00 with \"Room 301\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(1, calendar.getEventCount());
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Room 301", event.getLocation());
    System.out.println("✓ TEST PASSED: EditSeries handles non-series event correctly");
  }

  @Test
  public void testEditSeriesNonTimePropertyKeepsSameSeriesId() throws Exception {
    String createCommand =
        "create event \"Weekly Sync\" from 2025-12-01T14:00 to"
            + " 2025-12-01T15:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    String editCommand =
        "edit series location \"Weekly Sync\" from 2025-12-01T14:00 with \"Conference Room A\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      assertEquals("Conference Room A", e.getLocation());
    }
    System.out.println("✓ TEST PASSED: EditSeries non-time property keeps same seriesId");
  }

  @Test
  public void testEditSeriesEndPropertyKeepsSeriesIdAndAppliesOffset() throws Exception {
    String createCommand =
        "create event \"Team Review\" from 2025-12-01T15:00 "
            + "to 2025-12-01T16:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    String editCommand =
        "edit series end \"Team Review\" from 2025-12-01T15:00 with 2025-12-01T16:30";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      assertEquals(15, e.getStartDateTime().getHour());
      assertEquals(0, e.getStartDateTime().getMinute());
      assertEquals(16, e.getEndDateTime().getHour());
      assertEquals(30, e.getEndDateTime().getMinute());
    }
    System.out.println("✓ TEST PASSED: EditSeries end property keeps seriesId and applies offset");
  }

  @Test
  public void testEditSeriesMultiDaySpanThrowsException() throws Exception {
    String createCommand =
        "create event \"Training\" from 2025-12-01T10:00 to "
            +
            "2025-12-01T11:00 repeats M for 2 times";
    controller.processCommand(createCommand); // CORRECT
    String editCommand =
        "edit series end \"Training\" from 2025-12-01T10:00 "
            +
            "with 2025-12-02T09:00";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("must start and end on the same day"));
    assertTrue(exception.getMessage().contains("Event would span from"));
    System.out.println("✓ TEST PASSED: EditSeries prevents multi-day event span");
  }

  @Test
  public void testEditSeriesRollbackOnDuplicateDetection() throws Exception {
    String createCommand =
        "create event \"Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00 "
            +
            "repeats M for 2 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String conflictCommand =
        "create event \"Meeting\" from 2025-12-01T14:00 to 2025-12-01T15:00";
    controller.processCommand(conflictCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    int originalSize = calendar.getEventCount();
    String editCommand =
        "edit series start \"Meeting\" from 2025-12-01T10:00 with 2025-12-01T14:00";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand);
    });
    assertTrue(exception.getMessage().contains("duplicate")
        || exception.getMessage().contains("already exists"));
    assertEquals(originalSize, calendar.getEventCount());
    System.out.println("✓ TEST PASSED: EditSeries rolls back on duplicate detection");
  }

  @Test
  public void testEditSeriesSuccessfulTimeEdit() throws Exception {
    String createCommand =
        "create event \"Checkpoint\" from 2025-12-01T11:00 "
            + "to 2025-12-01T11:30 repeats M for 3 times";
    controller.processCommand(createCommand); // CORRECT
    String editCommand =
        "edit series start \"Checkpoint\" from 2025-12-01T11:00"
            +
            " with 2025-12-01T11:15";
    controller.processCommand(editCommand); // CORRECT
    for (Event e : calendar.getEvents()) {
      assertEquals(11, e.getStartDateTime().getHour());
      assertEquals(15, e.getStartDateTime().getMinute());
    }
  }

  @Test
  public void testEditSeriesSuccessfulNonTimeEdit() throws Exception {
    String createCommand =
        "create event \"Status Update\" from 2025-12-01T16:00 "
            + "to 2025-12-01T16:30 repeats M for 2 times";
    controller.processCommand(createCommand); // CORRECT

    String editCommand =
        "edit series description \"Status Update\" from "
            +
            "2025-12-01T16:00 with \"Progress Report\"";
    controller.processCommand(editCommand); // CORRECT
    for (Event e : calendar.getEvents()) {
      assertEquals("Progress Report", e.getDescription());
    }
    System.out.println("✓ TEST PASSED: EditSeries successfully "
        +
        "edits non-time properties");
  }

  @Test
  public void testEditSeriesEndTimeBeforeStartTimeThrowsException() throws Exception {
    String createCommand =
        "create event \"Workshop\" from 2025-12-01T10:00 "
            + "to 2025-12-01T11:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    String editCommand =
        "edit series end \"Workshop\" from "
            +
            "2025-12-01T10:00 with 2025-12-01T09:30";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand);
    });
    assertTrue(exception.getMessage().contains("New start time must be before end time"));
    assertEquals(3, calendar.getEventCount());
    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      assertEquals(10, e.getStartDateTime().getHour());
      assertEquals(11, e.getEndDateTime().getHour());
    }
  }


  @Test
  public void testEditForwardEventNotFound() {
    String editCommand =
        "edit events location \"Nonexistent\" from 2025-12-01T10:00 with \"Room 5\"";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand);
    });

    assertTrue(exception.getMessage().contains("Event not found"));
    System.out.println("✓ TEST PASSED: EditForward throws exception when event not found");
  }

  @Test
  public void testEditForwardNonSeriesEvent() throws Exception {
    String createCommand =
        "create event \"Solo Meeting\" from 2025-12-01T10:00 to 2025-12-01T11:00";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(1, calendar.getEventCount());
    String editCommand =
        "edit events location \"Solo Meeting\" from 2025-12-01T10:00 with \"Building A\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    Event event = calendar.getEvents().iterator().next();
    assertEquals("Building A", event.getLocation());
  }

  @Test
  public void testEditForwardStartPropertyBreaksSeriesAndAppliesOffset() throws Exception {
    String createCommand =
        "create event \"Daily Check\" from 2025-12-01T09:00 "
            + "to 2025-12-01T09:30 repeats M for 4 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(4, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit events start \"Daily Check\" from 2025-12-08T09:00 with 2025-12-08T09:15";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int unchangedCount = 0;
    int changedCount = 0;

    for (Event e : calendar.getEvents()) {
      if (e.getStartDateTime().getDayOfMonth() == 1) {
        assertEquals(originalSeriesId, e.getSeriesId());
        assertEquals(9, e.getStartDateTime().getHour());
        assertEquals(0, e.getStartDateTime().getMinute());
        unchangedCount++;
      } else {
        assertNotEquals(originalSeriesId, e.getSeriesId());
        assertEquals(9, e.getStartDateTime().getHour());
        assertEquals(15, e.getStartDateTime().getMinute());
        assertEquals(45, e.getEndDateTime().getMinute());
        changedCount++;
      }
    }
    assertEquals(1, unchangedCount);
    assertEquals(3, changedCount);
  }

  @Test
  public void testEditForwardEndPropertyKeepsSeriesIdAndAppliesOffset() throws Exception {
    String createCommand =
        "create event \"Team Sync\" from 2025-12-01T14:00 "
            + "to 2025-12-01T15:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    assertEquals(3, calendar.getEventCount());
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();
    String editCommand =
        "edit events end \"Team Sync\" from 2025-12-08T14:00 with 2025-12-08T15:30";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int unchangedCount = 0;
    int changedCount = 0;
    for (Event e : calendar.getEvents()) {
      if (e.getStartDateTime().getDayOfMonth() == 1) {
        assertEquals(15, e.getEndDateTime().getHour());
        assertEquals(0, e.getEndDateTime().getMinute());
        unchangedCount++;
      } else {
        assertEquals(originalSeriesId, e.getSeriesId());
        assertEquals(14, e.getStartDateTime().getHour());
        assertEquals(15, e.getEndDateTime().getHour());
        assertEquals(30, e.getEndDateTime().getMinute());
        changedCount++;
      }
    }
    assertEquals(1, unchangedCount);
    assertEquals(2, changedCount);
    System.out.println("✓ TEST PASSED: EditForward end property keeps seriesId and applies offset");
  }

  @Test
  public void testEditForwardInvalidTimeAdjustmentThrowsException() throws Exception {
    String createCommand =
        "create event \"Workshop\" from 2025-12-01T10:00 "
            +
            "to 2025-12-01T11:00 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit events end \"Workshop\" from 2025-12-08T10:00 with "
            +
            "2025-12-08T09:30";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("must be before end time"));

    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
    }
    System.out.println("✓ TEST PASSED: EditForward validates adjusted "
        +
        "start < adjusted end");
  }


  @Test
  public void testEditForwardMultiDaySpanThrowsException() throws Exception {
    String createCommand =
        "create event \"Session\" from 2025-12-01T10:00 to "
            +
            "2025-12-01T16:00 repeats M for 2 times";
    controller.processCommand(createCommand); // CORRECT

    String editCommand =
        "edit events end \"Session\" from 2025-12-08T10:00 "
            +
            "with 2025-12-09T02:00";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(editCommand); // CORRECT
    });
    assertTrue(exception.getMessage().contains("must start and end on the same day"));
    assertTrue(exception.getMessage().contains("Event would span from"));
    System.out.println("✓ TEST PASSED: EditForward prevents multi-day event span");
  }


  @Test
  public void testEditForwardNonTimePropertyKeepsSeriesId() throws Exception {
    String createCommand =
        "create event \"Standup\" from 2025-12-01T09:00 to "
            +
            "2025-12-01T09:15 repeats M for 3 times";
    controller.processCommand(createCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    String originalSeriesId = calendar.getEvents().iterator().next().getSeriesId();

    String editCommand =
        "edit events description \"Standup\" "
            +
            "from 2025-12-08T09:00 with \"Daily Update\"";
    controller.processCommand(editCommand);
    calendar = calendarModel.calendarModel("\"Test Calendar\"");
    int unchangedCount = 0;
    int changedCount = 0;

    for (Event e : calendar.getEvents()) {
      assertEquals(originalSeriesId, e.getSeriesId());
      if (e.getStartDateTime().getDayOfMonth() == 1) {
        assertEquals("", e.getDescription());
        unchangedCount++;
      } else {
        assertEquals("Daily Update", e.getDescription());
        changedCount++;
      }
    }
    assertEquals(1, unchangedCount);
    assertEquals(2, changedCount);
    System.out.println("✓ TEST PASSED: EditForward non-time property keeps same seriesId");
  }

  @Test
  public void testEditEndTimeMultiDaySpanWithSeriesIdThrowsException() throws Exception {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 16, 0, 0, 0, calendar.getTimezone());

    Event original = new EventBuilder()
        .setSubject("Workshop")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("abc123")
        .setDescription("")
        .setLocation("")
        .setStatus("")
        .build();
    Pattern pattern = Pattern.compile("(?<property>end).*with (?<newvalue>2025-12-02T10:00)");
    Matcher matcher = pattern.matcher("end with 2025-12-02T10:00");
    matcher.matches();

    EditEventDto dto = new EditEventDto(EditEventDto.EditType.EDIT_SINGLE, matcher);
    Exception exception = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEvent(original, dto, "abc123", calendar.getTimezone());
    });

    assertTrue(exception.getMessage().contains("must start and end on the same day"));
    assertTrue(exception.getMessage().contains("Event would span from"));
    assertTrue(exception.getMessage().contains("2025-12-01"));
    assertTrue(exception.getMessage().contains("2025-12-02"));
    System.out.println("✓ TEST PASSED: EditEvent validates end time multi-day span with seriesId");
  }

  @Test
  public void testCreateModifiedEventUnknownPropertyThrowsException() throws Exception {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());
    Event original = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("")
        .setDescription("")
        .setLocation("")
        .setStatus("")
        .build();
    Pattern pattern = Pattern.compile("(?<property>title).*with (?<newvalue>.+)");
    Matcher matcher = pattern.matcher("title with New Title");
    matcher.matches();
    EditEventDto dto = new EditEventDto(EditEventDto.EditType.EDIT_SINGLE, matcher);
    Exception exception = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEvent(original, dto, "", calendar.getTimezone());
    });

    assertTrue(exception.getMessage().contains("Unknown property"));
    System.out.println("✓ TEST PASSED: EditEvent throws exception for unknown property");
  }

  @Test
  public void testValidateNoDuplicateThrowsForDuplicateInNewEvents() throws Exception {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event1 = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("")
        .setDescription("")
        .setLocation("")
        .setStatus("")
        .build();

    Event event2 = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("")
        .setDescription("")
        .setLocation("")
        .setStatus("")
        .build();

    Set<Event> existingEvents = new HashSet<>();
    Set<Event> newEvents = new HashSet<>();
    Set<Event> eventsToRemove = new HashSet<>();
    newEvents.add(event1);
    Exception exception = assertThrows(Exception.class, () -> {
      EditEvent.validateNoDuplicate(event2, existingEvents, newEvents, eventsToRemove);
    });

    assertTrue(exception.getMessage().contains("Would create duplicate events"));
    System.out.println("✓ TEST PASSED: validateNoDuplicate detects duplicates in newEvents set");
  }

  @Test
  public void testValidateNoDuplicatePassesWhenNoDuplicate() throws Exception {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("")
        .setDescription("")
        .setLocation("")
        .setStatus("")
        .build();
    // This test doesn't need an assertion, it just needs to not throw.
    EditEvent.validateNoDuplicate(event, new HashSet<>(), new HashSet<>(), new HashSet<>());
    System.out.println("✓ TEST PASSED: validateNoDuplicate passes when no duplicates exist");
  }

  @Test
  public void testParseDateTimeParsesCorrectly() throws Exception {
    String dateTimeStr = "2025-12-01T10:00";
    ZonedDateTime parsed = EditEvent.parseDateTime(dateTimeStr, calendar.getTimezone());

    assertNotNull(parsed);
    assertEquals(2025, parsed.getYear());
    assertEquals(12, parsed.getMonthValue());
    assertEquals(1, parsed.getDayOfMonth());
    assertEquals(10, parsed.getHour());
    assertEquals(0, parsed.getMinute());
    System.out.println("✓ TEST PASSED: parseDateTime parses date-time string correctly");
  }

  @Test
  public void testFindEventBySubjectStartEndFindsEvent() throws Exception {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("")
        .setDescription("")
        .setLocation("")
        .setStatus("")
        .build();

    Set<Event> testEvents = new HashSet<>();
    testEvents.add(event);

    Pattern pattern = Pattern.compile("(?<subject>Meeting).*from"
        + " (?<start>2025-12-01T10:00) to (?<end>2025-12-01T11:00)");
    Matcher matcher = pattern.matcher("Meeting from 2025-12-01T10:00 to 2025-12-01T11:00");
    assertTrue("Regex matcher failed", matcher.matches());

    EditEventDto dto = new EditEventDto(EditEventDto.EditType.EDIT_SINGLE, matcher);
    Event found = EditEvent.findEventBySubjectStartEnd(dto, testEvents,
        calendar.getTimezone());
    assertNotNull("Event was not found in the test set", found);
    assertEquals("Found event had the wrong subject", "Meeting", found.getSubject());
    assertEquals("Found event was not the one we added", event, found);
    System.out.println("✓ TEST PASSED: findEventBySubjectStartEnd finds event correctly");
  }

  @Test
  public void testFindEventBySubjectStartEndThrowsWhenNotFound() {
    Pattern pattern = Pattern.compile("(?<subject>Ghost).*from (?<start>2025-12-01T10:00) "
        + "to (?<end>2025-12-01T11:00)");
    Matcher matcher = pattern.matcher("Ghost from 2025-12-01T10:00 to 2025-12-01T11:00");
    matcher.matches();

    EditEventDto dto = new EditEventDto(EditEventDto.EditType.EDIT_SINGLE, matcher);

    Exception exception = assertThrows(Exception.class, () -> {
      EditEvent.findEventBySubjectStartEnd(dto, calendar.getEvents(), calendar.getTimezone());
    });

    assertTrue(exception.getMessage().contains("Event not found"));
    System.out.println("✓ TEST PASSED: findEventBySubjectStartEnd throws when event not found");
  }

  @Test
  public void testFindBySeriesReturnsEmptyForNullSeriesId() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("abc123")
        .build();
    calendar.getEvents().add(event);

    List<Event> resultNull = EventFinder.findBySeries(null, calendar.getEvents());
    assertTrue(resultNull.isEmpty());
    System.out.println("✓ TEST PASSED: findBySeries returns empty list for null seriesId");
  }

  @Test
  public void testFindBySeriesReturnsEmptyForEmptySeriesId() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("abc123")
        .build();
    calendar.getEvents().add(event);

    List<Event> resultEmpty = EventFinder.findBySeries("", calendar.getEvents());
    assertTrue(resultEmpty.isEmpty());
    System.out.println("✓ TEST PASSED: findBySeries returns empty list for empty seriesId");
  }

  @Test
  public void testFindSeriesFromReturnsEmptyForNullSeriesId() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("xyz789")
        .build();
    calendar.getEvents().add(event);

    List<Event> resultNull = EventFinder.findSeriesFrom(null, start, calendar.getEvents());
    assertTrue(resultNull.isEmpty());
    System.out.println("✓ TEST PASSED: findSeriesFrom returns empty list for null seriesId");
  }

  @Test
  public void testFindSeriesFromReturnsEmptyForEmptySeriesId() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .setSeriesId("xyz789")
        .build();
    calendar.getEvents().add(event);

    List<Event> resultEmpty = EventFinder.findSeriesFrom("", start, calendar.getEvents());
    assertTrue(resultEmpty.isEmpty());
    System.out.println("✓ TEST PASSED: findSeriesFrom returns empty list for empty seriesId");
  }

  @Test
  public void testFindBySubjectFromStartFindsMatchingEvents() {

    ZonedDateTime start1 = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime start2 = ZonedDateTime.of(2025, 12, 8, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime start3 = ZonedDateTime.of(2025, 12, 15, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    ZonedDateTime end2 = start2.plusHours(1);
    ZonedDateTime end3 = start3.plusHours(1);
    Event event1 = new EventBuilder()
        .setSubject("Standup")
        .setStartDateTime(start1)
        .setEndDateTime(end1)
        .build();

    Event event2 = new EventBuilder()
        .setSubject("Standup")
        .setStartDateTime(start2)
        .setEndDateTime(end2)
        .build();

    Event event3 = new EventBuilder()
        .setSubject("Standup")
        .setStartDateTime(start3)
        .setEndDateTime(end3)
        .build();

    Set<Event> testEvents = new HashSet<>();
    testEvents.add(event1);
    testEvents.add(event2);
    testEvents.add(event3);
    List<Event> found = EventFinder.findBySubjectFromStart("Standup", start2, testEvents);
    assertEquals(2, found.size());
    assertTrue("Should contain event on the start date", found.contains(event2));
    assertTrue("Should contain event after the start date", found.contains(event3));
    assertFalse("Should not contain event before the start date", found.contains(event1));
    System.out.println("✓ TEST PASSED: findBySubjectFromStart finds events from specified time");
  }

  @Test
  public void testFindBySubjectFromStartReturnsEmptyWhenNoMatches() {
    ZonedDateTime start = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 12, 1, 11, 0, 0, 0, calendar.getTimezone());

    Event event = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(start)
        .setEndDateTime(end)
        .build();
    calendar.getEvents().add(event);
    ZonedDateTime searchFrom = ZonedDateTime.of(2025, 12, 1, 9, 0, 0, 0, calendar.getTimezone());
    List<Event> found = EventFinder.findBySubjectFromStart("Nonexistent", searchFrom,
        calendar.getEvents());

    assertTrue(found.isEmpty());
    System.out.println("✓ TEST PASSED: findBySubjectFromStart returns empty when no matches");
  }

  @Test
  public void testFindBySubjectFromStartFiltersCorrectly() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 12, 1, 9, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end1 = ZonedDateTime.of(2025, 12, 1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime start2 = ZonedDateTime.of(2025, 12, 1, 14, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);
    Event event1 = new EventBuilder()
        .setSubject("Task")
        .setStartDateTime(start1)
        .setEndDateTime(end1)
        .build();

    Event event2 = new EventBuilder()
        .setSubject("Task")
        .setStartDateTime(start2)
        .setEndDateTime(end2)
        .build();

    Set<Event> testEvents = new HashSet<>();
    testEvents.add(event1);
    testEvents.add(event2);
    ZonedDateTime searchFrom = ZonedDateTime.of(2025, 12, 1, 10,
        0, 0, 0, calendar.getTimezone());

    List<Event> found = EventFinder.findBySubjectFromStart("Task", searchFrom, testEvents);
    assertEquals("Should only find one event", 1, found.size());
    assertEquals("The found event should be the one at 14:00",
        14, found.get(0).getStartDateTime().getHour());
    assertTrue("The found list should contain event2", found.contains(event2));
    assertFalse("The found list should not contain event1", found.contains(event1));
    System.out.println("✓ TEST PASSED: findBySubjectFromStart correctly filters by start time");
  }

  @Test
  public void testGetGroupReturnsNullWhenMatcherGroupIsNull() {
    String patternStr = "(?<property>\\w+) (?<subject>\\S+) from (?<start>\\S+)(?<end> to \\S+)?";
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher("location Meeting from 2025-12-01T10:00");
    assertTrue(matcher.matches());
    EditEventDto dto = new EditEventDto(EditEventDto.EditType.EDIT_FORWARD, matcher);
    assertNull(dto.getTargetEndDateTime());
  }
}