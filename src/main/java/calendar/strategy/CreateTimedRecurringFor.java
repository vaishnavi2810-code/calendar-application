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
 * Strategy implementation for creating recurring timed events with a fixed number of occurrences.
 * This class generates a series of events that occur at the same time on specified weekdays
 * for a defined number of repetitions. All events in the series share a common series ID
 * for identification purposes. The start and end times must be on the same date.
 */
public class CreateTimedRecurringFor implements Icreate {

  /**
   * Creates a set of recurring timed events based on the specified parameters.
   * Starting from the given date, iterates through subsequent days and creates events
   * on matching weekdays until the specified number of occurrences is reached.
   * Each event occurs at the same time of day and is assigned the same series ID
   * to indicate they belong to the same recurring series. Validates that the end time
   * is after the start time and that both times occur on the same date.
   *
   * @param data the data transfer object containing event creation parameters including
   *        subject, start date-time, end date-time, weekdays pattern, and number of occurrences
   * @param list the existing set of events to check for conflicts before creating new events
   * @return a set containing all newly created event occurrences
   * @throws Exception if the end time is before the start time, if start and end are not
   *         on the same date, if any event in the series conflicts with an existing event,
   *         or if the number of occurrences cannot be parsed
   */
  @Override
  public Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId timezone) throws Exception {
    LocalDateTime floatingStart = LocalDateTime.parse(data.getStartDateTime(), DATETIME_FORMATTER);
    LocalDateTime floatingEnd = LocalDateTime.parse(data.getEndDateTime(), DATETIME_FORMATTER);
    if (floatingEnd.isBefore(floatingStart)) {
      throw new Exception("Error: Event end time cannot be before its start time.");
    }
    LocalDate currentDate = floatingStart.toLocalDate();
    LocalDate endDate = floatingEnd.toLocalDate();
    if (!currentDate.equals(endDate)) {
      throw new Exception("Date should be the same");
    }
    Set<DayOfWeek> wantedDays = parseWeekdays(data.getWeekdays());
    int n = Integer.parseInt(data.getnTimes());
    LocalTime startTime = floatingStart.toLocalTime();
    LocalTime endTime = floatingEnd.toLocalTime();
    String seriesId = UUID.randomUUID().toString();
    int eventsCreated = 0;
    HashSet<Event> newEvents = new HashSet<>();
    while (eventsCreated < n) {
      if (wantedDays.contains(currentDate.getDayOfWeek())) {
        ZonedDateTime eventStart = ZonedDateTime.of(currentDate, startTime, timezone);
        ZonedDateTime eventEnd = ZonedDateTime.of(currentDate, endTime, timezone);
        Event event = new EventBuilder()
            .setSubject(data.getSubject())
            .setStartDateTime(eventStart)
            .setEndDateTime(eventEnd)
            .setSeriesId(seriesId)
            .build();
        if (list.contains(event)) {
          throw new Exception("Event already exists");
        }
        newEvents.add(event);
        eventsCreated++;
      }
      currentDate = currentDate.plusDays(1);
    }
    return newEvents;
  }

}
