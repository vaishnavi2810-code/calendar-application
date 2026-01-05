package calendar.dto;

import java.util.regex.Matcher;

/**
 * Data Transfer Object for copying calendar events.
 * Encapsulates the parameters required for various copy strategies.
 */
public class CopyEventDto {

  /**
   * Enumeration representing the available types of copy operations.
   */
  public enum CopyType {
    COPY_SINGLE_EVENT,
    COPY_EVENTS_ON_DATE,
    COPY_EVENTS_BETWEEN_DATES
  }

  private final CopyType copyType;
  private final String targetCalendarName;

  private final String eventName;
  private final String sourceStartDateTime;
  private final String targetStartDateTime;

  private final String sourceDate;
  private final String targetDate;

  private final String intervalStartDate;
  private final String intervalEndDate;
  private final String targetStartDate;

  /**
   * Constructs a CopyEventDto by extracting values from a Regex Matcher.
   * Parses named groups from the matcher to populate the DTO fields.
   *
   * @param copyType the specific type of copy operation
   * @param matcher  the regex matcher containing the parsed command arguments
   */
  public CopyEventDto(CopyType copyType, Matcher matcher) {
    this.copyType = copyType;
    this.targetCalendarName = getGroup(matcher, "targetCalendar");

    this.eventName = getGroup(matcher, "eventName");
    this.sourceStartDateTime = getGroup(matcher, "sourceStartDateTime");
    this.targetStartDateTime = getGroup(matcher, "targetStartDateTime");

    this.sourceDate = getGroup(matcher, "sourceDate");
    this.targetDate = getGroup(matcher, "targetDate");

    this.intervalStartDate = getGroup(matcher, "intervalStartDate");
    this.intervalEndDate = getGroup(matcher, "intervalEndDate");
    this.targetStartDate = getGroup(matcher, "targetStartDate");
  }

  /**
   * Safely retrieves a named group from the matcher.
   * Strips surrounding quotes from the "eventName" group if present.
   *
   * @param matcher   the regex matcher
   * @param groupName the name of the group to retrieve
   * @return the matched string value, or null if the group is missing or invalid
   */
  private String getGroup(Matcher matcher, String groupName) {
    try {
      String val = matcher.group(groupName);
      if (val == null) {
        return null;
      }
      if (groupName.equals("eventName")
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
   * Retrieves the type of copy operation.
   *
   * @return the copy type enum
   */
  public CopyType getCopyType() {
    return copyType;
  }

  /**
   * Retrieves the name of the target calendar where events will be copied.
   *
   * @return the target calendar name
   */
  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  /**
   * Retrieves the name of the specific event to copy.
   * Used for COPY_SINGLE_EVENT.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Retrieves the start date/time of the source event.
   * Used for COPY_SINGLE_EVENT.
   *
   * @return the source start date string
   */
  public String getSourceStartDateTime() {
    return sourceStartDateTime;
  }

  /**
   * Retrieves the target start date/time for the copied event.
   * Used for COPY_SINGLE_EVENT.
   *
   * @return the target start date string
   */
  public String getTargetStartDateTime() {
    return targetStartDateTime;
  }

  /**
   * Retrieves the specific date to copy events from.
   * Used for COPY_EVENTS_ON_DATE.
   *
   * @return the source date string
   */
  public String getSourceDate() {
    return sourceDate;
  }

  /**
   * Retrieves the target date to paste events to.
   * Used for COPY_EVENTS_ON_DATE.
   *
   * @return the target date string
   */
  public String getTargetDate() {
    return targetDate;
  }

  /**
   * Retrieves the start date of the interval to copy.
   * Used for COPY_EVENTS_BETWEEN_DATES.
   *
   * @return the interval start date string
   */
  public String getIntervalStartDate() {
    return intervalStartDate;
  }

  /**
   * Retrieves the end date of the interval to copy.
   * Used for COPY_EVENTS_BETWEEN_DATES.
   *
   * @return the interval end date string
   */
  public String getIntervalEndDate() {
    return intervalEndDate;
  }

  /**
   * Retrieves the start date in the target calendar where the sequence begins.
   * Used for COPY_EVENTS_BETWEEN_DATES.
   *
   * @return the target start date string
   */
  public String getTargetStartDate() {
    return targetStartDate;
  }
}