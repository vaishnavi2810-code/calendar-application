package calendar.service;

import calendar.dto.CalendarDto;
import calendar.dto.CreateEventDto;
import calendar.dto.EditEventDto;
import calendar.dto.QueryEventDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * Service that builds DTOs from GUI input data.
 */
public class GuiDtoBuilderService {

  private static final DateTimeFormatter DATE_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


  /**
   * Builds a DTO for creating a new calendar.
   */
  public CalendarDto buildCreateCalendarDto(String name, String timezone) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("name is null or empty");
    }
    return CalendarDto.createCalendar(name, timezone);
  }

  /**
   * Builds a DTO for switching to a calendar.
   */
  public CalendarDto buildUseCalendarDto(String name) {
    return CalendarDto.useCalendar(name);
  }

  /**
   * Builds a DTO for editing a calendar property.
   */
  public CalendarDto buildEditCalendarDto(String name, String property, String value) {
    return CalendarDto.editCalendar(name, property, value);
  }


  /**
   * Builds a DTO for a timed single event.
   */
  public CreateEventDto buildTimedSingleEventDto(String subject, LocalDateTime start,
                                                 LocalDateTime end) {
    String startStr = start.format(DATETIME_FORMAT);
    String endStr = end.format(DATETIME_FORMAT);
    return CreateEventDto.timedSingle(subject, startStr, endStr);
  }

  /**
   * Builds a DTO for a recurring timed event with fixed repetitions.
   */
  public CreateEventDto buildTimedRecurringForDto(String subject, LocalDateTime start,
                                                  LocalDateTime end, Set<DayOfWeek> weekdays,
                                                  int times) {
    String startStr = start.format(DATETIME_FORMAT);
    String endStr = end.format(DATETIME_FORMAT);
    String weekdaysStr = formatWeekdays(weekdays);
    return CreateEventDto.timedRecurringFor(subject, startStr, endStr,
            weekdaysStr, String.valueOf(times));
  }

  /**
   * Builds a DTO for a recurring timed event until a date.
   */
  public CreateEventDto buildTimedRecurringUntilDto(String subject, LocalDateTime start,
                                                    LocalDateTime end, Set<DayOfWeek> weekdays,
                                                    LocalDate until) {
    String startStr = start.format(DATETIME_FORMAT);
    String endStr = end.format(DATETIME_FORMAT);
    String weekdaysStr = formatWeekdays(weekdays);
    String untilStr = until.format(DATE_FORMAT);
    return CreateEventDto.timedRecurringUntil(subject, startStr, endStr,
            weekdaysStr, untilStr);
  }

  /**
   * Builds a DTO for a single all-day event.
   */
  public CreateEventDto buildAllDaySingleEventDto(String subject, LocalDate date) {
    String dateStr = date.format(DATE_FORMAT);
    return CreateEventDto.allDaySingle(subject, dateStr);
  }

  /**
   * Builds a DTO for a recurring all-day event with fixed repetitions.
   */
  public CreateEventDto buildAllDayRecurringForDto(String subject, LocalDate date,
                                                   Set<DayOfWeek> weekdays, int times) {
    String dateStr = date.format(DATE_FORMAT);
    String weekdaysStr = formatWeekdays(weekdays);
    return CreateEventDto.allDayRecurringFor(subject, dateStr,
            weekdaysStr, String.valueOf(times));
  }

  /**
   * Builds a DTO for a recurring all-day event until a date.
   */
  public CreateEventDto buildAllDayRecurringUntilDto(String subject, LocalDate date,
                                                     Set<DayOfWeek> weekdays, LocalDate until) {
    String dateStr = date.format(DATE_FORMAT);
    String weekdaysStr = formatWeekdays(weekdays);
    String untilStr = until.format(DATE_FORMAT);
    return CreateEventDto.allDayRecurringUntil(subject, dateStr, weekdaysStr, untilStr);
  }

  /**
   * Builds a query DTO for events on a specific date.
   *
   * @param date the date to query
   * @return configured QueryEventDto
   */
  public QueryEventDto buildQueryForDate(LocalDate date) {
    String dateStr = date.format(DATE_FORMAT);
    return QueryEventDto.forDate(dateStr);
  }

  /**
   * Builds a DTO for editing a single event instance with multiple properties.
   */
  public EditEventDto buildEditSingleEventDto(String targetSubject,
                                              LocalDateTime targetStart,
                                              LocalDateTime targetEnd,
                                              Map<String, String> propertyChanges) {
    String targetStartStr = targetStart.format(DATETIME_FORMAT);
    String targetEndStr = targetEnd.format(DATETIME_FORMAT);

    return EditEventDto.editSingle(targetSubject, targetStartStr, targetEndStr, propertyChanges);
  }

  /**
   * Builds a DTO for editing all events in a series with multiple properties.
   */
  public EditEventDto buildEditSeriesDto(String targetSubject,
                                         LocalDateTime targetStart,
                                         Map<String, String> propertyChanges) {
    String targetStartStr = targetStart.format(DATETIME_FORMAT);

    return EditEventDto.editSeries(targetSubject, targetStartStr, propertyChanges);
  }

  /**
   * Builds a DTO for editing this and following events with multiple properties.
   */
  public EditEventDto buildEditForwardDto(String targetSubject,
                                          LocalDateTime fromStart,
                                          Map<String, String> propertyChanges) {
    String fromStartStr = fromStart.format(DATETIME_FORMAT);

    return EditEventDto.editForward(targetSubject, fromStartStr, propertyChanges);
  }

  /**
   * Builds a query DTO for events in a datetime range.
   *
   * @param start start datetime
   * @param end end datetime
   * @return configured QueryEventDto
   */
  public QueryEventDto buildQueryForRange(LocalDateTime start, LocalDateTime end) {
    String startStr = start.format(DATETIME_FORMAT);
    String endStr = end.format(DATETIME_FORMAT);
    return QueryEventDto.forRange(startStr, endStr);
  }

  /**
   * Builds a query DTO for status at a specific instant.
   *
   * @param instant the instant to check status
   * @return configured QueryEventDto
   */
  public QueryEventDto buildQueryForStatus(LocalDateTime instant) {
    String instantStr = instant.format(DATETIME_FORMAT);
    return QueryEventDto.forStatus(instantStr);
  }

  private String formatWeekdays(Set<DayOfWeek> weekdays) {
    StringBuilder sb = new StringBuilder();
    if (weekdays.contains(DayOfWeek.MONDAY)) {
      sb.append('M');
    }
    if (weekdays.contains(DayOfWeek.TUESDAY)) {
      sb.append('T');
    }
    if (weekdays.contains(DayOfWeek.WEDNESDAY)) {
      sb.append('W');
    }
    if (weekdays.contains(DayOfWeek.THURSDAY)) {
      sb.append('R');
    }
    if (weekdays.contains(DayOfWeek.FRIDAY)) {
      sb.append('F');
    }
    if (weekdays.contains(DayOfWeek.SATURDAY)) {
      sb.append('S');
    }
    if (weekdays.contains(DayOfWeek.SUNDAY)) {
      sb.append('U');
    }
    return sb.toString();
  }
}