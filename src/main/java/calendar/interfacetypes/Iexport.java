package calendar.interfacetypes;

import calendar.model.Event;
import java.io.IOException;
import java.util.Collection;

/**
 * Strategy interface for exporting calendar events to external file formats.
 * Implementations define how events should be formatted and written to different
 * file types (e.g., CSV for Google Calendar, iCalendar format, etc.).
 */

public interface Iexport {

  /**
   * Exports the given collection of events to a file with the specified name.
   * The export format and structure are determined by the implementing class.
   * The file is created in the current working directory or at the specified path.
   *
   * @param events the collection of events to be exported
   * @param fileName the name (and optional path) of the file to create
   * @return a confirmation message, typically the absolute path of the created file
   * @throws IOException if the file cannot be created, written to, or if an I/O error occurs
   */
  String export(Collection<Event> events, String fileName) throws IOException;
}