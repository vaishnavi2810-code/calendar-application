package calendar.model;

import java.time.ZonedDateTime;

/**
 * This is a builder class for creating Event objects using the Builder pattern.
 * Provides an interface for constructing events with various properties.
 * All fields are initialized with default values and can be set using setter methods.
 */
public class EventBuilder {

  private String subject;
  private ZonedDateTime startDateTime;
  private ZonedDateTime endDateTime;
  private String seriesId;
  private String description;
  private String location;
  private String status;

  /**
   * Constructs a new EventBuilder with default values.
   * Default values are empty strings for text fields and current time for date/time fields.
   */
  public EventBuilder() {
    this.subject = "";
    this.startDateTime = ZonedDateTime.now();
    this.endDateTime = ZonedDateTime.now();
    this.seriesId = "";
    this.description = "";
    this.location = "";
    this.status = "";
  }


  /**
   * Sets the subject (title) of the event.
   *
   * @param subject the event subject or title
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the event start date and time with timezone
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setStartDateTime(ZonedDateTime startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  /**
   * Sets the end date and time of the event.
   *
   * @param endDateTime the event end date and time with timezone
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setEndDateTime(ZonedDateTime endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  /**
   * Sets the series identifier for recurring events.
   * Events with the same seriesId belong to the same recurring series.
   *
   * @param seriesId the unique identifier for the event series, or empty string for single events
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setSeriesId(String seriesId) {
    this.seriesId = seriesId;
    return this;
  }

  /**
   * Sets the description providing additional details about the event.
   *
   * @param description the event description or notes
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setDescription(String description) {
    this.description = description;
    return this;
  }


  /**
   * Sets the physical or virtual location where the event takes place.
   *
   * @param location the event location (e.g., "Conference Room A", "Zoom")
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setLocation(String location) {
    this.location = location;
    return this;
  }

  /**
   * Sets the status of the event.
   *
   * @param status the event status (e.g., "confirmed", "pending", "cancelled")
   * @return this EventBuilder instance for method chaining
   */
  public EventBuilder setStatus(String status) {
    this.status = status;
    return this;
  }

  /**
   * Constructs and returns the Event object with all configured properties.
   * This is the final step in the builder pattern.
   *
   * @return a new Event object with the specified properties
   */
  public Event build() {
    return new Event(this.subject, this.startDateTime, this.endDateTime,
        this.seriesId, this.description, this.location, this.status);
  }
}