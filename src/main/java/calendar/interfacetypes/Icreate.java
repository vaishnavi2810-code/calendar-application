package calendar.interfacetypes;

import calendar.dto.CreateEventDto;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.Set;

/**
 * Strategy interface for creating calendar events.
 * Implementations define different approaches to event creation, such as creating
 * single events, recurring events with fixed repetitions, or recurring events with end dates.
 */
public interface Icreate {

  /**
   * Creates one or more events based on the provided creation parameters.
   * For single events, returns a set with one event. For recurring events,
   * generates and returns all occurrences according to the recurrence rules.
   *
   * @param data the data transfer object containing all event creation parameters
   * @param list the existing set of events to check for conflicts and add new events to
   * @return a set of all newly created event instances
   * @throws Exception if event creation fails due to invalid parameters, time conflicts,
   *                   or other validation errors
   */
  Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId timezone) throws Exception;
}