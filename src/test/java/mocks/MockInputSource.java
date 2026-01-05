package calendar.test;

import calendar.interfacetypes.IinputSource;

/**
 * Mock input source for testing.
 * This is a dummy implementation that does nothing.
 * Used when testing components that require an IinputSource but don't actually use it.
 */
public class MockInputSource implements IinputSource {

  @Override
  public String getNextCommand() {
    return null;
  }

  @Override
  public boolean hasMoreCommands() {
    return false;
  }

  @Override
  public void close() {
    // Nothing to close
  }
}