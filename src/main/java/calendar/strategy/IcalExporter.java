package calendar.strategy;

import calendar.interfacetypes.Iexport;
import calendar.model.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Export strategy implementation for creating iCalendar (.ics) files.
 * This class converts a collection of calendar events into the iCalendar
 * format, conforming to the RFC 5545 specification.
 */
public class IcalExporter implements Iexport {
  private static final DateTimeFormatter ICAL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  private static final String PRODID = "-//ExampleApp//MyCalendar 1.0//EN";

  /**
  * Exports a collection of events to a file in iCalendar (.ics) format.
  * Creates a file with the required iCalendar header and footer, and
  * one VEVENT entry for each event in the collection.
  *
  * @param events the collection of events to be exported
  * @param fileName the name of the file to create (e.g., "my_calendar.ical")
  * @return the absolute path of the created iCal file
  * @throws IOException if the file cannot be created or written to
  */
  @Override
  public String export(Collection<Event> events, String fileName) throws IOException {
    StringBuilder icalBuilder = new StringBuilder();

    icalBuilder.append("BEGIN:VCALENDAR\n");
    icalBuilder.append("VERSION:2.0\n");
    icalBuilder.append("PRODID:").append(PRODID).append("\n");
    icalBuilder.append("CALSCALE:GREGORIAN\n");

    for (Event event : events) {
      icalBuilder.append("BEGIN:VEVENT\n");
      ZonedDateTime start = event.getStartDateTime();
      ZonedDateTime end = event.getEndDateTime();
      String dtStart = start.withZoneSameInstant(ZoneOffset.UTC).format(ICAL_DATE_FORMAT);
      String dtEnd = end.withZoneSameInstant(ZoneOffset.UTC).format(ICAL_DATE_FORMAT);
      String dtStamp = ZonedDateTime.now(ZoneOffset.UTC).format(ICAL_DATE_FORMAT);
      icalBuilder.append("DTSTAMP:").append(dtStamp).append("\n");
      icalBuilder.append("DTSTART:").append(dtStart).append("\n");
      icalBuilder.append("DTEND:").append(dtEnd).append("\n");
      icalBuilder.append("SUMMARY:").append(escapeIcalString(event.getSubject())).append("\n");
      icalBuilder.append("LOCATION:").append(escapeIcalString(event.getLocation())).append("\n");
      icalBuilder.append("DESCRIPTION:")
              .append(escapeIcalString(event.getDescription()))
              .append("\n");
      icalBuilder.append("STATUS:").append(mapStatus(event.getStatus())).append("\n");
      icalBuilder.append("END:VEVENT\n");
    }
    icalBuilder.append("END:VCALENDAR\n");
    Path filePath = Paths.get(fileName);
    Files.writeString(filePath, icalBuilder.toString());
    return filePath.toAbsolutePath().toString();
  }

  /**
  * Escapes special characters in a string for iCalendar text fields.
  * According to iCal spec, newlines must be escaped as "\n",
  * and commas, semicolons, and backslashes must be escaped with a backslash.
  *
  * @param field The string to escape.
  * @return The escaped string, or an empty string if input is null.
 */
  private String escapeIcalString(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }
    return field.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
  }

  /**
  * Maps an event status string to a valid iCalendar STATUS value.
  * The iCal standard values are "TENTATIVE", "CONFIRMED", "CANCELLED".
  *
  * @param eventStatus The status string from the Event object.
  * @return A valid iCalendar status (defaults to "CONFIRMED").
  */
  private String mapStatus(String eventStatus) {
    if (eventStatus == null) {
      return "CONFIRMED";
    }
    String upperStatus = eventStatus.toUpperCase();
    switch (upperStatus) {
      case "TENTATIVE":
      case "CANCELLED":
        return upperStatus;
      case "CONFIRMED":
      default:
        return "CONFIRMED";
    }
  }
}