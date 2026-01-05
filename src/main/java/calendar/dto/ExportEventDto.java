package calendar.dto;

/**
 * Data Transfer Object for exporting calendar events to a file.
 * Encapsulates the target file name and provides utility methods to extract
 * file type information from the file name extension.
 */
public class ExportEventDto {
  private final String fileName;

  /**
   * Creates an ExportEventDto with the specified file name.
   *
   * @param fileName the name of the target export file including extension
   */
  public ExportEventDto(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  /**
   * Extracts and returns the file type from the file name by parsing the extension.
   * The extension is converted to lowercase. If no extension is found (no dot in the
   * file name or dot is at the beginning), returns an empty string.
   *
   * @return the file extension in lowercase, or an empty string if no extension exists
   */
  public String getFileType() {
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex > 0) ? fileName.substring(dotIndex + 1).toLowerCase() : "";
  }
}