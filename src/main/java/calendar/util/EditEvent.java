package calendar.util;

import static calendar.constants.CreateCalendarConstants.DATETIME_FORMATTER;

import calendar.dto.EditEventDto;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for editing events.
 */
public class EditEvent {

  private EditEvent() {

  }

  /**
   * Creates a modified version of an event with one property changed.
   *
   * @param original The original event
   * @param dto The edit data transfer object containing the property to change
   * @param newSeriesId The new series ID (empty string if not in a series)
   * @return The newly created modified event
   * @throws Exception if validation fails
   */
  public static Event createModifiedEvent(Event original, EditEventDto dto,
                                          String newSeriesId, ZoneId timezone) throws Exception {
    EventBuilder builder = new EventBuilder()
            .setSubject(original.getSubject())
            .setStartDateTime(original.getStartDateTime())
            .setEndDateTime(original.getEndDateTime())
            .setSeriesId(newSeriesId)
            .setDescription(original.getDescription())
            .setLocation(original.getLocation())
            .setStatus(original.getStatus());

    switch (dto.getProperty().toLowerCase()) {
      case "subject":
        builder.setSubject(dto.getNewValue());
        break;

      case "start":
        ZonedDateTime newStart = parseDateTime(dto.getNewValue(), timezone);
        if (!newStart.isBefore(original.getEndDateTime())) {
          throw new Exception("Invalid update: New start time ("
                  + newStart
                  + ") must be before end time (" + original.getEndDateTime() + ")");
        }
        if (newSeriesId != null && !newSeriesId.isEmpty()) {
          if (!newStart.toLocalDate().equals(original.getEndDateTime().toLocalDate())) {
            throw new Exception("Invalid update: Events in a "
                    +
                    "series must start and end on the same day. "
                    + "Event would span from " + newStart.toLocalDate()
                    + " to " + original.getEndDateTime().toLocalDate());
          }
        }

        builder.setStartDateTime(newStart);
        break;

      case "end":
        ZonedDateTime newEnd = parseDateTime(dto.getNewValue(), timezone);

        if (!newEnd.isAfter(original.getStartDateTime())) {
          throw new Exception("Invalid update: New end time ("
                  + newEnd
                  + ") must be after start time (" + original.getStartDateTime() + ")");
        }

        if (newSeriesId != null && !newSeriesId.isEmpty()) {
          if (!original.getStartDateTime().toLocalDate().equals(newEnd.toLocalDate())) {
            throw new Exception("Invalid update: Events in a series "
                    +
                    "must start and end on the same day. "
                    + "Event would span from " + original.getStartDateTime().toLocalDate()
                    + " to " + newEnd.toLocalDate());
          }
        }

        builder.setEndDateTime(newEnd);
        break;

      case "description":
        builder.setDescription(dto.getNewValue());
        break;

      case "location":
        builder.setLocation(dto.getNewValue());
        break;

      case "status":
        builder.setStatus(dto.getNewValue());
        break;

      default:
        throw new Exception("Unknown property: " + dto.getProperty());
    }

    return builder.build();
  }

  /**
   * Creates a modified version of an event with multiple properties changed.
   * Validates all proposed changes BEFORE building the event.
   * Used for multi-property edits (GUI-based interface).
   *
   * @param original The original event
   * @param changes Map of property names to new values
   * @param newSeriesId The new series ID (empty string if not in a series)
   * @param timezone The timezone for parsing date-times
   * @param existingEvents All events in the calendar (for duplicate checking)
   * @param eventsToRemove Events being removed in the same operation
   * @param eventsToCommit Events already validated in the same operation
   * @return The newly created modified event
   * @throws Exception if validation fails
   */
  public static Event createModifiedEventMulti(Event original,
                                               Map<String, String> changes,
                                               String newSeriesId,
                                               ZoneId timezone,
                                               Set<Event> existingEvents,
                                               Set<Event> eventsToRemove,
                                               Set<Event> eventsToCommit) throws Exception {
    Map<String, Object> proposed = parseProposedChanges(original, changes, timezone);
    validateProposedValues(proposed, newSeriesId, changes);
    validateNoDuplicateProposed(
        (String) proposed.get("subject"),
        (ZonedDateTime) proposed.get("start"),
        (ZonedDateTime) proposed.get("end"),
        existingEvents,
        eventsToRemove,
        eventsToCommit
    );
    return buildEventFromProposed(proposed, newSeriesId);
  }

