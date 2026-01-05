package calendar.dto;

import java.util.regex.Matcher;

/**
 * A DTO for carrying data for calendar management commands.
 * It's flexible and can hold data for creating, editing, using,
 * and copying calendars and events.
 */
public class CalendarDto {

  /**
   * Enum for all calendar-level (non-event-level) commands.
   */
  public enum CalendarCommandType {
    CREATE_CALENDAR,
    EDIT_CALENDAR,
    USE_CALENDAR,
  }

  private final CalendarCommandType type;
  private final String calendarName;
  private final String timeZone;
  private final String propertyName;
  private final String propertyValue;
  private final String targetCalendarName;
  private final String sourceEventName;
  private final String sourceStartTime;
  private final String targetStartTime;
  private final String sourceDate;
  private final String sourceStartDate;
  private final String sourceEndDate;
  private final String targetDate;

  /**
   * Creates a CalendarDto by extracting relevant fields from the matcher
   * based on the command type.
   *
   * @param type The type of calendar command being processed.
   * @param matcher The regex matcher containing the parsed command data.
   */
  public CalendarDto(CalendarCommandType type, Matcher matcher) {
    this.type = type;


    this.calendarName = getGroup(matcher, "name");
    this.timeZone = getGroup(matcher, "timezone");
    this.propertyName = getGroup(matcher, "property");
    this.propertyValue = getGroup(matcher, "value");

    this.targetCalendarName = getGroup(matcher, "target");
    this.sourceEventName = getGroup(matcher, "eventName");
    this.sourceStartTime = getGroup(matcher, "sourceTime");
    this.targetStartTime = getGroup(matcher, "targetTime");
    this.sourceDate = getGroup(matcher, "sourceDate");
    this.sourceStartDate = getGroup(matcher, "sourceStartDate");
    this.sourceEndDate = getGroup(matcher, "sourceEndDate");
    this.targetDate = getGroup(matcher, "targetDate");
  }

  /**
   * Creates a CalendarDto directly from parameters without using a Matcher.
   * Used for GUI mode where data comes from form fields rather than text parsing.
   *
   * @param type The type of calendar command being processed.
   * @param calendarName The name of the calendar.
   * @param timeZone The timezone of the calendar.
   * @param propertyName The property name for edit operations.
   * @param propertyValue The property value for edit operations.
   */
  private CalendarDto(CalendarCommandType type, String calendarName,
                      String timeZone, String propertyName, String propertyValue) {
    this.type = type;
    this.calendarName = calendarName;
    this.timeZone = timeZone;
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
    this.targetCalendarName = null;
    this.sourceEventName = null;
    this.sourceStartTime = null;
    this.targetStartTime = null;
    this.sourceDate = null;
    this.sourceStartDate = null;
    this.sourceEndDate = null;
    this.targetDate = null;
  }

  /**
   * Safely extracts a named group from the matcher.
   * Replicates the "un-quoting" logic from your other DTOs.
   */
  private String getGroup(Matcher matcher, String groupName) {
    try {
      String val = matcher.group(groupName);
      if (val == null) {
        return null;
      }
      if ((groupName.equals("eventName") || groupName.equals("propertyValue"))
              && val.length() >= 2
              && val.startsWith("\"")
              && val.endsWith("\"")) {
        return val.substring(1, val.length() - 1);
      }
      return val;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Creates a DTO for creating a new calendar.
   *
   * @param name The name of the calendar to create.
   * @param timezone The timezone ID for the calendar.
   * @return A CalendarDto configured for calendar creation.
   */
  public static CalendarDto createCalendar(String name, String timezone) {
    return new CalendarDto(CalendarCommandType.CREATE_CALENDAR, name, timezone, null, null);
  }

  /**
   * Creates a DTO for switching to a different calendar.
   *
   * @param name The name of the calendar to switch to.
   * @return A CalendarDto configured for calendar switching.
   */
  public static CalendarDto useCalendar(String name) {
    return new CalendarDto(CalendarCommandType.USE_CALENDAR, name, null, null, null);
  }

  /**
   * Creates a DTO for editing a calendar property.
   *
   * @param name The name of the calendar to edit.
   * @param property The property to modify.
   * @param value The new value for the property.
   * @return A CalendarDto configured for calendar editing.
   */
  public static CalendarDto editCalendar(String name, String property, String value) {
    return new CalendarDto(CalendarCommandType.EDIT_CALENDAR, name, null, property, value);
  }

  public CalendarCommandType getType() {
    return type;
  }

  public String getCalendarName() {
    return calendarName;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getPropertyValue() {
    return propertyValue;
  }
}