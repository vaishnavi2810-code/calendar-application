package calendar.dto;

import java.time.LocalDate;
import java.util.regex.Matcher;

/**
 * Data Transfer Object for querying calendar events.
 */
public class QueryEventDto {

  /**
   * Enumeration of different query types supported by the calendar system.
   */
  public enum QueryType {
    PRINT_ON_DATE,
    PRINT_IN_RANGE,
    SHOW_STATUS_AT
  }

  private final QueryType type;
  private final String onDate;
  private final String rangeStart;
  private final String rangeEnd;
  private final String atInstant;

  /**
   * Creates a QueryEventDto by extracting and parsing relevant fields from the provided
   * matcher based on the query type.
   *
   * @param type the type of query operation
   * @param matcher the regex matcher containing the parsed query data with named groups
   */
  public QueryEventDto(QueryType type, Matcher matcher) {
    this.type = type;
    this.onDate = getGroup(matcher, "date");
    this.rangeStart = getGroup(matcher, "start");
    this.rangeEnd = getGroup(matcher, "end");
    this.atInstant = getGroup(matcher, "datetime");
  }

  /**
   * Creates a QueryEventDto directly from parameters without using a Matcher.
   * Used for GUI mode where data comes from form fields rather than text parsing.
   *
   * @param type the type of query operation
   * @param onDate date string in YYYY-MM-DD format
   * @param rangeStart start datetime string in YYYY-MM-DDTHH:mm format
   * @param rangeEnd end datetime string in YYYY-MM-DDTHH:mm format
   * @param atInstant instant datetime string in YYYY-MM-DDTHH:mm format
   */
  private QueryEventDto(QueryType type, String onDate, String rangeStart,
                        String rangeEnd, String atInstant) {
    this.type = type;
    this.onDate = onDate;
    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
    this.atInstant = atInstant;
  }

  private String getGroup(Matcher matcher, String groupName) {
    try {
      return matcher.group(groupName);
    } catch (IllegalArgumentException | IllegalStateException e) {
      return null;
    }
  }

  private LocalDate parseLocalDate(Matcher matcher, String groupName) {
    String dateStr = getGroup(matcher, groupName);
    return (dateStr != null) ? LocalDate.parse(dateStr) : null;
  }

  /**
   * Creates a query DTO for events on a specific date.
   *
   * @param date date string in YYYY-MM-DD format
   * @return configured QueryEventDto
   */
  public static QueryEventDto forDate(String date) {
    return new QueryEventDto(QueryType.PRINT_ON_DATE, date, null, null, null);
  }

  /**
   * Creates a query DTO for events in a datetime range.
   *
   * @param startDateTime start in YYYY-MM-DDTHH:mm format
   * @param endDateTime end in YYYY-MM-DDTHH:mm format
   * @return configured QueryEventDto
   */
  public static QueryEventDto forRange(String startDateTime, String endDateTime) {
    return new QueryEventDto(QueryType.PRINT_IN_RANGE, null, startDateTime, endDateTime, null);
  }

  /**
   * Creates a query DTO for status at a specific instant.
   *
   * @param instant instant in YYYY-MM-DDTHH:mm format
   * @return configured QueryEventDto
   */
  public static QueryEventDto forStatus(String instant) {
    return new QueryEventDto(QueryType.SHOW_STATUS_AT, null, null, null, instant);
  }

  public QueryType getType() {
    return type;
  }

  public String getOnDate() {
    return onDate;
  }

  public String getRangeStart() {
    return rangeStart;
  }

  public String getRangeEnd() {
    return rangeEnd;
  }

  public String getAtInstant() {
    return atInstant;
  }
}