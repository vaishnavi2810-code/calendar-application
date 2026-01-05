package factory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.factory.InputSourceFactory;
import calendar.interfacetypes.IinputSource;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for the InputSourceFactory class.
 * Verifies that the factory correctly creates input sources based on the mode
 * and handles invalid modes appropriately.
 */
public class InputSourceFactoryTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /**
  * Tests that the "interactive" mode returns a valid IinputSource.
  */
  @Test
  public void testCreateInteractiveInputSource() throws Exception {
    IinputSource source = InputSourceFactory.createInputSource("interactive", null);
    assertNotNull("Should return a valid input source instance", source);
    assertTrue("Result should implement IinputSource", source instanceof IinputSource);
  }

  /**
  * Tests that the "headless" mode returns a valid IinputSource.
  * Uses a temporary file to ensure file existence checks pass.
  */
  @Test
  public void testCreateHeadlessInputSource() throws Exception {
    File commandFile = tempFolder.newFile("commands.txt");
    IinputSource source = InputSourceFactory.createInputSource("headless",
                commandFile.getAbsolutePath());

    assertNotNull("Should return a valid input source instance", source);
    assertTrue("Result should implement IinputSource", source instanceof IinputSource);
  }

  /**
  * Tests that the mode string is case-insensitive (e.g., "INTERACTIVE" works).
  */
  @Test
  public void testCreateInputSourceCaseInsensitive() throws Exception {
    IinputSource source = InputSourceFactory.createInputSource("INTERACTIVE", null);
    assertNotNull("Should handle uppercase mode", source);
    File commandFile = tempFolder.newFile("mixed_case.txt");
    IinputSource source2 = InputSourceFactory.createInputSource("HeadLess",
                commandFile.getAbsolutePath());
    assertNotNull("Should handle mixed case mode", source2);
  }

  /**
  * Tests that providing an unknown mode throws an Exception with a helpful message.
  */
  @Test
  public void testCreateInvalidMode() {
    try {
      InputSourceFactory.createInputSource("invalid_mode", "file.txt");
      fail("Should have thrown exception for invalid mode");
    } catch (Exception e) {
      String message = e.getMessage();
      assertNotNull("Exception message should not be null", message);
      assertTrue("Exception message should contain input mode",
                    message.contains("invalid_mode"));
      assertTrue("Exception message should list 'interactive'",
                    message.contains("interactive"));
      assertTrue("Exception message should list 'headless'",
                    message.contains("headless"));
    }
  }
}