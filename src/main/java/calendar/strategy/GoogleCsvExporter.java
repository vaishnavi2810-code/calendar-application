package calendar.strategy;

import static calendar.constants.ExportConstants.CSV_HEADER;
import static calendar.constants.ExportConstants.GOOGLE_DATE_FORMAT;
import static calendar.constants.ExportConstants.GOOGLE_TIME_FORMAT;

import calendar.interfacetypes.Iexport;
import calendar.model.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Export strategy implementation for creating Google Calendar-compatible CSV files.
 * This class converts a collection of calendar events into a CSV format with columns
 * for subject, start date, start time, end date, end time, description, location, and status.
 * The dates and times are formatted according to Google Calendar's expected format.
 */
public class GoogleCsvExporter implements Iexport {

  /**
   * Exports a collection of events to a CSV file in Google Calendar format.
   * Creates a CSV file with a header row and one row per event, containing the event's
   * subject, start/end dates and times, description, location, and status. The subject
   * field is properly quoted if it contains commas, quotes, or newlines. All date and
   * time values are formatted according to Google Calendar's requirements.
   *
   * @param events the collection of events to be exported
   * @param fileName the name of the file to create, including the .csv extension
   * @return the absolute path of the created CSV file
   * @throws IOException if the file cannot be created or written to
   */
  public String export(Collection<Event> events, String fileName) throws IOException {

    StringBuilder csvBuilder = new StringBuilder();
    csvBuilder.append(CSV_HEADER);
    for (Event event : events) {
      ZonedDateTime start = event.getStartDateTime();
      ZonedDateTime end = event.getEndDateTime();
      String eventLocation = event.getLocation();
      String eventDescription = event.getDescription();
      String status = event.getStatus();
      String startDateStr = start.format(GOOGLE_DATE_FORMAT);
      String startTimeStr = start.format(GOOGLE_TIME_FORMAT);
      String endDateStr = end.format(GOOGLE_DATE_FORMAT);
      String endTimeStr = end.format(GOOGLE_TIME_FORMAT);
      csvBuilder.append(quoteCsvField(event.getSubject())).append(",");
      csvBuilder.append(startDateStr).append(",");
      csvBuilder.append(startTimeStr).append(",");
      csvBuilder.append(endDateStr).append(",");
      csvBuilder.append(endTimeStr).append(",");
      csvBuilder.append(eventDescription).append(",");
      csvBuilder.append(eventLocation).append(",");
      csvBuilder.append(status).append("\n");

    }
    Path filePath = Paths.get(fileName);
    Files.writeString(filePath, csvBuilder.toString());
    return filePath.toAbsolutePath().toString();
  }

  /**
   * Properly formats a field value for CSV by escaping special characters.
   * If the field contains commas, double quotes, or newlines, wraps it in double quotes
   * and escapes any existing double quotes by doubling them (CSV standard). Returns
   * an empty string if the field is null or empty.
   *
   * @param field the field value to format
   * @return the properly escaped and quoted field value, or empty string if null/empty
   */
  public String quoteCsvField(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      field = field.replace("\"", "\"\"");
      return "\"" + field + "\"";
    }
    return field;
  }
}