package view;

import static org.junit.Assert.assertTrue;

import calendar.dto.SimpleMessageDto;
import calendar.view.ConsoleView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ConsoleView.
 * Verifies that messages and errors are correctly written to the
 * standard output and error streams.
 */
public class ConsoleViewTest {

  private ByteArrayOutputStream outputStream;
  private ByteArrayOutputStream errorStream;
  private PrintStream originalOut;
  private PrintStream originalErr;

  /**
   * Sets up the test environment by capturing System.out and System.err.
   * Redirects standard output and error streams to byte array streams for verification.
   */
  @Before
  public void setUp() {
    originalOut = System.out;
    originalErr = System.err;
    outputStream = new ByteArrayOutputStream();
    errorStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    System.setErr(new PrintStream(errorStream));
  }

  /**
   * Restores the original System.out and System.err streams after each test.
   * Ensures that other tests or system components are not affected by the stream redirection.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  /**
   * Verifies that a standard text message is correctly written to the standard output.
   */
  @Test
  public void testDisplayMessage() {
    ConsoleView view = new ConsoleView();
    view.display("Test message");

    String output = outputStream.toString();
    assertTrue(output.contains("Test message"));
  }

  /**
   * Verifies that passing a null message results in no output.
   */
  @Test
  public void testDisplayNullMessage() {
    ConsoleView view = new ConsoleView();
    view.display(null);

    String output = outputStream.toString();
    assertTrue(output.isEmpty());
  }

  /**
   * Verifies that passing an empty string results in no output.
   */
  @Test
  public void testDisplayEmptyMessage() {
    ConsoleView view = new ConsoleView();
    view.display("");

    String output = outputStream.toString();
    assertTrue(output.isEmpty());
  }

  /**
   * Verifies that error messages are correctly written to the standard error stream.
   * Checks that the output contains the expected error formatting.
   */
  @Test
  public void testDisplayError() {
    ConsoleView view = new ConsoleView();
    view.displayError("Error message");

    String errors = errorStream.toString();
    assertTrue(errors.contains("Error: Error message"));
  }

  /**
   * Verifies that the content of a SimpleMessageDto is correctly displayed via the view.
   */
  @Test
  public void testDisplayResult() {
    ConsoleView view = new ConsoleView();
    SimpleMessageDto dto = new SimpleMessageDto("Command successful");
    view.displayResult(dto);

    String output = outputStream.toString();
    assertTrue(output.contains("Command successful"));
  }
}