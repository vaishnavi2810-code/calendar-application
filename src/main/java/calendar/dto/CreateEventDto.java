package calendar.dto;

import java.util.regex.Matcher;

/**
 * Data Transfer Object for creating calendar events. Encapsulates all the parameters
 * needed to create different types of events including timed and all-day events,
 * with support for both single and recurring event patterns.
 */
public class CreateEventDto {

  /**
   * Enumeration of different event creation command types.
   */
  public enum CommandType {
    TIMED_SINGLE,
    TIMED_RECURRING_FOR,
    TIMED_RECURRING_UNTIL,
    ALL_DAY_SINGLE,
    ALL_DAY_RECURRING_FOR,
    ALL_DAY_RECURRING_UNTIL
  }

  private final CommandType type;
  private final String subject;
  private final String startDateTime;
  private final String endDateTime;
  private final String onDate;
  private final String weekdays;
  private final String ntimes;
  private final String untilDate;

  /**
   * Creates a CreateEventDto by extracting relevant fields from the provided matcher
   * based on the command type.
   *
   * @param type the type of event creation command being processed
   * @param matcher the regex matcher containing the parsed command data with named groups
   */
  public CreateEventDto(CommandType type, Matcher matcher) {
    this.type = type;
    this.subject = getGroup(matcher, "subject");
    this.startDateTime = getGroup(matcher, "start");
    this.endDateTime = getGroup(matcher, "end");
    this.onDate = getGroup(matcher, "date");
    this.weekdays = getGroup(matcher, "weekdays");
    this.ntimes = getGroup(matcher, "N");
    this.untilDate = getGroup(matcher, "until");
  }

  /**
   * Creates a CreateEventDto directly from parameters without using a Matcher.
   * Used for GUI mode where data comes from form fields rather than text parsing.
   *
   * @param type the type of event creation command being processed
   * @param subject the event subject/name
   * @param startDateTime start date-time in YYYY-MM-DDTHH:mm format
   * @param endDateTime end date-time in YYYY-MM-DDTHH:mm format
   * @param onDate date in YYYY-MM-DD format for all-day events
   * @param weekdays weekdays string (e.g., "MWF" for Mon/Wed/Fri)
   * @param ntimes number of repetitions as string
   * @param untilDate end date in YYYY-MM-DD format for recurring events
   */
  private CreateEventDto(CommandType type, String subject, String startDateTime,
                         String endDateTime, String onDate, String weekdays,
                         String ntimes, String untilDate) {
    this.type = type;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.onDate = onDate;
    this.weekdays = weekdays;
    this.ntimes = ntimes;
    this.untilDate = untilDate;
  }

  private String getGroup(Matcher matcher, String groupName) {
    try {
      String val = matcher.group(groupName);
      if (groupName.equals("subject")
              &&
              val.length() >= 2
              &&
              val.startsWith("\"")
              &&
              val.endsWith("\"")) {
        return val.substring(1, val.length() - 1);
      }
      return val;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Creates a DTO for a single timed event.
   *
   * @param subject event name
   * @param startDateTime start in YYYY-MM-DDTHH:mm format
   * @param endDateTime end in YYYY-MM-DDTHH:mm format
   * @return configured CreateEventDto
   */
  public static CreateEventDto timedSingle(String subject, String startDateTime,
                                           String endDateTime) {
    return new CreateEventDto(CommandType.TIMED_SINGLE, subject, startDateTime,
            endDateTime, null, null, null, null);
  }

  /**
   * Creates a DTO for a recurring timed event with fixed repetitions.
   *
   * @param subject event name
   * @param startDateTime start in YYYY-MM-DDTHH:mm format
   * @param endDateTime end in YYYY-MM-DDTHH:mm format
   * @param weekdays days to repeat (e.g., "MWF")
   * @param times number of repetitions
   * @return configured CreateEventDto
   */
  public static CreateEventDto timedRecurringFor(String subject, String startDateTime,
                                                 String endDateTime, String weekdays,
                                                 String times) {
    return new CreateEventDto(CommandType.TIMED_RECURRING_FOR, subject, startDateTime,
            endDateTime, null, weekdays, times, null);
  }

  /**
   * Creates a DTO for a recurring timed event until a date.
   *
   * @param subject event name
   * @param startDateTime start in YYYY-MM-DDTHH:mm format
   * @param endDateTime end in YYYY-MM-DDTHH:mm format
   * @param weekdays days to repeat (e.g., "MWF")
   * @param untilDate end date in YYYY-MM-DD format
   * @return configured CreateEventDto
   */
  public static CreateEventDto timedRecurringUntil(String subject, String startDateTime,
                                                   String endDateTime, String weekdays,
                                                   String untilDate) {
    return new CreateEventDto(CommandType.TIMED_RECURRING_UNTIL, subject, startDateTime,
            endDateTime, null, weekdays, null, untilDate);
  }

  /**
   * Creates a DTO for a single all-day event.
   *
   * @param subject event name
   * @param date date in YYYY-MM-DD format
   * @return configured CreateEventDto
   */
  public static CreateEventDto allDaySingle(String subject, String date) {
    return new CreateEventDto(CommandType.ALL_DAY_SINGLE, subject, null, null,
            date, null, null, null);
  }

  /**
   * Creates a DTO for a recurring all-day event with fixed repetitions.
   *
   * @param subject event name
   * @param date starting date in YYYY-MM-DD format
   * @param weekdays days to repeat (e.g., "MWF")
   * @param times number of repetitions
   * @return configured CreateEventDto
   */
  public static CreateEventDto allDayRecurringFor(String subject, String date,
                                                  String weekdays, String times) {
    return new CreateEventDto(CommandType.ALL_DAY_RECURRING_FOR, subject, null, null,
            date, weekdays, times, null);
  }

  /**
   * Creates a DTO for a recurring all-day event until a date.
   *
   * @param subject event name
   * @param date starting date in YYYY-MM-DD format
   * @param weekdays days to repeat (e.g., "MWF")
   * @param untilDate end date in YYYY-MM-DD format
   * @return configured CreateEventDto
   */
  public static CreateEventDto allDayRecurringUntil(String subject, String date,
                                                    String weekdays, String untilDate) {
    return new CreateEventDto(CommandType.ALL_DAY_RECURRING_UNTIL, subject, null, null,
            date, weekdays, null, untilDate);
  }

  public CommandType getType() {
    return type;
  }

  public String getSubject() {
    return subject;
  }

  public String getStartDateTime() {
    return startDateTime;
  }

  public String getEndDateTime() {
    return endDateTime;
  }

  public String getOnDate() {
    return onDate;
  }

  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of times a recurring event should repeat.
   *
   * @return the repetition count string
   */
  public String getnTimes() {
    return ntimes;
  }

  public String getUntilDate() {
    return untilDate;
  }
}