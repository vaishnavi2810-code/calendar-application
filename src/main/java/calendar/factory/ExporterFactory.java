package calendar.factory;

import calendar.dto.ExportEventDto;
import calendar.interfacetypes.Iexport;
import calendar.strategy.GoogleCsvExporter;
import calendar.strategy.IcalExporter;

/**
 * Factory class responsible for creating appropriate exporter strategy implementations
 * based on the file type specified in the export parameters. This factory uses the
 * Strategy pattern to instantiate the correct exporter for different file formats.
 * Supports CSV (.csv) and iCalendar (.ical/.ics) format exports.
 */
public class ExporterFactory {

  private ExporterFactory() {

  }

  /**
  * Creates and returns the appropriate exporter strategy based on the file extension
  * specified in the DTO's file name. This method parses the file name to
  * detect the extension and returns the correct strategy.
  *
  * @param data the export data transfer object containing the file name
  * @return an Iexport implementation appropriate for the specified file type
  * @throws Exception if the file name is invalid, missing an extension, or the file type
  */
  public static Iexport getExporter(ExportEventDto data) throws Exception {
    String fileName = data.getFileName();


    if (fileName == null || fileName.isEmpty()) {
      throw new Exception("Invalid file name.");
    }

    int lastDotIndex = fileName.lastIndexOf('.');


    if (lastDotIndex <= 0 || lastDotIndex == fileName.length() - 1) {
      throw new Exception("Invalid file name or missing extension.");
    }


    String extension = fileName.substring(lastDotIndex + 1);


    switch (extension.toLowerCase()) {
      case "csv":
        return new GoogleCsvExporter();

      case "ical":
      case "ics":
        return new IcalExporter();

      default:

        throw new Exception("Error: File type '." + extension + "' is not supported.");
    }
  }
}