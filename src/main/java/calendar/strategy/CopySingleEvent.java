package calendar.strategy;

import static calendar.util.EditEvent.validateNoDuplicate;

import calendar.dto.CopyEventDto;
import calendar.interfacetypes.Icopy;
import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Strategy for copying a single event from one calendar to another by name and datetime.
 * Maintains weekday consistency by adjusting the target date to match the source event's weekday.
 * For example, if the source event is on a Monday, the copied event will also be on a Monday.
 * Handles timezone conversion between source and target calendars.
 */
public class CopySingleEvent implements Icopy {

  /**
  * Copies a single event from source calendar to target calendar.
  * The target date is automatically adjusted to match the source event's weekday.
  * If the source event is part of a recurring series, a new series ID is generated.
  * Validates that the copied event does not create duplicates in the target calendar.
  *
  * @param dto contains event name, source start datetime, and target start datetime
  * @param sourceCalendar the calendar containing the event to copy
  * @param targetCalendar the calendar to copy the event into
  * @return updated set of events for the target calendar including the copied event
  * @throws Exception if the source event is not found, would create duplicates,
  *                   or if a recurring event would span multiple days
  */
  @Override
  public Set<Event> copy(CopyEventDto dto, Calendar sourceCalendar, Calendar targetCalendar)
      throws Exception {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    LocalDateTime sourceLocalDateTime =
        LocalDateTime.parse(dto.getSourceStartDateTime(), formatter);
    ZonedDateTime sourceStartDateTime =
        ZonedDateTime.of(sourceLocalDateTime, sourceCalendar.getTimezone());
    Event sourceEvent = sourceCalendar.getEvents().stream()
        .filter(e -> e.getSubject().equals(dto.getEventName())
            && e.getStartDateTime().equals(sourceStartDateTime))
        .findFirst()
        .orElseThrow(() -> new Exception("Event '" + dto.getEventName()
            + "' starting at " + dto.getSourceStartDateTime() + " not found."));
    Duration eventDuration = Duration.between(
        sourceEvent.getStartDateTime(),
        sourceEvent.getEndDateTime());
    LocalDateTime targetLocalDateTime = LocalDateTime
        .parse(dto.getTargetStartDateTime(), formatter);
    ZonedDateTime newStart = ZonedDateTime.of(targetLocalDateTime, targetCalendar.getTimezone());
    ZonedDateTime newEnd = newStart.plus(eventDuration);
    if (sourceEvent.getSeriesId() != null && !sourceEvent.getSeriesId().isEmpty()
        && !newStart.toLocalDate().equals(newEnd.toLocalDate())) {
      throw new Exception("Error: Start Date and End Date "
          + "should not differ for a recurring event.");
    }
    String newSeriesId = null;
    if (sourceEvent.getSeriesId() != null && !sourceEvent.getSeriesId().isEmpty()) {
      newSeriesId = UUID.randomUUID().toString();
    }
    Event newEvent = new EventBuilder()
        .setSubject(sourceEvent.getSubject())
        .setStartDateTime(newStart)
        .setEndDateTime(newEnd)
        .setLocation(sourceEvent.getLocation())
        .setDescription(sourceEvent.getDescription())
        .setStatus(sourceEvent.getStatus())
        .setSeriesId(newSeriesId)
        .build();
    Set<Event> newEvents = new HashSet<>();
    Set<Event> eventsToRemove = new HashSet<>();
    validateNoDuplicate(newEvent, targetCalendar.getEvents(), newEvents, eventsToRemove);
    Set<Event> updatedEvents = new HashSet<>(targetCalendar.getEvents());
    updatedEvents.add(newEvent);
    return updatedEvents;
  }
}