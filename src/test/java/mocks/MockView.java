package calendar.test;

import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;

/**
 * Mock View implementation for testing.
 * This view does nothing - it just satisfies the Iview interface for tests.
 */
public class MockView implements Iview {

  @Override
  public void display(String message) {
    // Do nothing in tests
  }

  @Override
  public void displayError(String errorMessage) {
    // Do nothing in tests
  }

  @Override
  public void displayResult(IresultDto result) {
    // Do nothing in tests
  }
}