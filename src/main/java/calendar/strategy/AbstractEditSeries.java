package calendar.strategy;

import static calendar.util.EditEvent.createModifiedEvent;
import static calendar.util.EditEvent.createModifiedEventMulti;
import static calendar.util.EditEvent.parseDateTime;
import static calendar.util.EditEvent.validateNoDuplicate;

import calendar.constants.CreateCalendarConstants;
import calendar.dto.EditEventDto;
import calendar.interfacetypes.Iedit;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.util.EventFinder;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract template for "edit" strategies.
 * Implements the common algorithm for editing one or more events,
 * leaving the specific event-selection logic to subclasses.
 */
public abstract class AbstractEditSeries implements Iedit {

  /**
   * This is the "Template Method." It defines the skeleton of the edit algorithm.
   */
  @Override
  public void edit(EditEventDto dto, Set<Event> events, ZoneId timezone) throws Exception {
    ZonedDateTime targetStart = parseDateTime(dto.getTargetStartDateTime(), timezone);
    Set<Event> originalEvents = new HashSet<>(events);
    List<Event> matchingEvents = EventFinder.findBySubjectAndStart(
        dto.getTargetSubject(),
        targetStart,
        originalEvents
    );
    if (matchingEvents.isEmpty()) {
      throw new Exception("Event not found with subject '"
          + dto.getTargetSubject()
          + "' starting at " + targetStart);
    }
    Set<Event> eventsToRemove = new HashSet<>();
    Set<Event> eventsToCommit = new HashSet<>();
    for (Event targetEvent : matchingEvents) {
      String seriesId = targetEvent.getSeriesId();
      if (seriesId == null || seriesId.isEmpty()) {
        eventsToRemove.add(targetEvent);
        Event newEvent;
        if (dto.hasMultipleProperties()) {
          newEvent = createModifiedEventMulti(
              targetEvent,
              dto.getPropertyChanges(),
              "",
              timezone,
              events,
              eventsToRemove,
              eventsToCommit
          );
        } else {
          newEvent = createModifiedEvent(targetEvent, dto, "", timezone);
          validateNoDuplicate(newEvent, events, eventsToCommit, eventsToRemove);
        }

        eventsToCommit.add(newEvent);

      } else {
        List<Event> futureEvents = getEventsToEdit(seriesId, targetStart, events);
        if (dto.hasMultipleProperties()) {
          handleMultiPropertySeriesEdit(
              dto,
              targetEvent,
              futureEvents,
              seriesId,
              timezone,
              events,
              eventsToRemove,
              eventsToCommit
          );
        } else {
          handleSinglePropertySeriesEdit(
              dto,
              targetEvent,
              futureEvents,
              seriesId,
              timezone,
              events,
              eventsToRemove,
              eventsToCommit
          );
        }
      }
    }
    events.removeAll(eventsToRemove);
    events.addAll(eventsToCommit);
  }

  /**
   * Handles editing a series with multiple properties changed.
   */
  private void handleMultiPropertySeriesEdit(EditEventDto dto,
                                             Event targetEvent,
                                             List<Event> futureEvents,
                                             String seriesId,
                                             ZoneId timezone,
                                             Set<Event> allEvents,
                                             Set<Event> eventsToRemove,
                                             Set<Event> eventsToCommit) throws Exception {
    Map<String, String> changes = dto.getPropertyChanges();
    boolean hasStartChange = changes.containsKey("start");
    boolean hasEndChange = changes.containsKey("end");
    if (hasStartChange && hasEndChange) {
      handleBothTimesChanged(
          changes,
          futureEvents,
          seriesId,
          timezone,
          allEvents,
          eventsToRemove,
          eventsToCommit
      );
    } else if (hasStartChange) {
      handleOnlyStartChanged(
          changes,
          targetEvent,
          futureEvents,
          timezone,
          allEvents,
          eventsToRemove,
          eventsToCommit
      );
    } else if (hasEndChange) {
      handleOnlyEndChanged(
          changes,
          targetEvent,
          futureEvents,
          seriesId,
          timezone,
          allEvents,
          eventsToRemove,
          eventsToCommit
      );
    } else {
      handleNonTimeChanges(
          changes,
          futureEvents,
          seriesId,
          timezone,
          allEvents,
          eventsToRemove,
          eventsToCommit
      );
    }
  }

