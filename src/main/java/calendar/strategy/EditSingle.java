package calendar.strategy;

import static calendar.util.EditEvent.createModifiedEvent;
import static calendar.util.EditEvent.createModifiedEventMulti;
import static calendar.util.EditEvent.findEventBySubjectStartEnd;
import static calendar.util.EditEvent.validateNoDuplicate;

import calendar.dto.EditEventDto;
import calendar.interfacetypes.Iedit;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Implements the "edit event" (single) logic.
 * Modifies only ONE specific event identified by subject, start, and end time.
 * Breaks from series by assigning a new UUID if editing start time.
 */
public class EditSingle implements Iedit {

  @Override
  public void edit(EditEventDto dto, Set<Event> events, ZoneId timezone) throws Exception {
    Event targetEvent = findEventBySubjectStartEnd(dto, events, timezone);
    Set<Event> eventsToRemove = new HashSet<>();
    eventsToRemove.add(targetEvent);
    Set<Event> eventsToCommit = new HashSet<>();
    String newSeriesId = determineNewSeriesId(targetEvent, dto);
    Event newEvent;
    if (dto.hasMultipleProperties()) {
      newEvent = createModifiedEventMulti(
          targetEvent,
          dto.getPropertyChanges(),
          newSeriesId,
          timezone,
          events,
          eventsToRemove,
          eventsToCommit
      );
    } else {
      newEvent = createModifiedEvent(targetEvent, dto, newSeriesId, timezone);
      validateNoDuplicate(newEvent, events, eventsToCommit, eventsToRemove);
    }
    eventsToCommit.add(newEvent);
    events.removeAll(eventsToRemove);
    events.addAll(eventsToCommit);
  }

  /**
   * Determines the new series ID based on whether start time is being modified.
   * If start time is modified, breaks from series with new UUID.
   * Otherwise, maintains existing series ID.
   */
  private String determineNewSeriesId(Event targetEvent, EditEventDto dto) {
    String newSeriesId = "";

    if (targetEvent.getSeriesId() != null && !targetEvent.getSeriesId().isEmpty()) {
      boolean isStartBeingModified = false;
      if (dto.hasMultipleProperties()) {
        isStartBeingModified = dto.getPropertyChanges().containsKey("start");
      } else {
        isStartBeingModified = dto.getProperty() != null
            && dto.getProperty().equalsIgnoreCase("start");
      }
      if (isStartBeingModified) {
        newSeriesId = UUID.randomUUID().toString();
      } else {
        newSeriesId = targetEvent.getSeriesId();
      }
    }
    return newSeriesId;
  }
}