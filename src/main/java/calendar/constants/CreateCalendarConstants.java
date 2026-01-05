package calendar.constants;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Constants class containing timezone, date/time formatters, and day of week mappings
 * used throughout the calendar application. This class cannot be instantiated and provides
 * utility methods for parsing weekday strings into DayOfWeek enums.
 */
public class CreateCalendarConstants {

  /**
   * Private constructor to prevent instantiation of this constants class.
   *
   * @throws AssertionError always, to enforce non-instantiability
   */
  private CreateCalendarConstants() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  public static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  public static final DateTimeFormatter PRINT_DATE_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateTimeFormatter PRINT_TIME_FORMAT =
          DateTimeFormatter.ofPattern("HH:mm");
  public static final Map<Character, DayOfWeek> DAY_CHAR_MAP = Map.of(
      'M', DayOfWeek.MONDAY,
      'T', DayOfWeek.TUESDAY,
      'W', DayOfWeek.WEDNESDAY,
      'R', DayOfWeek.THURSDAY,
      'F', DayOfWeek.FRIDAY,
      'S', DayOfWeek.SATURDAY,
      'U', DayOfWeek.SUNDAY
  );

  /**
   * Parses a string of weekday abbreviations into a set of DayOfWeek enums.
   * Each character in the input string is mapped to its corresponding day of the week
   * using the DAY_CHAR_MAP. Invalid characters are ignored. The input is case-insensitive.
   *
   * @param weekdays a string containing weekday abbreviations such as "MWF", "TR", or "U"
   * @return a set of DayOfWeek enums corresponding to the input string, or an empty set
   *         if the input is null or empty
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdays) {
    Set<DayOfWeek> days = new HashSet<>();
    if (weekdays == null || weekdays.isEmpty()) {
      return days;
    }
    for (char dayChar : weekdays.toUpperCase().toCharArray()) {
      DayOfWeek day = DAY_CHAR_MAP.get(dayChar);
      if (day != null) {
        days.add(day);
      }
    }
    return days;
  }
}
