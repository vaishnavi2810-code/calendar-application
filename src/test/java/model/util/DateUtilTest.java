package model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.Event;
import calendar.util.EditEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * class to test util functionality.
 */
public class DateUtilTest {

  @Test
  public void testExtractValidWeekdaysMixedDays() {
    List<Event> events = new ArrayList<>();
    events.add(createMockEvent(LocalDateTime.of(2025, 11, 24, 10, 0)));
    events.add(createMockEvent(LocalDateTime.of(2025, 11, 26, 10, 0)));
    Set<DayOfWeek> result = EditEvent.extractValidWeekdays(events);
    assertEquals("Should find exactly 2 distinct weekdays", 2, result.size());
    assertTrue(result.contains(DayOfWeek.MONDAY));
    assertTrue(result.contains(DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testExtractValidWeekdaysRemovesDuplicates() {
    List<Event> events = new ArrayList<>();
    events.add(createMockEvent(LocalDateTime.of(2025, 11, 24, 10, 0)));
    events.add(createMockEvent(LocalDateTime.of(2025, 11, 24, 14, 0)));
    Set<DayOfWeek> result = EditEvent.extractValidWeekdays(events);
    assertEquals("Should collapse duplicate Mondays into size 1", 1, result.size());
    assertTrue(result.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testExtractValidWeekdaysEmptyInput() {
    List<Event> events = new ArrayList<>();
    Set<DayOfWeek> result = EditEvent.extractValidWeekdays(events);
    assertTrue("Empty list should result in empty set", result.isEmpty());
  }

  @Test
  public void testFindNextValidTodayIsMatch() {
    LocalDate monday = LocalDate.of(2025, 11, 24);
    Set<DayOfWeek> validDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    LocalDate result = EditEvent.findNextValidWeekday(monday, validDays);
    assertEquals("If start date is valid, return start date", monday, result);
  }

  @Test
  public void testFindNextValidNextMatchIsTomorrow() {
    LocalDate monday = LocalDate.of(2025, 11, 24);
    Set<DayOfWeek> validDays = Set.of(DayOfWeek.TUESDAY);
    LocalDate result = EditEvent.findNextValidWeekday(monday, validDays);
    assertEquals("Should advance to tomorrow (Tuesday)",
          monday.plusDays(1), result);
  }

  @Test
  public void testFindNextValidWrapAroundWeek() {
    LocalDate monday = LocalDate.of(2025, 11, 24);
    Set<DayOfWeek> validDays = Set.of(DayOfWeek.SUNDAY);
    LocalDate result = EditEvent.findNextValidWeekday(monday, validDays);
    assertEquals("Should advance 6 days to find Sunday",
                DayOfWeek.SUNDAY, result.getDayOfWeek());
    assertEquals(monday.plusDays(6), result);
  }

  @Test
  public void testFindNextValidNoValidDaysThrowsException() {
    LocalDate today = LocalDate.now();
    Set<DayOfWeek> emptySet = new HashSet<>();
    try {
      EditEvent.findNextValidWeekday(today, emptySet);
      fail("Should have thrown IllegalStateException because set is empty");
    } catch (IllegalStateException e) {
      assertEquals("No valid weekday found", e.getMessage());
    }
  }

  private Event createMockEvent(LocalDateTime localStart) {
    ZonedDateTime zdtStart = ZonedDateTime.of(localStart, ZoneId.of("UTC"));
    ZonedDateTime zdtEnd = zdtStart.plusHours(1);
    return new Event("Test", zdtStart, zdtEnd, null, "", "", "");
  }
}