package calendar.strategy;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Strategy implementation for creating a single all-day event.
 * This class generates a one-time all-day event with default hours of 8:00 AM to 5:00 PM
 * on the specified date. The event is created in the configured timezone and checked
 * for conflicts with existing events.
 */
public class CreateAllDaySingle implements Icreate {

  /**
   * Creates a single all-day event based on the specified parameters.
   * The event is created with default times of 8:00 AM to 5:00 PM in the configured timezone.
   * The event does not have a series ID since it is a standalone occurrence.
   *
   * @param data the data transfer object containing event creation parameters including
   *             subject and date
   * @param list the existing set of events to check for conflicts before creating the new event
   * @return a set containing the single newly created event
   * @throws Exception if the event conflicts with an existing event or if the date cannot be parsed
   */
  @Override
  public Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId timezone) throws Exception {
    LocalDate date = LocalDate.parse(data.getOnDate());
    HashSet<Event> result = new HashSet<>();
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    ZonedDateTime zonedStart = ZonedDateTime.of(date, startTime, timezone);
    ZonedDateTime zonedEnd = ZonedDateTime.of(date, endTime, timezone);
    EventBuilder builder = new EventBuilder()
        .setSubject(data.getSubject())
        .setStartDateTime(zonedStart)
        .setEndDateTime(zonedEnd);
    Event event = builder.build();
    if (list.contains(event)) {
      throw new Exception("Event already exists");
    }
    result.add(event);
    return result;
  }
}