  /**
   * Handles the case where both start and end times are changed.
   * Applies the new times to all events while preserving each event's date.
   */
  private void handleBothTimesChanged(Map<String, String> changes,
                                      List<Event> futureEvents,
                                      String seriesId,
                                      ZoneId timezone,
                                      Set<Event> allEvents,
                                      Set<Event> eventsToRemove,
                                      Set<Event> eventsToCommit) throws Exception {
    futureEvents.sort(Comparator.comparing(Event::getStartDateTime));
    String newSeriesId = UUID.randomUUID().toString();
    ZonedDateTime newStartReference = parseDateTime(changes.get("start"), timezone);
    ZonedDateTime newEndReference = parseDateTime(changes.get("end"), timezone);
    Event firstEvent = futureEvents.get(0);
    Duration startOffset = Duration.between(firstEvent.getStartDateTime(), newStartReference);
    Duration endOffset = Duration.between(firstEvent.getEndDateTime(), newEndReference);
    eventsToRemove.addAll(futureEvents);
    for (Event oldEvent : futureEvents) {
      ZonedDateTime adjustedStart = oldEvent.getStartDateTime().plus(startOffset);
      ZonedDateTime adjustedEnd = oldEvent.getEndDateTime().plus(endOffset);
      if (!adjustedStart.isBefore(adjustedEnd)) {
        throw new Exception("Invalid update: New start time must be before end time");
      }
      if (!adjustedStart.toLocalDate().equals(adjustedEnd.toLocalDate())) {
        throw new Exception("Invalid update: Events in a series must stay on the same day");
      }

      Map<String, String> eventChanges = new HashMap<>(changes);
      eventChanges.put("start", adjustedStart.format(
          CreateCalendarConstants.DATETIME_FORMATTER));
      eventChanges.put("end", adjustedEnd.format(
          CreateCalendarConstants.DATETIME_FORMATTER));

      Event newEvent = createModifiedEventMulti(
          oldEvent,
          eventChanges,
          newSeriesId,
          timezone,
          allEvents,
          eventsToRemove,
          eventsToCommit
      );

      eventsToCommit.add(newEvent);
    }
  }

  /**
   * Handles the case where only start time is changed.
   * Calculates offset and applies to both start and end times.
   */
  private void handleOnlyStartChanged(Map<String, String> changes,
                                      Event targetEvent,
                                      List<Event> futureEvents,
                                      ZoneId timezone,
                                      Set<Event> allEvents,
                                      Set<Event> eventsToRemove,
                                      Set<Event> eventsToCommit) throws Exception {
    futureEvents.sort(Comparator.comparing(Event::getStartDateTime));
    String newSeriesId = UUID.randomUUID().toString();
    ZonedDateTime newTargetStart = parseDateTime(changes.get("start"), timezone);
    Event firstEvent = futureEvents.get(0);
    Duration offset = Duration.between(firstEvent.getStartDateTime(), newTargetStart);
    eventsToRemove.addAll(futureEvents);
    int eventIndex = 0;
    for (Event oldEvent : futureEvents) {
      eventIndex++;
      try {
        ZonedDateTime adjustedStart = oldEvent.getStartDateTime().plus(offset);
        ZonedDateTime adjustedEnd = oldEvent.getEndDateTime().plus(offset);
        Map<String, String> eventChanges = new HashMap<>(changes);
        eventChanges.put("start", adjustedStart.format(
            calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER));
        eventChanges.put("end", adjustedEnd.format(
            calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER));
        Event newEvent = createModifiedEventMulti(
            oldEvent,
            eventChanges,
            newSeriesId,
            timezone,
            allEvents,
            eventsToRemove,
            eventsToCommit
        );
        eventsToCommit.add(newEvent);
      } catch (Exception e) {
        throw new Exception("Failed to update event #" + eventIndex
            + " in series (starting at "
            + oldEvent.getStartDateTime() + "): "
            + e.getMessage());
      }
    }
  }

  /**
   * Handles the case where only end time is changed.
   * Calculates offset and applies only to end time, keeps start same.
   */
  private void handleOnlyEndChanged(Map<String, String> changes,
                                    Event targetEvent,
                                    List<Event> futureEvents,
                                    String seriesId,
                                    ZoneId timezone,
                                    Set<Event> allEvents,
                                    Set<Event> eventsToRemove,
                                    Set<Event> eventsToCommit) throws Exception {
    ZonedDateTime newTargetEnd = parseDateTime(changes.get("end"), timezone);
    Duration offset = Duration.between(targetEvent.getEndDateTime(), newTargetEnd);
    eventsToRemove.addAll(futureEvents);
    int eventIndex = 0;
    for (Event oldEvent : futureEvents) {
      eventIndex++;
      try {
        ZonedDateTime adjustedEnd = oldEvent.getEndDateTime().plus(offset);
        Map<String, String> eventChanges = new HashMap<>(changes);
        eventChanges.put("end", adjustedEnd.format(
            calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER));
        Event newEvent = createModifiedEventMulti(
            oldEvent,
            eventChanges,
            seriesId,
            timezone,
            allEvents,
            eventsToRemove,
            eventsToCommit
        );
        eventsToCommit.add(newEvent);
      } catch (Exception e) {
        throw new Exception("Failed to update event #" + eventIndex
            + " in series (starting at "
            + oldEvent.getStartDateTime() + "): "
            + e.getMessage());
      }
    }
  }

