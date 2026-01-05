package calendar.input;

import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IinputSourceCreator;

/**
 * Creator for interactive input sources.
 * Implements the Strategy Pattern for creating interactive mode input.
 */
public class InteractiveInputSourceCreator implements IinputSourceCreator {

  @Override
  public IinputSource create(String filename) {
    return new InteractiveInputSource();
  }
}