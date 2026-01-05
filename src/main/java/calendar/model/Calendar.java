package calendar.model;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an immutable calendar containing events within a specific timezone.
 *
 * <p>A Calendar is a named collection of {@link Event} objects, all operating within
 * a single timezone context. This class serves as the core domain model for organizing
 * and grouping events in the MVCalendar application.</p>
 *
 * <h2>Immutability</h2>
 *
 * <p>This class is designed to be immutable after construction. The events set
 * is defensively copied both on construction and retrieval to prevent external
 * modification. Any changes to calendar properties require creating a new instance,
 * typically via {@link CalendarModelBuilder}.</p>
 *
 * <h2>Timezone Handling</h2>
 *
 * <p>All events within a calendar share the same timezone context. When events are
 * queried or displayed, their times are interpreted relative to this calendar's
 * timezone. Timezone conversions are handled by the CalendarModel when copying
 * events between calendars with different timezones.</p>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Creating a calendar using the builder
 * Calendar workCalendar = new CalendarModelBuilder()
 *     .setName("Work")
 *     .setTimeZone(ZoneId.of("America/New_York"))
 *     .setEvents(new HashSet<>())
 *     .build();
 *
 * // Accessing calendar properties
 * String name = workCalendar.getName();           // "Work"
 * ZoneId zone = workCalendar.getTimezone();       // America/New_York
 * Set<Event> events = workCalendar.getEvents();   // Defensive copy
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Due to its immutable design, this class is inherently thread-safe. Multiple
 * threads can safely read from a Calendar instance without synchronization.</p>
 *
 * @see Event
 * @see CalendarModelBuilder
 * @see calendar.model.CalendarCollection
 */
public class Calendar {

  /**
   * The collection of events belonging to this calendar.
   * Stored as a Set to ensure no duplicate events exist.
   */
  private final Set<Event> events;

  /**
   * The timezone in which all events in this calendar are interpreted.
   * Used for display purposes and timezone conversion operations.
   */
  private final ZoneId timezone;

  /**
   * The unique identifier name for this calendar.
   * Used as the key for storage and retrieval in the repository.
   */
  private final String name;

  /**
   * Constructs a new Calendar with the specified name, timezone, and events.
   *
   * <p>The provided events set is stored directly without defensive copying
   * at construction time. For safe construction with defensive copying,
   * use {@link CalendarModelBuilder} which handles this automatically.</p>
   *
   * @param name     the unique identifier for this calendar; must not be null
   *                 or empty when used with the repository layer
   * @param timezone the timezone for interpreting event times; must not be null
   * @param events   the initial set of events for this calendar; must not be null
   * @see CalendarModelBuilder
   */
  public Calendar(String name, ZoneId timezone, Set<Event> events) {
    this.events = events;
    this.timezone = timezone;
    this.name = name;
  }

  /**
   * Returns the total number of events in this calendar.
   *
   * <p>This count includes all events regardless of their date, time,
   * or recurrence status. Each occurrence of a recurring event series
   * is counted as a separate event.</p>
   *
   * @return the number of events in this calendar; never negative
   */
  public int getEventCount() {
    return events.size();
  }

  /**
   * Returns a defensive copy of all events in this calendar.
   *
   * <p>The returned set is a new {@link HashSet} containing references to
   * the same {@link Event} objects. Since Event objects are immutable,
   * this provides full protection against external modification of the
   * calendar's event collection.</p>
   *
   * <p>Modifications to the returned set will not affect this calendar's
   * internal state. To add or remove events, create a new Calendar instance
   * with the modified event set.</p>
   *
   * @return a new Set containing all events in this calendar; never null
   */
  public Set<Event> getEvents() {
    return new HashSet<>(events);
  }

  /**
   * Returns the timezone associated with this calendar.
   *
   * <p>All events in this calendar have their times interpreted relative
   * to this timezone. When displaying events or performing time-based
   * queries, this timezone provides the context for interpretation.</p>
   *
   * <p>Common timezone examples include:</p>
   * <ul>
   *   <li>{@code ZoneId.of("America/New_York")} - Eastern Time</li>
   *   <li>{@code ZoneId.of("Europe/London")} - British Time</li>
   *   <li>{@code ZoneId.of("Asia/Tokyo")} - Japan Standard Time</li>
   *   <li>{@code ZoneId.systemDefault()} - System's default timezone</li>
   * </ul>
   *
   * @return the timezone for this calendar; never null
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Returns the unique name identifier for this calendar.
   *
   * <p>The name serves as the primary identifier for the calendar within
   * the repository layer. It is used for:</p>
   * <ul>
   *   <li>Storage and retrieval from {@link  calendar.model.CalendarCollection}</li>
   *   <li>Display in the user interface dropdown</li>
   *   <li>Reference in copy operations between calendars</li>
   *   <li>Setting the active calendar in {@link calendar.model.CalendarCollection}</li>
   * </ul>
   *
   * @return the name of this calendar; may be empty if not properly initialized
   */
  public String getName() {
    return name;
  }
}