  /**
   * Handles the case where no time properties are changed.
   * Just updates the specified properties on all events.
   */
  private void handleNonTimeChanges(Map<String, String> changes,
                                    List<Event> futureEvents,
                                    String seriesId,
                                    ZoneId timezone,
                                    Set<Event> allEvents,
                                    Set<Event> eventsToRemove,
                                    Set<Event> eventsToCommit) throws Exception {
    eventsToRemove.addAll(futureEvents);
    int eventIndex = 0;
    for (Event oldEvent : futureEvents) {
      eventIndex++;
      try {
        Event newEvent = createModifiedEventMulti(
            oldEvent,
            changes,
            seriesId,
            timezone,
            allEvents,
            eventsToRemove,
            eventsToCommit
        );
        eventsToCommit.add(newEvent);
      } catch (Exception e) {
        throw new Exception("Failed to update event #" + eventIndex
            + " in series (starting at "
            + oldEvent.getStartDateTime() + "): "
            + e.getMessage());
      }
    }
  }

  /**
   * Handles editing a series with single property changed (existing logic).
   */
  private void handleSinglePropertySeriesEdit(EditEventDto dto,
                                              Event targetEvent,
                                              List<Event> futureEvents,
                                              String seriesId,
                                              ZoneId timezone,
                                              Set<Event> allEvents,
                                              Set<Event> eventsToRemove,
                                              Set<Event> eventsToCommit) throws Exception {
    if (dto.getProperty().equalsIgnoreCase("start")
        || dto.getProperty().equalsIgnoreCase("end")) {
      String newSeriesId = seriesId;
      if (dto.getProperty().equalsIgnoreCase("start")) {
        newSeriesId = UUID.randomUUID().toString();
      }
      Duration offset;
      if (dto.getProperty().equalsIgnoreCase("start")) {
        ZonedDateTime newTargetStart = parseDateTime(dto.getNewValue(), timezone);
        offset = Duration.between(targetEvent.getStartDateTime(), newTargetStart);
      } else {
        ZonedDateTime newTargetEnd = parseDateTime(dto.getNewValue(), timezone);
        offset = Duration.between(targetEvent.getEndDateTime(), newTargetEnd);
      }
      eventsToRemove.addAll(futureEvents);
      for (Event oldEvent : futureEvents) {
        ZonedDateTime adjustedStart;
        ZonedDateTime adjustedEnd;
        if (dto.getProperty().equalsIgnoreCase("start")) {
          adjustedStart = oldEvent.getStartDateTime().plus(offset);
        } else {
          adjustedStart = oldEvent.getStartDateTime();
        }
        adjustedEnd = oldEvent.getEndDateTime().plus(offset);

        if (!adjustedStart.isBefore(adjustedEnd)) {
          throw new Exception("Invalid update: New start time must be before end time");
        }
        if (!adjustedStart.toLocalDate().equals(adjustedEnd.toLocalDate())) {
          throw new Exception("Invalid update: Events in a "
              + "series must start and end on the same day. "
              + "Event would span from " + adjustedStart.toLocalDate()
              + " to " + adjustedEnd.toLocalDate());
        }
        Event newEvent = new EventBuilder()
            .setSubject(oldEvent.getSubject())
            .setStartDateTime(adjustedStart)
            .setEndDateTime(adjustedEnd)
            .setSeriesId(newSeriesId)
            .setDescription(oldEvent.getDescription())
            .setLocation(oldEvent.getLocation())
            .setStatus(oldEvent.getStatus())
            .build();
        validateNoDuplicate(newEvent, allEvents, eventsToCommit, eventsToRemove);
        eventsToCommit.add(newEvent);
      }
    } else {
      eventsToRemove.addAll(futureEvents);
      for (Event oldEvent : futureEvents) {
        Event newEvent = createModifiedEvent(oldEvent, dto, seriesId, timezone);
        validateNoDuplicate(newEvent, allEvents, eventsToCommit, eventsToRemove);
        eventsToCommit.add(newEvent);
      }
    }
  }


  /**
   * Provides the specific implementation for which events in a series to edit.
   *
   * @param seriesId    The ID of the series to find.
   * @param targetStart The start time of the event that was targeted (for "forward" logic).
   * @param allEvents   The set of all events.
   * @return A List of events that should be modified by this strategy.
   */
  protected abstract List<Event> getEventsToEdit(String seriesId,
                                                 ZonedDateTime targetStart,
                                                 Set<Event> allEvents);

}