package calendar.factory;

import calendar.input.HeadlessInputSourceCreator;
import calendar.input.InteractiveInputSourceCreator;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IinputSourceCreator;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating input source instances based on mode.
 * Uses Strategy pattern with creator objects to avoid switch statements.
 * Follows the Open/Closed Principle - can add new modes without modifying this class.
 */
public class InputSourceFactory {

  private static final Map<String, IinputSourceCreator> creators = new HashMap<>();

  static {
    creators.put("interactive", new InteractiveInputSourceCreator());
    creators.put("headless", new HeadlessInputSourceCreator());
  }

  /**
   * Creates an input source based on the specified mode.
   *
   * @param mode the mode ("interactive" or "headless")
   * @param filename the filename for headless mode (ignored for interactive)
   * @return the created input source
   * @throws Exception if the mode is invalid or input source creation fails
   */
  public static IinputSource createInputSource(String mode, String filename) throws Exception {
    String normalizedMode = mode.toLowerCase();

    IinputSourceCreator creator = creators.get(normalizedMode);
    if (creator == null) {
      throw new Exception("Invalid mode: " + mode + ". Available modes: "
          + String.join(", ", creators.keySet()));
    }

    return creator.create(filename);
  }
}