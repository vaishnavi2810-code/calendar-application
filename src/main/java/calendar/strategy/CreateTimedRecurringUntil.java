package calendar.strategy;

import static calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER;
import static calendar.constants.CreateCalendarConstants.parseWeekdays;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Strategy implementation for creating recurring timed events until a specified end date.
 * This class generates a series of events that occur at the same time on specified weekdays,
 * continuing until the specified end date is reached. All events in the series share a common
 * series ID for identification purposes. The start and end times must be on the same date.
 */
public class CreateTimedRecurringUntil implements Icreate {

  /**
   * Creates a set of recurring timed events based on the specified parameters.
   * Starting from the given date, iterates through subsequent days and creates events
   * on matching weekdays until the specified end date (inclusive). Each event occurs at
   * the same time of day and is assigned the same series ID to indicate they belong to
   * the same recurring series. Validates that the end time is after the start time,
   * that both times occur on the same date, and that the until date is not before the start date.
   *
   * @param data the data transfer object containing event creation parameters including
   *             subject, start date-time, end date-time, weekdays pattern, and until date
   * @param list the existing set of events to check for conflicts before creating new events
   * @return a set containing all newly created event occurrences up to and including the until date
   * @throws Exception if the end time is before the start time, if start and end are not
   *                   on the same date, if the until date is before the start date,
   *                   if any event in the series conflicts with an existing event,
   *                   or if the dates cannot be parsed
   */
  @Override
  public Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId timezone) throws Exception {
    LocalDateTime floatingStart = LocalDateTime.parse(data.getStartDateTime(), DATETIME_FORMATTER);
    LocalDateTime floatingEnd = LocalDateTime.parse(data.getEndDateTime(), DATETIME_FORMATTER);
    if (floatingEnd.isBefore(floatingStart)) {
      throw new Exception("Error: Event end time cannot be before its start time.");
    }
    LocalDate date = floatingStart.toLocalDate();
    LocalTime startTime = floatingStart.toLocalTime();
    LocalTime endTime = floatingEnd.toLocalTime();
    String seriesId = UUID.randomUUID().toString();
    LocalDate untillDate = LocalDate.parse(data.getUntilDate());
    HashSet<Event> result = new HashSet<>();
    if (!floatingStart.toLocalDate().equals(floatingEnd.toLocalDate())) {
      throw new Exception("Error: Recurring events must start and end on the same day.");
    }
    if (untillDate.isBefore(date)) {
      throw new Exception("Error: 'until' date cannot be before the event's start date.");
    }
    Set<DayOfWeek> wantedDays = parseWeekdays(data.getWeekdays());
    while (!date.isAfter(untillDate)) {
      if (wantedDays.contains(date.getDayOfWeek())) {
        ZonedDateTime startDateTime = ZonedDateTime.of(date, startTime, timezone);
        ZonedDateTime endDateTime = ZonedDateTime.of(date, endTime, timezone);
        EventBuilder eventBuilder = new EventBuilder()
            .setSubject(data.getSubject())
            .setEndDateTime(endDateTime)
            .setStartDateTime(startDateTime)
            .setSeriesId(seriesId);
        Event event = eventBuilder.build();
        if (list.contains(event)) {
          throw new Exception("Event already exists");
        }
        result.add(event);
      }
      date = date.plusDays(1);
    }
    return result;
  }
}
