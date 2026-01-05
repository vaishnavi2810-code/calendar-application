package calendar.util;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class providing static methods for finding and filtering calendar events.
 * This class contains methods for searching events by various criteria including subject,
 * start time, series membership, date ranges, and specific time instants. All methods
 * are static and the class cannot be instantiated.
 */
public class EventFinder {
  private EventFinder() {
  }

  /**
   * Finds all events matching the specified subject and exact start time.
   * Performs exact matching on both the subject string and the start date-time.
   * This method is useful for identifying specific event occurrences when multiple
   * events may share the same subject but occur at different times.
   *
   * @param subject the exact subject string to match
   * @param start the exact start date-time to match
   * @param events the set of events to search through
   * @return a list of events matching both subject and start time, or an empty list if none match
   */
  public static List<Event> findBySubjectAndStart(String subject, ZonedDateTime start,
                                            Set<Event> events) {
    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(start))
        .collect(Collectors.toList());
  }

  /**
   * Finds all events belonging to a specific recurring event series.
   * Returns all events that share the specified series ID, which identifies
   * occurrences of the same recurring event pattern.
   *
   * @param seriesId the series identifier to match
   * @param events the set of events to search through
   * @return a list of all events in the series, or an empty list if the series ID is
   *         null/empty or no events match
   */
  public static List<Event> findBySeries(String seriesId, Set<Event> events) {
    if (seriesId == null || seriesId.isEmpty()) {
      return List.of();
    }
    return events.stream()
        .filter(e -> seriesId.equals(e.getSeriesId()))
        .collect(Collectors.toList());
  }

  /**
   * Finds events in a series starting from a specific time (inclusive).
   * Returns all events in the specified series that start at or after the given time.
   * This method is useful for edit operations that affect "this and future occurrences."
   *
   * @param seriesId the series identifier to match
   * @param fromTime the earliest start time to include (inclusive)
   * @param events the set of events to search through
   * @return a list of series events starting at or after the specified time, or an empty list
   *         if the series ID is null/empty or no events match
   */
  public static List<Event> findSeriesFrom(String seriesId, ZonedDateTime fromTime,
                                           Set<Event> events) {
    if (seriesId == null || seriesId.isEmpty()) {
      return List.of();
    }
    return events.stream()
        .filter(e -> seriesId.equals(e.getSeriesId()))
        .filter(e -> !e.getStartDateTime().isBefore(fromTime))
        .collect(Collectors.toList());
  }

  /**
   * Finds all events occurring on a specific date.
   * Returns events that have any overlap with the specified date, regardless of when
   * they start or end. Events spanning multiple days are included if the query date
   * falls within their duration. The date is interpreted in the specified timezone.
   *
   * @param date the date to search for events
   * @param timezone the timezone to use for date boundary calculations
   * @param events the set of events to search through
   * @return a list of events occurring on the specified date, or an empty list if none match
   */
  public static List<Event> findOnDate(LocalDate date, ZoneId timezone,
                                       Set<Event> events) {
    ZonedDateTime dayStart = date.atStartOfDay(timezone);
    ZonedDateTime dayEnd = date.plusDays(1).atStartOfDay(timezone);
    return events.stream()
        .filter(e -> eventOverlapsWithRange(e, dayStart, dayEnd))
        .collect(Collectors.toList());
  }

  /**
   * Finds all events occurring within a specified date-time range.
   * Returns events that have any overlap with the time period between start and end.
   * An event overlaps if it starts before the range ends and ends after the range starts.
   *
   * @param start the start of the range (inclusive)
   * @param end the end of the range (exclusive)
   * @param events the set of events to search through
   * @return a list of events overlapping with the specified range, or an empty list if none match
   */
  public static List<Event> findInRange(ZonedDateTime start, ZonedDateTime end,
                                        Set<Event> events) {
    return events.stream()
        .filter(e -> eventOverlapsWithRange(e, start, end))
        .collect(Collectors.toList());
  }

  /**
   * Finds all events that are active at a specific instant in time.
   * An event is considered active if the instant falls within its duration
   * (at or after the start time and before the end time). This method is used
   * for availability checks.
   *
   * @param instant the specific moment in time to check
   * @param events the set of events to search through
   * @return a list of events active at the specified instant, or an empty list if none are active
   */
  public static List<Event> findActiveAt(ZonedDateTime instant, Set<Event> events) {
    return events.stream()
        .filter(e -> eventIsActiveAt(e, instant))
        .collect(Collectors.toList());
  }

  private static boolean eventOverlapsWithRange(Event event, ZonedDateTime rangeStart,
                                                ZonedDateTime rangeEnd) {
    return event.getStartDateTime().isBefore(rangeEnd)
        && event.getEndDateTime().isAfter(rangeStart);
  }

  private static boolean eventIsActiveAt(Event event, ZonedDateTime instant) {
    return !instant.isBefore(event.getStartDateTime())
        && instant.isBefore(event.getEndDateTime());
  }

  /**
   * Finds all events with a specific subject that start on or after a given time.
   * Events are filtered by exact subject match and start time comparison.
   *
   * @param subject the subject to search for (exact match)
   * @param start the earliest start time to include
   * @param events the set of events to search through
   * @return a list of events matching the subject that start on or after the given time
   */
  public static List<Event> findBySubjectFromStart(String subject, ZonedDateTime start,
                                                   Set<Event> events) {
    return events.stream()
            .filter(e -> e.getSubject().equals(subject)
                    && !e.getStartDateTime().isBefore(start))
            .collect(Collectors.toList());
  }
}