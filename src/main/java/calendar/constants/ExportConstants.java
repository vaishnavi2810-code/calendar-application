package calendar.constants;

import java.time.format.DateTimeFormatter;

/**
 * Constants class containing formatters and header strings used for exporting
 * calendar events to external formats such as Google Calendar CSV files.
 * This class cannot be instantiated.
 */
public class ExportConstants {
  public static final DateTimeFormatter GOOGLE_DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  public static final DateTimeFormatter GOOGLE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("hh:mm a");
  public static final String CSV_HEADER = "Subject,Start Date,Start Time,End Date,End Time\n";

}
