package calendar.strategy;

import calendar.dto.CopyEventDto;
import calendar.model.Calendar;
import calendar.model.Event;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for copying all events within a date range.
 * Maintains weekday consistency by adjusting the target date to match the first event's weekday.
 * All events are shifted by the same number of days to preserve relative positions.
 */
public class CopyEventsBetweenDates extends AbstractCopyEventsByDate {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Override
  protected List<Event> getEventsInDateRange(CopyEventDto dto, Calendar sourceCalendar)
      throws Exception {
    LocalDate intervalStartDate = LocalDate.parse(dto.getIntervalStartDate(), FORMATTER);
    LocalDate intervalEndDate = LocalDate.parse(dto.getIntervalEndDate(), FORMATTER);

    ZonedDateTime intervalStart = intervalStartDate.atStartOfDay(sourceCalendar.getTimezone());
    ZonedDateTime intervalEnd = intervalEndDate.atTime(23, 59, 59)
        .atZone(sourceCalendar.getTimezone());

    return sourceCalendar.getEvents().stream()
        .filter(e -> !e.getStartDateTime().isAfter(intervalEnd)
            && !e.getEndDateTime().isBefore(intervalStart))
        .collect(Collectors.toList());
  }

  @Override
  protected LocalDate calculateTargetDate(CopyEventDto dto, LocalDate firstEventDate)
      throws Exception {
    LocalDate userProvidedTargetDate = LocalDate.parse(dto.getTargetStartDate(), FORMATTER);
    DayOfWeek firstEventWeekday = firstEventDate.getDayOfWeek();

    // Adjust target date to match the first event's weekday
    return adjustToMatchWeekday(userProvidedTargetDate, firstEventWeekday);
  }

  /**
   * Adjusts the target date to match the source weekday.
   * If the target date already falls on the same weekday, it is returned unchanged.
   * Otherwise, the date is adjusted forward to the next occurrence of that weekday.
   */
  private LocalDate adjustToMatchWeekday(LocalDate targetDate, DayOfWeek sourceWeekday) {
    DayOfWeek targetWeekday = targetDate.getDayOfWeek();
    if (targetWeekday == sourceWeekday) {
      return targetDate;
    }
    return targetDate.with(TemporalAdjusters.next(sourceWeekday));
  }

  @Override
  protected String getNoEventsFoundMessage(CopyEventDto dto) {
    return "No events found between "
        + dto.getIntervalStartDate() + " and " + dto.getIntervalEndDate();
  }
}