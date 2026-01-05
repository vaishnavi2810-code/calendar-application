import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests CalendarRunner and CalendarApp functionality.
 * Verifies command-line argument parsing, mode selection, and error handling.
 */
public class CalendarTest {

  private ByteArrayOutputStream outputStream;
  private ByteArrayOutputStream errorStream;
  private PrintStream originalOut;
  private PrintStream originalErr;
  private InputStream originalSystemIn;


  /**
   * Sets up test fixtures before each test.
   * Captures System.out, System.err, and System.in for testing.
   */
  @Before
  public void setUp() {
    originalOut = System.out;
    originalErr = System.err;
    originalSystemIn = System.in;
    outputStream = new ByteArrayOutputStream();
    errorStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    System.setErr(new PrintStream(errorStream));
  }

  /**
   * Restores System.out and System.err after each test.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void testMainOneArgument() {
    String[] args = {"--mode"};
    CalendarRunner.main(args);
    String error = errorStream.toString();
    assertTrue("Should show missing arguments error",
        error.contains("Missing arguments"));
    assertTrue("Should show usage",
        error.contains("Usage:"));
  }

  @Test
  public void testMainWrongFirstArgument() {
    String[] args = {"mode", "interactive"};
    CalendarRunner.main(args);

    String error = errorStream.toString();
    assertTrue("Should show --mode error",
        error.contains("First argument must be --mode"));
    assertTrue("Should show usage",
        error.contains("Usage:"));
  }

  @Test
  public void testMainInteractiveModeExtraArgs() {
    String[] args = {"--mode", "interactive", "extra"};
    CalendarRunner.main(args);

    String error = errorStream.toString();
    assertTrue("Should show error about extra arguments",
        error.contains("Interactive mode takes no additional arguments"));
    assertTrue("Should show usage",
        error.contains("Usage:"));
  }


  @Test
  public void testMainHeadlessModeSuccess() {
    String[] args = {"--mode", "headless", "commands.txt"};
    CalendarRunner.main(args);
    String output = outputStream.toString();
    assertTrue("Should show starting message with filename",
        output.contains("Starting in headless mode with file: commands.txt"));
  }

  @Test
  public void testMainHeadlessModeMissingFilename() {
    String[] args = {"--mode", "headless"};
    CalendarRunner.main(args);

    String error = errorStream.toString();
    assertTrue("Should show error about missing filename",
        error.contains("Headless mode requires exactly one filename argument"));
    assertTrue("Should show usage",
        error.contains("Usage:"));
  }

  @Test
  public void testMainHeadlessModeExtraArgs() {
    String[] args = {"--mode", "headless", "file1.txt", "file2.txt"};
    CalendarRunner.main(args);

    String error = errorStream.toString();
    assertTrue("Should show error about extra arguments",
        error.contains("Headless mode requires exactly one filename argument"));
    assertTrue("Should show usage",
        error.contains("Usage:"));
  }


  @Test
  public void testMainInvalidMode() {
    String[] args = {"--mode", "batch"};
    CalendarRunner.main(args);

    String error = errorStream.toString();
    assertTrue("Should show invalid mode error",
        error.contains("Invalid mode"));
    assertTrue("Should show usage",
        error.contains("Usage:"));
  }


  @Test
  public void testMainHeadlessModeMixedCase() {
    String[] args = {"--mode", "HeAdLeSs", "test.txt"};

    CalendarRunner.main(args);

    String output = outputStream.toString();
    assertTrue("Should handle mixed case HeAdLeSs",
                output.contains("Starting in headless mode with file: test.txt"));
  }

  @Test
  public void testInteractiveModeStartsSuccessfully() {
    String simulatedInput = "exit\n";
    InputStream fakeIn = new ByteArrayInputStream(simulatedInput.getBytes());
    System.setIn(fakeIn);
    String[] args = {"--mode", "interactive"};
    CalendarRunner.main(args);
    String output = outputStream.toString();
    assertTrue(output.contains("Starting in interactive mode..."));
    assertTrue(output.contains("Welcome to MVCalendar!"));
    assertTrue(output.contains("Goodbye!")); // Confirms the app exited
  }
}