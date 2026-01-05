package model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import calendar.dto.CreateEventDto;
import org.junit.Test;

/**
 * Unit tests for the CreateEventDto factory methods.
 * Verifies that the static factory methods correctly populate the DTO fields
 * and assign the correct CommandType.
 */
public class CreateEventDtoTest {
  @Test
  public void testTimedSingleFactory() {
    String subject = "Meeting";
    String start = "2023-10-01T10:00";
    String end = "2023-10-01T11:00";

    CreateEventDto dto = CreateEventDto.timedSingle(subject, start, end);

    assertEquals("Type should be TIMED_SINGLE",
                CreateEventDto.CommandType.TIMED_SINGLE, dto.getType());
    assertEquals(subject, dto.getSubject());
    assertEquals(start, dto.getStartDateTime());
    assertEquals(end, dto.getEndDateTime());

    assertNull(dto.getOnDate());
    assertNull(dto.getWeekdays());
    assertNull(dto.getnTimes());
    assertNull(dto.getUntilDate());
  }

  @Test
  public void testAllDaySingleFactory() {
    String subject = "Holiday";
    String date = "2023-12-25";

    CreateEventDto dto = CreateEventDto.allDaySingle(subject, date);

    assertEquals("Type should be ALL_DAY_SINGLE",
                CreateEventDto.CommandType.ALL_DAY_SINGLE, dto.getType());
    assertEquals(subject, dto.getSubject());
    assertEquals(date, dto.getOnDate());
    assertNull(dto.getStartDateTime());
    assertNull(dto.getEndDateTime());
    assertNull(dto.getWeekdays());
  }

  @Test
  public void testTimedRecurringForFactory() {
    String subject = "Class";
    String start = "2023-10-01T10:00";
    String end = "2023-10-01T11:00";
    String weekdays = "MWF";
    String times = "5";

    CreateEventDto dto = CreateEventDto.timedRecurringFor(subject, start, end, weekdays, times);

    assertEquals(CreateEventDto.CommandType.TIMED_RECURRING_FOR, dto.getType());
    assertEquals(subject, dto.getSubject());
    assertEquals(start, dto.getStartDateTime());
    assertEquals(end, dto.getEndDateTime());
    assertEquals(weekdays, dto.getWeekdays());
    assertEquals(times, dto.getnTimes());

    assertNull(dto.getUntilDate());
  }

  @Test
  public void testTimedRecurringUntilFactory() {
    String subject = "Sync";
    String start = "2023-10-01T09:00";
    String end = "2023-10-01T09:30";
    String weekdays = "TR";
    String until = "2023-12-01";

    CreateEventDto dto = CreateEventDto.timedRecurringUntil(subject, start, end, weekdays, until);

    assertEquals(CreateEventDto.CommandType.TIMED_RECURRING_UNTIL, dto.getType());
    assertEquals(subject, dto.getSubject());
    assertEquals(start, dto.getStartDateTime());
    assertEquals(end, dto.getEndDateTime());
    assertEquals(weekdays, dto.getWeekdays());
    assertEquals(until, dto.getUntilDate());

    assertNull(dto.getnTimes());
  }

  @Test
  public void testAllDayRecurringForFactory() {
    String subject = "Cleanup";
    String date = "2023-10-01";
    String weekdays = "S";
    String times = "3";

    CreateEventDto dto = CreateEventDto.allDayRecurringFor(subject, date, weekdays, times);
    assertEquals(CreateEventDto.CommandType.ALL_DAY_RECURRING_FOR, dto.getType());
    assertEquals(subject, dto.getSubject());
    assertEquals(date, dto.getOnDate());
    assertEquals(weekdays, dto.getWeekdays());
    assertEquals(times, dto.getnTimes());
    assertNull(dto.getStartDateTime());
    assertNull(dto.getEndDateTime());
  }

  @Test
  public void testAllDayRecurringUntilFactory() {
    String subject = "Vacation";
    String date = "2023-07-01";
    String weekdays = "MTWRF";
    String until = "2023-07-10";

    CreateEventDto dto = CreateEventDto.allDayRecurringUntil(subject, date, weekdays, until);

    assertEquals(CreateEventDto.CommandType.ALL_DAY_RECURRING_UNTIL, dto.getType());
    assertEquals(subject, dto.getSubject());
    assertEquals(date, dto.getOnDate());
    assertEquals(weekdays, dto.getWeekdays());
    assertEquals(until, dto.getUntilDate());

    assertNull(dto.getnTimes());
  }
}