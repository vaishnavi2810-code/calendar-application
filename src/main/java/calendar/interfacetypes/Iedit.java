package calendar.interfacetypes;

import calendar.dto.EditEventDto;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.Set;

/**
 * Strategy interface for editing calendar events.
 * Implementations define different edit scopes, such as editing a single occurrence,
 * all forward occurrences, or the entire event series.
 */
public interface Iedit {

  /**
   * Edits one or more events based on the edit parameters and scope specified in the DTO.
   * The edit operation modifies the specified property (subject, start time, end time, location)
   * for the target events according to the edit type (single, forward, or series).
   *
   * @param dto the data transfer object containing edit parameters, target event identification,
   *            and the new value
   * @param allEvents the complete set of calendar events, modified in place by this operation
   * @throws Exception if the edit fails due to invalid parameters, event not found,
   *                   time conflicts, or other validation errors
   */
  void edit(EditEventDto dto, Set<Event> allEvents, ZoneId timezone) throws Exception;
}