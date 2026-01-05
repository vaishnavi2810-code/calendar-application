package calendar.model;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder class for creating CalendarModel objects using the Builder pattern.
 * Provides a fluent interface for constructing calendars with various properties.
 */
public class CalendarModelBuilder {
  private String name;
  private ZoneId timeZone;
  private Set<Event> events;

  /**
  * Constructs a new CalendarModelBuilder with default values.
  * Default timezone is system default, empty event set, and empty name.
  */
  public CalendarModelBuilder() {
    this.name = "";
    this.timeZone = ZoneId.systemDefault();
    this.events = new HashSet<>();
  }

  /**
  * Sets the name of the calendar.
  *
  * @param name the calendar name (e.g., "work", "personal")
  * @return this CalendarModelBuilder instance for method chaining
  */
  public CalendarModelBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
  * Sets the timezone for the calendar.
  *
  * @param timeZone the timezone for this calendar
  * @return this CalendarModelBuilder instance for method chaining
  */
  public CalendarModelBuilder setTimeZone(ZoneId timeZone) {
    this.timeZone = timeZone;
    return this;
  }

  /**
  * Sets the initial events for the calendar.
  * Note: This replaces any previously set events in the builder.
  *
  * @param events the set of events to initialize the calendar with
  * @return this CalendarModelBuilder instance for method chaining
  */
  public CalendarModelBuilder setEvents(Set<Event> events) {
    this.events = new HashSet<>(events);
    return this;
  }

  /**
  * Constructs and returns the CalendarModel with all configured properties.
  *
  * @return a new CalendarModel with the specified properties
  */
  public Calendar build() {
    return new Calendar(this.name, this.timeZone, this.events);
  }
}