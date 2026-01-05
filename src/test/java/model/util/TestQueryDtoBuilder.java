package model.util;

import calendar.dto.QueryEventDto;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test utility class for building QueryEventDto objects in unit tests.
 * This class provides static factory methods to create DTOs for various query types
 * by simulating the regex pattern matching that occurs in the parser. Each method
 * constructs a query command string, matches it against the appropriate pattern, and returns
 * a QueryEventDto initialized with the matched groups. This approach ensures that
 * test DTOs are created in the same way as production DTOs.
 */
public class TestQueryDtoBuilder {

  /**
  * Creates a QueryEventDto for printing events on a specific date.
  * Simulates the command: "print events on {date}"
  *
  * @param date the date in format "yyyy-MM-dd"
  * @return a QueryEventDto configured for printing events on the specified date
  * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
  */
  public static QueryEventDto createPrintOnDateDto(String date) {
    String datePattern = "(?<date>\\d{4}-\\d{2}-\\d{2})";
    String patternStr = String.format("^print events on %s$", datePattern);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("print events on %s", date);
    Matcher matcher = pattern.matcher(command);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }

    return new QueryEventDto(QueryEventDto.QueryType.PRINT_ON_DATE, matcher);
  }

  /**
  * Creates a QueryEventDto for printing events within a date-time range.
  * Simulates the command: "print events from {start} to {end}"
  *
  * @param start the start date-time in format "yyyy-MM-dd'T'HH:mm"
  * @param end the end date-time in format "yyyy-MM-dd'T'HH:mm"
  * @return a QueryEventDto configured for printing events in the specified range
  * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
  */
  public static QueryEventDto createPrintInRangeDto(String start, String end) {
    String dateTimeStart = "(?<start>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String dateTimeEnd = "(?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String patternStr = String.format("^print events from %s to %s$",
           dateTimeStart, dateTimeEnd);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("print events from %s to %s", start, end);
    Matcher matcher = pattern.matcher(command);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new QueryEventDto(QueryEventDto.QueryType.PRINT_IN_RANGE, matcher);
  }

  /**
  * Creates a QueryEventDto for showing availability status at a specific instant.
  * Simulates the command: "show status on {datetime}"
  *
  * @param datetime the date-time instant in format "yyyy-MM-dd'T'HH:mm"
  * @return a QueryEventDto configured for showing status at the specified instant
  * @throws IllegalArgumentException if the constructed command doesn't match the expected pattern
  */
  public static QueryEventDto createShowStatusAtDto(String datetime) {
    String dateTimePattern = "(?<datetime>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
    String patternStr = String.format("^show status on %s$", dateTimePattern);
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    String command = String.format("show status on %s", datetime);
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Test data doesn't match pattern: " + command);
    }
    return new QueryEventDto(QueryEventDto.QueryType.SHOW_STATUS_AT, matcher);
  }
}