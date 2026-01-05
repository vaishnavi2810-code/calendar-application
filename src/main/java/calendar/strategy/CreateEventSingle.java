package calendar.strategy;

import static calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Strategy implementation for creating a single timed event.
 * This class generates a one-time event with specific start and end times on a given date.
 * The event is created in the configured timezone and validated to ensure the end time
 * is after the start time and that no conflicts exist with existing events.
 */
public class CreateEventSingle implements Icreate {

  /**
   * Creates a single timed event based on the specified parameters.
   * Parses the start and end date-time strings, validates that the end time is after
   * the start time, and creates the event in the configured timezone. The event does not
   * have a series ID since it is a standalone occurrence.
   *
   * @param data the data transfer object containing event creation parameters including
   *             subject, start date-time, and end date-time
   * @param list the existing set of events to check for conflicts before creating the new event
   * @return a set containing the single newly created event
   * @throws Exception if the end time is before the start time, if the event conflicts
   *                   with an existing event, or if the date-time strings cannot be parsed
   */
  @Override
  public Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId timezone) throws Exception {
    LocalDateTime floatingStart = LocalDateTime.parse(data.getStartDateTime(), DATETIME_FORMATTER);
    LocalDateTime floatingEnd = LocalDateTime.parse(data.getEndDateTime(), DATETIME_FORMATTER);
    if (floatingEnd.isBefore(floatingStart)) {
      throw new Exception("Error: Event end time cannot be before its start time.");
    }
    HashSet<Event> result = new HashSet<>();
    ZonedDateTime eventStart = ZonedDateTime.of(floatingStart, timezone);
    ZonedDateTime eventEnd = ZonedDateTime.of(floatingEnd, timezone);
    Event event = new EventBuilder()
        .setEndDateTime(eventEnd)
        .setStartDateTime(eventStart)
        .setSubject(data.getSubject())
        .build();
    if (list.contains(event)) {
      throw new Exception("Event already exists");
    }
    result.add(event);
    return result;
  }
}