  /**
   * Parses proposed changes and combines with original values.
   * Extracts all proposed property values into a map.
   */
  private static Map<String, Object> parseProposedChanges(Event original,
                                                          Map<String, String> changes,
                                                          ZoneId timezone) throws Exception {
    Map<String, Object> proposed = new HashMap<>();
    proposed.put("subject", original.getSubject());
    proposed.put("start", original.getStartDateTime());
    proposed.put("end", original.getEndDateTime());
    proposed.put("description", original.getDescription());
    proposed.put("location", original.getLocation());
    proposed.put("status", original.getStatus());
    for (Map.Entry<String, String> change : changes.entrySet()) {
      String property = change.getKey().toLowerCase();
      String newValue = change.getValue();

      switch (property) {
        case "subject":
          proposed.put("subject", newValue);
          break;
        case "start":
          proposed.put("start", parseDateTime(newValue, timezone));
          break;
        case "end":
          proposed.put("end", parseDateTime(newValue, timezone));
          break;
        case "description":
          proposed.put("description", newValue);
          break;
        case "location":
          proposed.put("location", newValue);
          break;
        case "status":
          proposed.put("status", newValue);
          break;
        default:
          throw new Exception("Unknown property: " + property);
      }
    }

    return proposed;
  }

  /**
   * Validates internal consistency of proposed values.
   * Checks that proposed values satisfy all business rules.
   */
  private static void validateProposedValues(Map<String, Object> proposed,
                                             String seriesId,
                                             Map<String, String> changes) throws Exception {
    List<String> errors = new ArrayList<>();
    ZonedDateTime proposedStart = (ZonedDateTime) proposed.get("start");
    ZonedDateTime proposedEnd = (ZonedDateTime) proposed.get("end");
    if (!proposedStart.isBefore(proposedEnd)) {
      StringBuilder error = new StringBuilder();
      error.append("Invalid update: Start time (")
          .append(proposedStart)
          .append(") must be before end time (")
          .append(proposedEnd)
          .append(")");

      if (changes.containsKey("start") && changes.containsKey("end")) {
        error.append("\n  → Both start and end times were modified");
      } else if (changes.containsKey("start")) {
        error.append("\n  → Start time was modified");
      } else if (changes.containsKey("end")) {
        error.append("\n  → End time was modified");
      }

      errors.add(error.toString());
    }

    if (seriesId != null && !seriesId.isEmpty()) {
      if (!proposedStart.toLocalDate().equals(proposedEnd.toLocalDate())) {
        errors.add("Invalid update: Events in a series must start and end on the same day."
            + "\n  → Event would span from "
            + proposedStart.toLocalDate()
            + " to "
            + proposedEnd.toLocalDate());
      }
    }

    if (!errors.isEmpty()) {
      throw new Exception(String.join("\n\n", errors));
    }
  }

  /**
   * Validates that proposed event values don't create duplicates.
   * Checks against existing events and other events being created in the same operation.
   */
  private static void validateNoDuplicateProposed(String proposedSubject,
                                                  ZonedDateTime proposedStart,
                                                  ZonedDateTime proposedEnd,
                                                  Set<Event> existingEvents,
                                                  Set<Event> eventsToRemove,
                                                  Set<Event> eventsToCommit) throws Exception {

    for (Event existing : existingEvents) {
      if (eventsToRemove.contains(existing)) {
        continue;
      }

      if (existing.getSubject().equals(proposedSubject)
          && existing.getStartDateTime().equals(proposedStart)
          && existing.getEndDateTime().equals(proposedEnd)) {
        throw new Exception("Edit operation failed: An event with subject '"
            + proposedSubject + "' from "
            + proposedStart + " to "
            + proposedEnd + " already exists.");
      }
    }

    for (Event newEvent : eventsToCommit) {
      if (newEvent.getSubject().equals(proposedSubject)
          && newEvent.getStartDateTime().equals(proposedStart)
          && newEvent.getEndDateTime().equals(proposedEnd)) {
        throw new Exception("Edit operation failed: Would create duplicate events with subject '"
            + proposedSubject
            + "' from "
            + proposedStart
            + " to "
            + proposedEnd);
      }
    }
  }

