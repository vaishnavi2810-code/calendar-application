package mocks;

import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.IresultDto;

/**
 * Mock command for testing.
 * Tracks execution and can simulate failures.
 */
public class MockCommand implements Icommand {

  private boolean wasExecuted = false;
  private Exception exceptionToThrow = null;
  private int executeCallCount = 0;
  private IresultDto resultToReturn = null;

  /**
   * Sets an exception to throw when executed.
   *
   * @param e the exception to throw, or null for no exception
   */
  public void setExceptionToThrow(Exception e) {
    this.exceptionToThrow = e;
  }

  /**
   * Sets the IResultDto this mock should return.
   *
   * @param result the DTO to return
   */
  public void setResultToReturn(IresultDto result) {
    this.resultToReturn = result;
  }

  @Override
  public IresultDto execute() throws Exception {
    wasExecuted = true;
    executeCallCount++;

    if (exceptionToThrow != null) {
      throw exceptionToThrow;
    }

    return this.resultToReturn;
  }

  /**
   * Checks if the command was executed.
   *
   * @return true if executed, false otherwise
   */
  public boolean wasExecuted() {
    return wasExecuted;
  }

  public int getExecuteCallCount() {
    return executeCallCount;
  }

  /**
   * Resets the execution state.
   */
  public void reset() {
    wasExecuted = false;
    executeCallCount = 0;
    exceptionToThrow = null;
    resultToReturn = null;
  }
}