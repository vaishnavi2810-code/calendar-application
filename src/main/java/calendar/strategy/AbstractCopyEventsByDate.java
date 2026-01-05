package calendar.strategy;

import static calendar.util.EditEvent.validateNoDuplicate;

import calendar.dto.CopyEventDto;
import calendar.interfacetypes.Icopy;
import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract template for copying multiple events based on date criteria.
 * Implements the common algorithm for copying events within a date range,
 * leaving the event-selection and target date calculation to subclasses.
 */
public abstract class AbstractCopyEventsByDate implements Icopy {

  @Override
  public Set<Event> copy(CopyEventDto dto, Calendar sourceCalendar, Calendar targetCalendar)
      throws Exception {
    List<Event> eventsToCopy = getEventsInDateRange(dto, sourceCalendar);
    if (eventsToCopy.isEmpty()) {
      throw new Exception(getNoEventsFoundMessage(dto));
    }
    eventsToCopy = eventsToCopy.stream()
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .collect(Collectors.toList());
    LocalDate firstEventDate = eventsToCopy.get(0).getStartDateTime().toLocalDate();
    LocalDate targetDate = calculateTargetDate(dto, firstEventDate);
    long daysBetween = ChronoUnit.DAYS.between(firstEventDate, targetDate);
    return copyEventsWithOffset(eventsToCopy, daysBetween, targetCalendar);
  }

  /**
   * Copies all events by shifting them by the specified number of days.
   * Maintains series relationships and validates against duplicates.
   */
  private Set<Event> copyEventsWithOffset(List<Event> eventsToCopy,
                                          long daysBetween,
                                          Calendar targetCalendar) throws Exception {
    Set<Event> newEvents = new HashSet<>();
    Set<Event> eventsToRemove = new HashSet<>();
    Map<String, String> seriesIdMapping = new HashMap<>();
    for (Event sourceEvent : eventsToCopy) {
      ZonedDateTime newStart = sourceEvent.getStartDateTime()
          .plusDays(daysBetween)
          .withZoneSameInstant(targetCalendar.getTimezone());
      Duration eventDuration = Duration.between(
          sourceEvent.getStartDateTime(),
          sourceEvent.getEndDateTime());
      ZonedDateTime newEnd = newStart.plus(eventDuration);
      if (sourceEvent.getSeriesId() != null && !sourceEvent.getSeriesId().isEmpty()
          && !newStart.toLocalDate().equals(newEnd.toLocalDate())) {
        throw new Exception("Error: Start Date and End Date should "
            + "not differ for a recurring event.");
      }
      String newSeriesId = null;
      if (sourceEvent.getSeriesId() != null && !sourceEvent.getSeriesId().isEmpty()) {
        if (!seriesIdMapping.containsKey(sourceEvent.getSeriesId())) {
          seriesIdMapping.put(sourceEvent.getSeriesId(), UUID.randomUUID().toString());
        }
        newSeriesId = seriesIdMapping.get(sourceEvent.getSeriesId());
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
      validateNoDuplicate(newEvent, targetCalendar.getEvents(), newEvents, eventsToRemove);
      newEvents.add(newEvent);
    }
    Set<Event> updatedEvents = new HashSet<>(targetCalendar.getEvents());
    updatedEvents.addAll(newEvents);
    return updatedEvents;
  }

  /**
   * Subclasses implement this to specify which events should be copied
   * based on date criteria.
   */
  protected abstract List<Event> getEventsInDateRange(CopyEventDto dto,
                                                      Calendar sourceCalendar)
      throws Exception;

  /**
   * Subclasses implement this to calculate the target date.
   * Some subclasses may adjust for weekday matching, others use the date as-is.
   *
   * @param dto the copy data transfer object
   * @param firstEventDate the date of the first event being copied
   * @return the calculated target date (possibly adjusted)
   */
  protected abstract LocalDate calculateTargetDate(CopyEventDto dto, LocalDate firstEventDate)
      throws Exception;

  /**
   * Subclasses implement this to provide an appropriate error message.
   */
  protected abstract String getNoEventsFoundMessage(CopyEventDto dto);
}