  /**
   * Builds event from validated proposed values.
   * This should never fail because all validation is complete.
   */
  private static Event buildEventFromProposed(Map<String, Object> proposed,
                                              String newSeriesId) throws Exception {
    return new EventBuilder()
        .setSubject((String) proposed.get("subject"))
        .setStartDateTime((ZonedDateTime) proposed.get("start"))
        .setEndDateTime((ZonedDateTime) proposed.get("end"))
        .setSeriesId(newSeriesId)
        .setDescription((String) proposed.get("description"))
        .setLocation((String) proposed.get("location"))
        .setStatus((String) proposed.get("status"))
        .build();
  }


  /**
   * Parses a date-time string into a ZonedDateTime.
   *
   * @param dateTimeStr The date-time string in format "yyyy-MM-ddTHH:mm"
   * @return The parsed ZonedDateTime
   */
  public static ZonedDateTime parseDateTime(String dateTimeStr, ZoneId timezone) throws Exception {
    LocalDateTime floating = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    return ZonedDateTime.of(floating, timezone);
  }

  /**
   * Finds an event by subject, start time, AND end time for precise identification.
   *
   * @param dto The edit DTO containing the target event identifiers
   * @param events The set of events to search
   * @return The found event
   * @throws Exception if event is not found
   */
  public static Event findEventBySubjectStartEnd(EditEventDto dto, Set<Event> events,
                                                 ZoneId timezone) throws Exception {
    ZonedDateTime targetStart = parseDateTime(dto.getTargetStartDateTime(), timezone);
    ZonedDateTime targetEnd = parseDateTime(dto.getTargetEndDateTime(), timezone);

    for (Event event : events) {
      if (event.getSubject().equals(dto.getTargetSubject())
              && event.getStartDateTime().equals(targetStart)
              && event.getEndDateTime().equals(targetEnd)) {
        return event;
      }
    }

    throw new Exception("Event not found with subject '"
            + dto.getTargetSubject()
            + "' from " + targetStart
            + " to " + targetEnd);
  }

  /**
   * Validates that a new event doesn't create duplicates.
   * Checks if an event with same subject, start, and end already exists.
   *
   * @param newEvent The new event to validate
   * @param existingEvents All existing events
   * @param newEvents Events being created in the same operation
   * @param eventsToRemove Events being removed in the same operation
   * @throws Exception if duplicate would be created
   */
  public static void validateNoDuplicate(Event newEvent, Set<Event> existingEvents,
                                         Set<Event> newEvents,
                                         Set<Event> eventsToRemove) throws Exception {

    Set<Event> remainingEvents = existingEvents.stream()
            .filter(e -> !eventsToRemove.contains(e))
            .collect(Collectors.toSet());

    if (remainingEvents.contains(newEvent)) {
      throw new Exception("Edit operation failed: An event with subject '"
              + newEvent.getSubject() + "' from "
              + newEvent.getStartDateTime() + " to "
              + newEvent.getEndDateTime() + " already exists.");
    }

    if (newEvents.contains(newEvent)) {
      throw new Exception("Edit operation failed: Would create duplicate events with subject '"
              + newEvent.getSubject()
              + "' from "
              + newEvent.getStartDateTime()
              + " to "
              + newEvent.getEndDateTime());
    }
  }

  /**
   * Extracts the set of weekdays on which events in the series occur.
   */
  public static Set<DayOfWeek> extractValidWeekdays(List<Event> seriesEvents) {
    Set<DayOfWeek> validWeekdays = new HashSet<>();
    for (Event event : seriesEvents) {
      DayOfWeek weekday = event.getStartDateTime().getDayOfWeek();
      validWeekdays.add(weekday);
    }
    return validWeekdays;
  }

  /**
   * Finds the next date on or after fromDate that falls on a valid weekday.
   */
  public static LocalDate findNextValidWeekday(LocalDate fromDate, Set<DayOfWeek> validWeekdays) {
    LocalDate candidate = fromDate;
    for (int i = 0; i < 7; i++) {
      if (validWeekdays.contains(candidate.getDayOfWeek())) {
        return candidate;
      }
      candidate = candidate.plusDays(1);
    }
    throw new IllegalStateException("No valid weekday found");
  }
}