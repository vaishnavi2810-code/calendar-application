package input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.input.InteractiveInputSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for InteractiveInputSource.
 */
public class InteractiveInputSourceTest {

  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;

  /**
   * SETUP TEST.
   */
  @Before
  public void setUp() {
    originalOut = System.out;
    outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
  }

  /**
   * ok.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testGetNextCommandReturnsInput() {
    String input = "create event Meeting\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveInputSource source = new InteractiveInputSource();

    String command = source.getNextCommand();
    assertEquals("create event Meeting", command);

    String output = outputStream.toString();
    assertTrue("Should show prompt", output.contains("> "));
  }

  @Test
  public void testGetNextCommandTrimsWhitespace() {
    String input = "  create event Meeting  \n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveInputSource source = new InteractiveInputSource();

    String command = source.getNextCommand();
    assertEquals("create event Meeting", command);
  }

  @Test
  public void testHasMoreCommandsReturnsTrueWhenInputAvailable() {
    String input = "command1\ncommand2\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveInputSource source = new InteractiveInputSource();

    assertTrue(source.hasMoreCommands());
  }

  @Test
  public void testGetNextCommandReturnsNullWhenNoInput() {
    String input = "";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveInputSource source = new InteractiveInputSource();

    assertNull(source.getNextCommand());
  }

  @Test
  public void testMultipleCommands() {
    String input = "command1\ncommand2\ncommand3\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveInputSource source = new InteractiveInputSource();

    assertEquals("command1", source.getNextCommand());
    assertEquals("command2", source.getNextCommand());
    assertEquals("command3", source.getNextCommand());
  }

  @Test
  public void testClose() {
    String input = "command\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveInputSource source = new InteractiveInputSource();
    source.close();
  }
}