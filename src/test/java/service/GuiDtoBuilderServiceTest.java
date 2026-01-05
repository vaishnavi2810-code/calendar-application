package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import calendar.dto.CalendarDto;
import calendar.dto.CreateEventDto;
import calendar.dto.QueryEventDto;
import calendar.service.GuiDtoBuilderService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for GuiBuilderService.
 * Verifies that the builder service correctly constructs Data Transfer Objects (DTOs)
 * for various calendar and event operations, ensuring proper formatting of dates,
 * times, and weekday strings.
 */
public class GuiDtoBuilderServiceTest {

  private GuiDtoBuilderService builder;

  /**
   * Initializes the GuiDtoBuilderService instance before each test.
   */
  @Before
  public void setUp() {
    builder = new GuiDtoBuilderService();
  }

  /**
   * Verifies that a CalendarDto is correctly built for creating a new calendar.
   * Checks that the calendar name and timezone are correctly populated.
   */
  @Test
  public void testBuildCreateCalendarDto() {
    CalendarDto dto = builder.buildCreateCalendarDto("Work", "America/New_York");
    assertNotNull(dto);
    assertEquals("Work", dto.getCalendarName());
    assertEquals("America/New_York", dto.getTimeZone());
  }

  /**
   * Verifies that a CalendarDto is correctly built for switching the active calendar.
   */
  @Test
  public void testBuildUseCalendarDto() {
    CalendarDto dto = builder.buildUseCalendarDto("Personal");
    assertNotNull(dto);
    assertEquals("Personal", dto.getCalendarName());
  }

  /**
   * Verifies that a CalendarDto is correctly built for editing calendar properties.
   * Checks that the property name and new value are correctly mapped.
   */
  @Test
  public void testBuildEditCalendarDto() {
    CalendarDto dto = builder.buildEditCalendarDto("Work", "timezone", "UTC");
    assertNotNull(dto);
    assertEquals("Work", dto.getCalendarName());
    assertEquals("timezone", dto.getPropertyName());
    assertEquals("UTC", dto.getPropertyValue());
  }

  /**
   * Verifies the construction of a DTO for a single, non-recurring timed event.
   * Ensures start and end date-times are formatted correctly as ISO-8601 strings.
   */
  @Test
  public void testBuildTimedSingleEventDto() {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 14, 30);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 16, 0);

    CreateEventDto dto = builder.buildTimedSingleEventDto("Meeting", start, end);
    assertNotNull(dto);
    assertEquals("Meeting", dto.getSubject());
    assertEquals("2023-10-01T14:30", dto.getStartDateTime());
    assertEquals("2023-10-01T16:00", dto.getEndDateTime());
  }

  /**
   * Verifies the construction of a DTO for a timed event that recurs a specific number of times.
   * Checks for correct weekday formatting and recurrence count.
   */
  @Test
  public void testBuildTimedRecurringForDto() {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 10, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);
    CreateEventDto dto = builder.buildTimedRecurringForDto("Class", start, end, days, 5);
    assertEquals("Class", dto.getSubject());
    assertEquals("2023-10-01T09:00", dto.getStartDateTime());
    assertEquals("MWF", dto.getWeekdays());
    assertEquals("5", dto.getnTimes());
  }

  /**
   * Verifies the construction of a DTO for a timed event that recurs until a specific date.
   * Checks that the 'until' date is formatted correctly.
   */
  @Test
  public void testBuildTimedRecurringUntilDto() {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDate until = LocalDate.of(2023, 12, 25);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.THURSDAY);
    CreateEventDto dto = builder.buildTimedRecurringUntilDto("Gym", start, end, days, until);
    assertEquals("Gym", dto.getSubject());
    assertEquals("TR", dto.getWeekdays());
    assertEquals("2023-12-25", dto.getUntilDate());
  }

  /**
   * Verifies the construction of a DTO for a single all-day event.
   * Ensures the date is formatted as YYYY-MM-DD.
   */
  @Test
  public void testBuildAllDaySingleEventDto() {
    LocalDate date = LocalDate.of(2023, 11, 15);
    CreateEventDto dto = builder.buildAllDaySingleEventDto("Birthday", date);
    assertEquals("Birthday", dto.getSubject());
    assertEquals("2023-11-15", dto.getOnDate());
  }

  /**
   * Verifies the construction of a DTO for an all-day event recurring a specific number of times.
   */
  @Test
  public void testBuildAllDayRecurringForDto() {
    LocalDate date = LocalDate.of(2023, 1, 1);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.SATURDAY);
    days.add(DayOfWeek.SUNDAY);
    CreateEventDto dto = builder.buildAllDayRecurringForDto("Weekend", date, days, 10);
    assertEquals("Weekend", dto.getSubject());
    assertEquals("SU", dto.getWeekdays());
    assertEquals("10", dto.getnTimes());
  }

  /**
   * Verifies the construction of a DTO for an all-day event recurring until a specific date.
   */
  @Test
  public void testBuildAllDayRecurringUntilDto() {
    LocalDate date = LocalDate.of(2023, 1, 1);
    LocalDate until = LocalDate.of(2023, 2, 1);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);
    CreateEventDto dto = builder.buildAllDayRecurringUntilDto("Friday Fun", date, days, until);
    assertEquals("Friday Fun", dto.getSubject());
    assertEquals("F", dto.getWeekdays());
    assertEquals("2023-02-01", dto.getUntilDate());
  }

  /**
   * Verifies that the query DTO for a specific date is built with the correct format.
   */
  @Test
  public void testBuildQueryForDate() {
    LocalDate date = LocalDate.of(2024, 5, 5);
    QueryEventDto dto = builder.buildQueryForDate(date);
    assertNotNull(dto);
    assertEquals("2024-05-05", dto.getOnDate());
  }

  /**
   * Verifies that the query DTO for a date range correctly formats the start and end timestamps.
   */
  @Test
  public void testBuildQueryForRange() {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
    QueryEventDto dto = builder.buildQueryForRange(start, end);
    assertEquals("2024-01-01T00:00", dto.getRangeStart());
    assertEquals("2024-01-31T23:59", dto.getRangeEnd());
  }

  /**
   * Verifies that the query DTO for checking status at a specific instant is formatted correctly.
   */
  @Test
  public void testBuildQueryForStatus() {
    LocalDateTime instant = LocalDateTime.of(2024, 6, 15, 12, 30);
    QueryEventDto dto = builder.buildQueryForStatus(instant);
    assertEquals("2024-06-15T12:30", dto.getAtInstant());
  }

  /**
   * Verifies that weekday abbreviations are correctly ordered (e.g., M, T, W, R, F, S, U)
   * regardless of the insertion order in the set.
   */
  @Test
  public void testFormatWeekdaysOrdering() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.SUNDAY);
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    CreateEventDto dto = builder.buildTimedRecurringForDto("Test",
            LocalDateTime.now(), LocalDateTime.now(), days, 1);
    assertEquals("MWU", dto.getWeekdays());
  }

  /**
   * Verifies that attempting to build a calendar DTO with a null name throws an exception.
   */
  @Test
  public void testBuildCreateCalendarDtoWithNullName() {
    assertThrows(IllegalArgumentException.class, () -> {
      builder.buildCreateCalendarDto(null, "UTC");
    });
  }

  /**
   * Verifies that attempting to build a calendar DTO with an empty name throws an exception.
   */
  @Test
  public void testBuildCreateCalendarDtoWithEmptyName() {
    assertThrows(IllegalArgumentException.class, () -> {
      builder.buildCreateCalendarDto("", "UTC");
    });
  }
}