package calendar.strategy;

import static calendar.constants.CreateCalendarConstants.parseWeekdays;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Strategy implementation for creating recurring all-day events until a specified end date.
 * This class generates a series of all-day events that occur on specified weekdays,
 * continuing until the specified end date is reached. All-day events are created with
 * default hours of 8:00 AM to 5:00 PM. All events in the series share a common series ID.
 */
public class CreateAllDayRecurringUntil implements Icreate {

  /**
   * Creates a set of recurring all-day events based on the specified parameters.
   * Starting from the given date, iterates through subsequent days and creates events
   * on matching weekdays until the specified end date (inclusive). Each event is assigned
   * the same series ID to indicate they belong to the same recurring series.
   * All-day events are created with default times of 8:00 AM to 5:00 PM in the configured timezone.
   *
   * @param data the data transfer object containing event creation parameters including
   *             subject, start date, weekdays pattern, and end date
   * @param list the existing set of events to check for conflicts before creating new events
   * @return a set containing all newly created event occurrences up to and including the end date
   * @throws Exception if any event in the series conflicts with an existing event,
   *                   or if the dates cannot be parsed
   */
  @Override
  public Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId timezone) throws Exception {
    LocalDate date = LocalDate.parse(data.getOnDate());
    LocalDate untilDate = LocalDate.parse(data.getUntilDate());
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    Set<DayOfWeek> wantedDays = parseWeekdays(data.getWeekdays());
    HashSet<Event> newEvents = new HashSet<>();
    String seriesId = UUID.randomUUID().toString();
    while (!date.isAfter(untilDate)) {
      if (wantedDays.contains(date.getDayOfWeek())) {
        ZonedDateTime eventStart = ZonedDateTime.of(date, startTime, timezone);
        ZonedDateTime eventEnd = ZonedDateTime.of(date, endTime, timezone);
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
      }
      date = date.plusDays(1);
    }
    return newEvents;
  }
}
