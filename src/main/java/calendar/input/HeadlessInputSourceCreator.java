package calendar.input;

import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IinputSourceCreator;
import java.io.IOException;

/**
 * Creator for headless input sources.
 * Implements the Strategy Pattern for creating file-based input.
 */
public class HeadlessInputSourceCreator implements IinputSourceCreator {

  @Override
  public IinputSource create(String filename) throws Exception {
    if (filename == null || filename.isEmpty()) {
      throw new Exception("Filename required for headless mode");
    }
    try {
      return new HeadlessInputSource(filename);
    } catch (IOException e) {
      throw new Exception("Failed to read commands file: " + e.getMessage());
    }
  }
}