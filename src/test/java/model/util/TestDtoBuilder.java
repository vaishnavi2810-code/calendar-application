package model.util;

import calendar.dto.CreateEventDto;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test utility class for building CreateEventDto objects in unit tests.
 * This class provides static factory methods to create DTOs for various event types
 * by simulating the regex pattern matching that occurs in the parser. Each method
 * constructs a command string, matches it against the appropriate pattern, and returns
 * a CreateEventDto initialized with the matched groups. This approach ensures that
 * test DTOs are created in the same way as production DTOs.
 */
public class TestDtoBuilder {

  /**
   * Creates a CreateEventDto for a single timed event without quotes around the subject.
   * Simulates the command: "create event {subject} from {start} to {end}"
   *
   * @param subject the event subject (single word, no quotes needed)
   * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
   * @return a CreateEventDto configured for a timed single event
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createTimedSingleDtoWithoutQuotes(String subject,
                                                                 String start, String end) {
    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            +
            "to (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String patternStr = String.format("^create event %s %s$", subjectPattern, fromTo);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
    String command = String.format("create event %s from %s to %s", subject, start, end);
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.TIMED_SINGLE, matcher);
  }

  /**
   * Creates a CreateEventDto for a single timed event with quotes around the subject.
   * Simulates the command: "create event "{subject}" from {start} to {end}"
   *
   * @param subject the event subject (can contain multiple words)
   * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
   * @return a CreateEventDto configured for a timed single event
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createTimedSingleDtoWithQuotes(String subject,
                                                                String start, String end) {
    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            +
            "to (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";

    String patternStr = String.format("^create event %s %s$", subjectPattern, fromTo);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event \"%s\" from %s to %s", subject, start, end);
    Matcher matcher = pattern.matcher(command);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }

    return new CreateEventDto(CreateEventDto.CommandType.TIMED_SINGLE, matcher);
  }

  /**
   * Creates a CreateEventDto for a single all-day event without quotes around the subject.
   * Simulates the command: "create event {subject} on {date}"
   *
   * @param subject the event subject (single word, no quotes needed)
   * @param date the event date in format "yyyy-MM-dd"
   * @return a CreateEventDto configured for an all-day single event
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createAllDaySingleDtoWithoutQuotes(String subject, String date) {
    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String patternStr = String.format("^create event %s %s$", subjectPattern, onDate);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
    String command = String.format("create event %s on %s", subject, date);
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_SINGLE, matcher);
  }

  /**
   * Creates a CreateEventDto for a single all-day event with quotes around the subject.
   * Simulates the command: "create event "{subject}" on {date}"
   *
   * @param subject the event subject (can contain multiple words)
   * @param date the event date in format "yyyy-MM-dd"
   * @return a CreateEventDto configured for an all-day single event
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createAllDaySingleDtoWithQuotes(String subject, String date) {
    String subjectPattern = "(?<subject>\\\"(.*?)\\\")";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String patternStr = String.format("^create event %s %s$", subjectPattern, onDate);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event \"%s\" on %s", subject, date);
    Matcher matcher = pattern.matcher(command);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }

    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_SINGLE, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring all-day event until a specified date, with quotes.
   * Simulates the command:"create event "{subject}" on {date} repeats {weekdays} until {untilDate}"
   *
   * @param subject the event subject (can contain multiple words)
   * @param startDate the start date in format "yyyy-MM-dd"
   * @param weekdays the weekdays pattern (e.g., "MWF", "TR", "SU")
   * @param untilDate the end date in format "yyyy-MM-dd"
   * @return a CreateEventDto configured for an all-day recurring event until the specified date
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createAllDayRecurringDtoUntilWithQuotes(
      String subject,
      String startDate,
      String weekdays,
      String untilDate) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\")";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String repeatsUntil = "repeats (?<weekdays>[MTWRFSU]+) until (?<until>\\d{4}-\\d{2}-\\d{2})";
    String patternStr = String.format("^create event %s %s %s$",
        subjectPattern,
        onDate,
        repeatsUntil);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
    String command = String.format("create event \"%s\" on %s repeats %s until %s",
        subject,
        startDate,
        weekdays,
        untilDate);
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_RECURRING_UNTIL, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring all-day event until a specified date, without quotes.
   * Simulates the command: "create event {subject} on {date} repeats {weekdays} until {untilDate}"
   *
   * @param subject the event subject (single word, no quotes needed)
   * @param startDate the start date in format "yyyy-MM-dd"
   * @param weekdays the weekdays pattern (e.g., "MWF", "TR", "SU")
   * @param untilDate the end date in format "yyyy-MM-dd"
   * @return a CreateEventDto configured for an all-day recurring event until the specified date
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createAllDayRecurringDtoUntilWithoutQuotes(
      String subject,
      String startDate,
      String weekdays,
      String untilDate) {
    String subjectPattern = "(?<subject>\\S+)";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String repeatsUntil = "repeats (?<weekdays>[MTWRFSU]+) "
            +
            "until (?<until>\\d{4}-\\d{2}-\\d{2})";
    String patternStr = String.format("^create event %s %s %s$",
        subjectPattern,
        onDate,
        repeatsUntil);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
    String command = String.format("create event %s on %s repeats %s until %s",
        subject,
        startDate,
        weekdays,
        untilDate);
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_RECURRING_UNTIL, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring timed event with a
   * fixed number of occurrences, with quotes.
   * Simulates the command:create event {subject} from {start} to {end} repeats {weekdays}
   * for {ntimes} times
   *
   * @param subject the event subject (can contain multiple words)
   * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param weekdays the weekdays pattern (e.g., "MWF", "TR")
   * @param ntimes the number of occurrences as a string
   * @return a CreateEventDto configured for a timed recurring event with fixed repetitions
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createTimedRecurringForDtoWithQuotes(
          String subject,
          String start,
          String end,
          String weekdays,
          String ntimes) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            +
            "to (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String repeatsFor = "repeats (?<weekdays>[MTWRFSU]+) for (?<N>\\d+) times";

    String patternStr = String.format("^create event %s %s %s$",
            subjectPattern, fromTo, repeatsFor);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event \"%s\" from %s to %s repeats %s for %s times",
            subject, start, end, weekdays, ntimes);

    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.TIMED_RECURRING_FOR, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring timed event with
   * a fixed number of occurrences, without quotes.
   * Simulates the command: "create event {subject} from {start} to {end} repeats {weekdays}
   * for {ntimes} times"
   *
   * @param subject the event subject (single word, no quotes needed)
   * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param weekdays the weekdays pattern (e.g., "MWF", "TR")
   * @param ntimes the number of occurrences as a string
   * @return a CreateEventDto configured for a timed recurring event with fixed repetitions
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createTimedRecurringForDtoWithoutQuotes(
          String subject,
          String start,
          String end,
          String weekdays,
          String ntimes) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            +
            "to (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String repeatsFor = "repeats (?<weekdays>[MTWRFSU]+) for (?<N>\\d+) times";

    String patternStr = String.format("^create event %s %s %s$",
            subjectPattern, fromTo, repeatsFor);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event %s from %s to %s repeats %s for %s times",
            subject, start, end, weekdays, ntimes);

    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.TIMED_RECURRING_FOR, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring timed event until a specified date, with quotes.
   * Simulates the command: "create event "{subject}" from {start} to {end}
   * repeats {weekdays} until {untilDate}"
   *
   * @param subject the event subject (can contain multiple words)
   * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param weekdays the weekdays pattern (e.g., "MWF", "TR")
   * @param untilDate the end date in format "yyyy-MM-dd"
   * @return a CreateEventDto configured for a timed recurring event until the specified date
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createTimedRecurringUntilDtoWithQuotes(
          String subject,
          String start,
          String end,
          String weekdays,
          String untilDate) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            +
            "to (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String repeatsUntil = "repeats (?<weekdays>[MTWRFSU]+) until (?<until>\\d{4}-\\d{2}-\\d{2})";

    String patternStr = String.format("^create event %s %s %s$",
            subjectPattern, fromTo, repeatsUntil);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event \"%s\" from %s to %s repeats %s until %s",
            subject, start, end, weekdays, untilDate);

    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.TIMED_RECURRING_UNTIL, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring timed event until a specified date, without quotes.
   * Simulates the command: "create event {subject} from {start} to {end}
   * repeats {weekdays} until {untilDate}"
   *
   * @param subject the event subject (single word, no quotes needed)
   * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
   * @param weekdays the weekdays pattern (e.g., "MWF", "TR")
   * @param untilDate the end date in format "yyyy-MM-dd"
   * @return a CreateEventDto configured for a timed recurring event until the specified date
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createTimedRecurringUntilDtoWithoutQuotes(
          String subject,
          String start,
          String end,
          String weekdays,
          String untilDate) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String fromTo = "from (?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            +
            "to (?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String repeatsUntil = "repeats (?<weekdays>[MTWRFSU]+) until (?<until>\\d{4}-\\d{2}-\\d{2})";

    String patternStr = String.format("^create event %s %s %s$",
            subjectPattern, fromTo, repeatsUntil);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event %s from %s to %s repeats %s until %s",
            subject, start, end, weekdays, untilDate);

    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_RECURRING_UNTIL, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring all-day event with a fixed number
   * of occurrences, with quotes.
   * Simulates the command: create event {subject} on {date} repeats {weekdays} for {ntimes} times
   *
   * @param subject the event subject (can contain multiple words)
   * @param startDate the start date in format "yyyy-MM-dd"
   * @param weekdays the weekdays pattern (e.g., "MWF", "S")
   * @param ntimes the number of occurrences as a string
   * @return a CreateEventDto configured for an all-day recurring event with fixed repetitions
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createAllDayRecurringForDtoWithQuotes(
          String subject,
          String startDate,
          String weekdays,
          String ntimes) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String repeatsFor = "repeats (?<weekdays>[MTWRFSU]+) for (?<N>\\d+) times";

    String patternStr = String.format("^create event %s %s %s$", subjectPattern,
            onDate, repeatsFor);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event \"%s\" on %s repeats %s for %s times",
            subject, startDate, weekdays, ntimes);

    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_RECURRING_FOR, matcher);
  }

  /**
   * Creates a CreateEventDto for a recurring all-day event with a fixed number
   * of occurrences, without quotes.
   * Simulates the command: "create event {subject} on {date} repeats {weekdays} for {ntimes} times"
   *
   * @param subject the event subject (single word, no quotes needed)
   * @param startDate the start date in format "yyyy-MM-dd"
   * @param weekdays the weekdays pattern (e.g., "MWF", "S")
   * @param ntimes the number of occurrences as a string
   * @return a CreateEventDto configured for an all-day recurring event with fixed repetitions
   * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
   */
  public static CreateEventDto createAllDayRecurringForDtoWithoutQuotes(
          String subject,
          String startDate,
          String weekdays,
          String ntimes) {

    String subjectPattern = "(?<subject>\\\"(.*?)\\\"|\\S+)";
    String onDate = "on (?<date>\\d{4}-\\d{2}-\\d{2})";
    String repeatsFor = "repeats (?<weekdays>[MTWRFSU]+) for (?<N>\\d+) times";

    String patternStr = String.format("^create event %s %s %s$",
            subjectPattern, onDate, repeatsFor);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("create event %s on %s repeats %s for %s times",
            subject, startDate, weekdays, ntimes);

    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new CreateEventDto(CreateEventDto.CommandType.ALL_DAY_RECURRING_FOR, matcher);
  }

}