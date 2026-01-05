package model.copy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.Calendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.service.CommandParserService;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests copy event functionality including single event copy,
 * copy events on date, copy events between dates, weekday consistency,
 * timezone conversion, and series relationship preservation.
 */
public class CopyEventTest {

  private Calendar sourceCalendar;
  private Calendar targetCalendar;
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
    controller.processCommand("create calendar --name Work --timezone America/New_York");
    controller.processCommand("use calendar --name Work");
    sourceCalendar = calendarModel.calendarModel("Work");
    controller.processCommand("create calendar --name Personal --timezone America/Los_Angeles");
    targetCalendar = calendarModel.calendarModel("Personal");
  }

  @Test
  public void testCopySingleEventBasic() throws Exception {
    controller.processCommand("create event \"Monday Meeting\" from 2024-09-09T10:00 "
        + "to 2024-09-09T11:00");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(1, sourceCalendar.getEventCount());
    controller.processCommand("copy event \"Monday Meeting\" on 2024-09-09T10:00 "
        + "--target Personal to 2025-01-07T10:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(1, targetCalendar.getEventCount());
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertEquals("Monday Meeting", copiedEvent.getSubject());
    assertEquals(7, copiedEvent.getStartDateTime().getDayOfMonth());
    System.out.println("✓ TEST PASSED: Copy single event maintains weekday consistency");
  }

  @Test
  public void testCopySingleEventTimezoneConversion() throws Exception {
    controller.processCommand("create event \"Afternoon Call\" "
        + "from 2024-09-09T14:00 to 2024-09-09T15:00");
    controller.processCommand("copy event \"Afternoon Call\" "
        + "on 2024-09-09T14:00 --target Personal to 2025-01-13T10:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertEquals(10, copiedEvent.getStartDateTime().getHour());
    assertEquals(0, copiedEvent.getStartDateTime().getMinute());
    System.out.println("✓ TEST PASSED: Copy single event "
        + "uses user-specified time in target timezone");
  }

  @Test
  public void testCopySingleEventGeneratesNewSeriesId() throws Exception {
    controller.processCommand("create event \"Weekly Standup\" from 2024-09-09T09:00 "
        + "to 2024-09-09T09:30 repeats M for 3 times");
    sourceCalendar = calendarModel.calendarModel("Work");
    controller.processCommand("copy event \"Weekly Standup\" on 2024-09-09T09:00 "
        + "--target Personal to 2025-01-13T09:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertNotNull(copiedEvent.getSeriesId());
    String originalSeriesId = sourceCalendar.getEvents().iterator().next().getSeriesId();
    assertNotEquals(originalSeriesId, copiedEvent.getSeriesId());
    System.out.println("✓ TEST PASSED: Copy single event generates new series ID");
  }

  @Test
  public void testCopySingleEventNotFound() {
    String copyCommand = "copy event \"Ghost Event\" on "
        + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(copyCommand);
    });
    assertTrue(exception.getMessage().contains("not found"));
  }

  @Test
  public void testCopySingleEventDuplicateFails() throws Exception {
    controller.processCommand("create event \"Meeting\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("copy event \"Meeting\" on 2024-09-09T10:00 "
        + "--target Personal to 2025-01-13T10:00");
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("copy event \"Meeting\" on 2024-09-09T10:00 "
          + "--target Personal to 2025-01-13T10:00");
    });
    assertTrue(exception.getMessage().contains("already exists")
        || exception.getMessage().contains("duplicate"));
    System.out.println("✓ TEST PASSED: Copy single event detects duplicates");
  }

  @Test
  public void testCopyEventsOnDateBasic() throws Exception {
    controller.processCommand("create event \"Morning Meeting\" "
        + "from 2024-09-05T09:00 to 2024-09-05T10:00");
    controller.processCommand("create event \"Lunch\" "
        + "from 2024-09-05T12:00 to 2024-09-05T13:00");
    controller.processCommand("create event \"Client Call\" "
        + "from 2024-09-05T14:00 to 2024-09-05T15:00");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(3, sourceCalendar.getEventCount());
    controller.processCommand("copy events on 2024-09-05 --target Personal to 2025-01-08");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(3, targetCalendar.getEventCount());
    for (Event e : targetCalendar.getEvents()) {
      assertEquals(8, e.getStartDateTime().getDayOfMonth());
    }
    System.out.println("✓ TEST PASSED: Copy events on date maintains weekday consistency");
  }

  @Test
  public void testCopyEventsOnDateTimezoneConversion() throws Exception {
    controller.processCommand("create event \"Afternoon Meeting\" "
        + "from 2024-09-05T14:00 to 2024-09-05T15:00");
    controller.processCommand("copy events on 2024-09-05 --target Personal to 2025-01-08");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertEquals(11, copiedEvent.getStartDateTime().getHour());
    System.out.println("✓ TEST PASSED: Copy events on date converts timezone correctly");
  }

  @Test
  public void testCopyEventsOnDateNoEventsFound() {
    String copyCommand = "copy events on 2024-10-10 --target Personal to 2025-01-08";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(copyCommand);
    });

    assertTrue(exception.getMessage().contains("No events found"));
    System.out.println("✓ TEST PASSED: Copy events on date throws exception when no events found");
  }

  @Test
  public void testCopyEventsOnDatePreservesSeriesRelationship() throws Exception {
    controller.processCommand("create event \"Daily Standup\" from "
        + "2024-09-05T08:30 to 2024-09-05T09:00 repeats M for 5 times");
    sourceCalendar = calendarModel.calendarModel("Work");
    String originalSeriesId = sourceCalendar.getEvents().iterator().next().getSeriesId();
    controller.processCommand("copy events on 2024-09-09 --target Personal to 2025-01-08");
    targetCalendar = calendarModel.calendarModel("Personal");
    for (Event e : targetCalendar.getEvents()) {
      if (e.getSubject().equals("Daily Standup")) {
        assertNotNull(e.getSeriesId());
        assertNotEquals(originalSeriesId, e.getSeriesId());
      }
    }
  }

  @Test
  public void testCopyEventsBetweenDatesBasic() throws Exception {
    controller.processCommand("create event \"Monday Event\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("create event \"Thursday Event\" "
        + "from 2024-09-12T14:00 to 2024-09-12T15:00");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(2, sourceCalendar.getEventCount());
    controller.processCommand("copy events between 2024-09-09 "
        + "and 2024-09-12 --target Personal to 2025-01-07");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(2, targetCalendar.getEventCount());
    Event mondayEvent = targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Monday Event"))
        .findFirst()
        .orElseThrow();
    assertEquals(DayOfWeek.MONDAY, mondayEvent.getStartDateTime().getDayOfWeek());
    assertEquals(13, mondayEvent.getStartDateTime().getDayOfMonth());
    Event thursdayEvent = targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Thursday Event"))
        .findFirst()
        .orElseThrow();
    assertEquals(DayOfWeek.THURSDAY, thursdayEvent.getStartDateTime().getDayOfWeek());
    assertEquals(16, thursdayEvent.getStartDateTime().getDayOfMonth());
    System.out.println("✓ TEST PASSED: Copy events between maintains weekday and relative spacing");
  }

  @Test
  public void testCopyEventsBetweenPartialSeriesCopy() throws Exception {
    controller.processCommand("create event \"Weekly Review\" "
        + "from 2024-09-09T16:00 to 2024-09-09T17:00 repeats M for 10 times");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(10, sourceCalendar.getEventCount());
    controller.processCommand("copy events between 2024-09-23 "
        + "and 2024-10-21 --target Personal to 2025-03-01");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(5, targetCalendar.getEventCount());
    Set<String> newSeriesIds = new HashSet<>();
    for (Event e : targetCalendar.getEvents()) {
      assertNotNull(e.getSeriesId());
      String originalSeriesId = sourceCalendar.getEvents().iterator().next().getSeriesId();
      assertNotEquals(originalSeriesId, e.getSeriesId());
      newSeriesIds.add(e.getSeriesId());
      assertEquals(DayOfWeek.MONDAY, e.getStartDateTime().getDayOfWeek());
    }
    assertEquals(1, newSeriesIds.size());

    System.out.println("✓ TEST PASSED: Copy events between maintains partial series relationship");
  }

  @Test
  public void testCopyEventsBetweenNoEventsFound() {
    String copyCommand = "copy events between "
        + "2024-08-01 and 2024-08-31 --target Personal to 2025-01-08";
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand(copyCommand);
    });
    assertTrue(exception.getMessage().contains("No events found"));
  }

  @Test
  public void testCopyEventsBetweenTargetCalendarNotFound() throws Exception {
    controller.processCommand("create event \"Meeting\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("copy events between 2024-09-09 "
          + "and 2024-09-09 --target FakeCalendar to 2025-01-08");
    });
    assertTrue(exception.getMessage().contains("not found")
        || exception.getMessage().contains("Target calendar"));
  }

  @Test
  public void testCopyEventsBetweenMixedEvents() throws Exception {
    controller.processCommand("create event \"Single Event\" "
        + "from 2024-09-05T10:00 to 2024-09-05T11:00");
    controller.processCommand("create event \"Series Event\" "
        + "from 2024-09-09T14:00 to 2024-09-09T15:00 repeats M for 3 times");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(4, sourceCalendar.getEventCount());
    controller.processCommand("copy events between "
        + "2024-09-05 and 2024-09-23 --target Personal to 2025-01-08");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(4, targetCalendar.getEventCount());
    Event singleEvent = targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Single Event"))
        .findFirst()
        .orElseThrow();
    assertTrue(singleEvent.getSeriesId() == null || singleEvent.getSeriesId().isEmpty());
    Set<String> seriesIds = new HashSet<>();
    targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Series Event"))
        .forEach(e -> {
          assertNotNull(e.getSeriesId());
          seriesIds.add(e.getSeriesId());
        });
    assertEquals(1, seriesIds.size());
  }

  @Test
  public void testWeekdayConsistencyWhenAlreadyMatches() throws Exception {
    controller.processCommand("create event \"Monday Task\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("copy event \"Monday Task\" on "
        + "2024-09-09T10:00 --target Personal to 2025-01-13T10:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertEquals(13, copiedEvent.getStartDateTime().getDayOfMonth());
  }

  @Test
  public void testWeekdayAdjustmentForAllDaysOfWeek() throws Exception {
    controller.processCommand("create event \"Monday\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("create event \"Tuesday\" "
        + "from 2024-09-10T10:00 to 2024-09-10T11:00");
    controller.processCommand("create event \"Wednesday\" "
        + "from 2024-09-11T10:00 to 2024-09-11T11:00");
    controller.processCommand("create event \"Thursday\" "
        + "from 2024-09-12T10:00 to 2024-09-12T11:00");
    controller.processCommand("create event \"Friday\" "
        + "from 2024-09-13T10:00 to 2024-09-13T11:00");
    controller.processCommand("copy events between 2024-09-09 and "
        + "2024-09-13 --target Personal to 2025-01-11");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(5, targetCalendar.getEventCount());
    for (Event e : targetCalendar.getEvents()) {
      switch (e.getSubject()) {
        case "Monday":
          assertEquals(DayOfWeek.MONDAY, e.getStartDateTime().getDayOfWeek());
          break;
        case "Tuesday":
          assertEquals(DayOfWeek.TUESDAY, e.getStartDateTime().getDayOfWeek());
          break;
        case "Wednesday":
          assertEquals(DayOfWeek.WEDNESDAY, e.getStartDateTime().getDayOfWeek());
          break;
        case "Thursday":
          assertEquals(DayOfWeek.THURSDAY, e.getStartDateTime().getDayOfWeek());
          break;
        case "Friday":
          assertEquals(DayOfWeek.FRIDAY, e.getStartDateTime().getDayOfWeek());
          break;
        default:
          break;
      }
    }
    System.out.println("✓ TEST PASSED: Weekday consistency maintained for all days of week");
  }

  @Test
  public void testCopyWithinSameCalendar() throws Exception {
    controller.processCommand("create event \"Team Meeting\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(1, sourceCalendar.getEventCount());
    controller.processCommand("copy events between "
        + "2024-09-09 and 2024-09-09 --target Work to 2025-05-05");
    sourceCalendar = calendarModel.calendarModel("Work");
    assertEquals(2, sourceCalendar.getEventCount());
  }

  @Test
  public void testCopyWithinSameCalendarNoTimezoneConversion() throws Exception {
    controller.processCommand("create event \"Meeting\" from 2024-09-09T14:00 to 2024-09-09T15:00");
    controller.processCommand("copy events on 2024-09-09 --target Work to 2025-01-13");
    sourceCalendar = calendarModel.calendarModel("Work");
    for (Event e : sourceCalendar.getEvents()) {
      if (e.getSubject().equals("Meeting")) {
        assertEquals(14, e.getStartDateTime().getHour());
      }
    }
    System.out.println("✓ TEST PASSED: Copy within same calendar doesn't convert timezone");
  }

  @Test
  public void testCopyMultiDayEvent() throws Exception {
    controller.processCommand("create event \"Conference\" "
        + "from 2024-09-09T09:00 to 2024-09-11T17:00");
    sourceCalendar = calendarModel.calendarModel("Work");
    controller.processCommand("copy event \"Conference\" "
        + "on 2024-09-09T09:00 --target Personal to 2025-01-07T09:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertEquals(7, copiedEvent.getStartDateTime().getDayOfMonth());
    assertEquals(9, copiedEvent.getEndDateTime().getDayOfMonth());
    System.out.println("✓ TEST PASSED: Multi-day event preserves duration and weekday span");
  }

  @Test
  public void testMultipleSeriesGetDifferentNewIds() throws Exception {
    controller.processCommand("create event \"Series A\" "
        + "from 2024-09-09T09:00 to 2024-09-09T10:00 repeats M for 3 times");
    controller.processCommand("create event \"Series B\" "
        + "from 2024-09-10T14:00 to 2024-09-10T15:00 repeats M for 3 times");
    sourceCalendar = calendarModel.calendarModel("Work");
    controller.processCommand("copy events between 2024-09-09 "
        + "and 2024-09-24 --target Personal to 2025-01-13");
    targetCalendar = calendarModel.calendarModel("Personal");
    Set<String> newSeriesAids = new HashSet<>();
    Set<String> newSeriesBids = new HashSet<>();
    for (Event e : targetCalendar.getEvents()) {
      if (e.getSubject().equals("Series A")) {
        newSeriesAids.add(e.getSeriesId());
      } else if (e.getSubject().equals("Series B")) {
        newSeriesBids.add(e.getSeriesId());
      }
    }
    assertEquals(1, newSeriesAids.size());
    assertEquals(1, newSeriesBids.size());
    String newSeriesAid = newSeriesAids.iterator().next();
    String newSeriesBid = newSeriesBids.iterator().next();
    assertNotEquals(newSeriesAid, newSeriesBid);
    String seriesAid = sourceCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Series A")).findFirst().orElseThrow().getSeriesId();
    String seriesBid = sourceCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Series B")).findFirst().orElseThrow().getSeriesId();
    assertNotEquals(seriesAid, newSeriesAid);
    assertNotEquals(seriesBid, newSeriesBid);
  }

  @Test
  public void testCopyEventsBetweenDuplicateFails() throws Exception {
    controller.processCommand("create event \"Meeting\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("copy events between "
        + "2024-09-09 and 2024-09-09 --target Personal to 2025-01-13");
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("copy events between 2024-09-09 "
          + "and 2024-09-09 --target Personal to 2025-01-13");
    });
    assertTrue(exception.getMessage().contains("already exists")
        || exception.getMessage().contains("duplicate"));
  }

  @Test
  public void testCopySeriesAllOrNothingBehavior() throws Exception {
    controller.processCommand("create event \"Daily Standup\" from "
        + "2024-09-09T08:30 to 2024-09-09T09:00 repeats M for 5 times");
    controller.processCommand("use calendar --name Personal");
    controller.processCommand("create event \"Daily Standup\" from "
        + "2025-01-27T05:30 to 2025-01-27T06:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(1, targetCalendar.getEventCount());
    controller.processCommand("use calendar --name Work");
    Exception exception = assertThrows(Exception.class, () -> {
      controller.processCommand("copy events between 2024-09-09 "
          + "and 2024-10-07 --target Personal to 2025-01-27");
    });
    assertTrue(exception.getMessage().contains("already exists")
        || exception.getMessage().contains("duplicate"));
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(1, targetCalendar.getEventCount());
  }

  @Test
  public void testTimezoneConversionEstToPst() throws Exception {
    controller.processCommand("create event \"Morning\" from 2024-09-09T09:00 to 2024-09-09T10:00");
    controller.processCommand("create event \"Afternoon\" "
        + "from 2024-09-09T14:00 to 2024-09-09T15:00");
    controller.processCommand("create event \"Evening\" from 2024-09-09T17:00 to 2024-09-09T18:00");
    controller.processCommand("copy events on 2024-09-09 --target Personal to 2025-01-13");
    targetCalendar = calendarModel.calendarModel("Personal");
    for (Event e : targetCalendar.getEvents()) {
      switch (e.getSubject()) {
        case "Morning":
          assertEquals(6, e.getStartDateTime().getHour());
          break;
        case "Afternoon":
          assertEquals(11, e.getStartDateTime().getHour());
          break;
        case "Evening":
          assertEquals(14, e.getStartDateTime().getHour());
          break;
        default:
          break;
      }
    }
    System.out.println("✓ TEST PASSED: Timezone conversion EST to PST works correctly");
  }

  @Test
  public void testRelativeSpacingPreserved() throws Exception {
    controller.processCommand("create event \"Event1\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("create event \"Event2\" "
        + "from 2024-09-11T14:00 to 2024-09-11T15:00");
    controller.processCommand("create event \"Event3\" "
        + "from 2024-09-16T09:00 to 2024-09-16T10:00");
    controller.processCommand("copy events between 2024-09-09 "
        + "and 2024-09-16 --target Personal to 2025-01-13");
    targetCalendar = calendarModel.calendarModel("Personal");
    assertEquals(3, targetCalendar.getEventCount());
    Event event1 = targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Event1"))
        .findFirst()
        .orElseThrow();
    Event event2 = targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Event2"))
        .findFirst()
        .orElseThrow();
    Event event3 = targetCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals("Event3"))
        .findFirst()
        .orElseThrow();
    assertEquals(2, event2.getStartDateTime().getDayOfMonth()
        - event1.getStartDateTime().getDayOfMonth());
    assertEquals(7, event3.getStartDateTime().getDayOfMonth()
        - event1.getStartDateTime().getDayOfMonth());
    System.out.println("✓ TEST PASSED: Relative spacing between events preserved");
  }

  @Test
  public void testCopyEventWithNoActiveCalendar() throws Exception {
    Icalendarcollection freshRepository = new CalendarCollection();
    CalendarModel freshService = new CalendarModel(freshRepository);
    EventController freshController = new EventController(
        new calendar.test.MockInputSource(),
        freshService,
        new CommandParserService(),
        new calendar.test.MockView()
    );

    freshController.processCommand("create calendar --name Test --timezone America/New_York");
    Exception exception = assertThrows(Exception.class, () -> {
      freshController.processCommand("copy events on 2024-09-09 --target Test to 2025-01-13");
    });
    assertTrue(exception.getMessage().contains("No calendar")
        || exception.getMessage().contains("not selected"));
    System.out.println("✓ TEST PASSED: Copy fails when no calendar is active");
  }

  @Test
  public void testCopyEventsOnDateAlreadyOnCorrectWeekday() throws Exception {
    controller.processCommand("create event \"Thursday Event\" "
        + "from 2024-09-05T10:00 to 2024-09-05T11:00");
    controller.processCommand("copy events on 2024-09-05 --target Personal to 2025-01-09");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copiedEvent = targetCalendar.getEvents().iterator().next();
    assertEquals(9, copiedEvent.getStartDateTime().getDayOfMonth());
    assertEquals(DayOfWeek.THURSDAY, copiedEvent.getStartDateTime().getDayOfWeek());
    System.out.println("✓ TEST PASSED: No weekday adjustment when target already matches");
  }

  @Test
  public void testCopyPreservesAllEventProperties() throws Exception {
    controller.processCommand("create event \"Detailed Event\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00");
    controller.processCommand("edit event location \"Detailed Event\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00 with \"Room 101\"");
    controller.processCommand("edit event description \"Detailed Event\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00 with \"Important meeting\"");
    controller.processCommand("edit event status \"Detailed Event\" "
        + "from 2024-09-09T10:00 to 2024-09-09T11:00 with \"confirmed\"");
    sourceCalendar = calendarModel.calendarModel("Work");
    Event original = sourceCalendar.getEvents().iterator().next();
    controller.processCommand("copy event \"Detailed Event\" "
        + "on 2024-09-09T10:00 --target Personal to 2025-01-13T10:00");
    targetCalendar = calendarModel.calendarModel("Personal");
    Event copied = targetCalendar.getEvents().iterator().next();
    assertEquals(original.getSubject(), copied.getSubject());
    assertEquals(original.getLocation(), copied.getLocation());
    assertEquals(original.getDescription(), copied.getDescription());
    assertEquals(original.getStatus(), copied.getStatus());
    System.out.println("✓ TEST PASSED: Copy preserves all event properties");
  }
}