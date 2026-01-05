package calendar.model;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Represents a calendar event with a subject, time range, and optional properties.
 * Events are immutable once created. Two events are considered equal if they have
 * the same subject, start time, and end time, regardless of other properties.
 * Events can be standalone or part of a recurring series identified by seriesId.
 */
public class Event {
  private final String subject;
  private final ZonedDateTime startDateTime;
  private final ZonedDateTime endDateTime;
  private final String seriesId;
  private final String description;
  private final String location;
  private final String status;

  /**
   * Constructs a new Event with the specified properties.
   * All fields are immutable after construction.
   *
   * @param subject the event title or name
   * @param startDateTime the event start date and time with timezone
   * @param endDateTime the event end date and time with timezone
   * @param seriesId the unique identifier for recurring series, or empty for standalone events
   * @param description additional details or notes about the event
   * @param location the physical or virtual location of the event
   * @param status the current status of the event (e.g., "confirmed", "pending")
   */
  public Event(String subject, ZonedDateTime startDateTime, ZonedDateTime endDateTime,
               String seriesId, String description, String location, String status) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.seriesId = seriesId;
    this.description = description;
    this.location = location;
    this.status = status;
  }

  public String getSubject() {
    return subject;
  }

  public ZonedDateTime getStartDateTime() {
    return startDateTime;
  }

  public ZonedDateTime getEndDateTime() {
    return endDateTime;
  }

  public String getSeriesId() {
    return seriesId;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Event other = (Event) obj;
    return Objects.equals(this.subject, other.subject)
            &&
           Objects.equals(this.startDateTime, other.startDateTime)
            &&
           Objects.equals(this.endDateTime, other.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return "Event{"
            +
           "subject='" + subject + '\''
            +
           "startDateTime=" + startDateTime
            +
           "endDateTime=" + endDateTime
            +
           "seriesId='" + seriesId + '\''
            +
           ", description='" + description + '\''
            +
           ", location='" + location + '\''
            +
           ", status='" + status + '\''
            +
           '}';
  }
}
