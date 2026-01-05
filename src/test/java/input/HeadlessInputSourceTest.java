package input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.input.HeadlessInputSource;
import java.io.File;
import java.io.FileWriter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for HeadlessInputSource.
 */
public class HeadlessInputSourceTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testLoadCommandsFromFile() throws Exception {
    File testFile = tempFolder.newFile("test.txt");
    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write("command1\n");
      writer.write("command2\n");
      writer.write("command3\n");
    }

    HeadlessInputSource source = new HeadlessInputSource(testFile.getAbsolutePath());

    assertTrue(source.hasMoreCommands());
    assertEquals("command1", source.getNextCommand());
    assertEquals("command2", source.getNextCommand());
    assertEquals("command3", source.getNextCommand());
    assertFalse(source.hasMoreCommands());
  }

  @Test
  public void testSkipEmptyLines() throws Exception {
    File testFile = tempFolder.newFile("empty.txt");
    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write("command1\n");
      writer.write("\n");
      writer.write("  \n");
      writer.write("command2\n");
    }

    HeadlessInputSource source = new HeadlessInputSource(testFile.getAbsolutePath());

    assertEquals("command1", source.getNextCommand());
    assertEquals("command2", source.getNextCommand());
    assertFalse(source.hasMoreCommands());
  }

  @Test
  public void testSkipComments() throws Exception {
    File testFile = tempFolder.newFile("comments.txt");
    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write("# This is a comment\n");
      writer.write("command1\n");
      writer.write("# Another comment\n");
      writer.write("command2\n");
    }

    HeadlessInputSource source = new HeadlessInputSource(testFile.getAbsolutePath());

    assertEquals("command1", source.getNextCommand());
    assertEquals("command2", source.getNextCommand());
    assertFalse(source.hasMoreCommands());
  }

  @Test
  public void testGetNextCommandReturnsNullWhenEmpty() throws Exception {
    File testFile = tempFolder.newFile("empty.txt");

    HeadlessInputSource source = new HeadlessInputSource(testFile.getAbsolutePath());

    assertNull(source.getNextCommand());
    assertFalse(source.hasMoreCommands());
  }

  @Test(expected = Exception.class)
  public void testNonExistentFileThrowsException() throws Exception {
    new HeadlessInputSource("nonexistent.txt");
  }

  @Test
  public void testClose() throws Exception {
    File testFile = tempFolder.newFile("test.txt");
    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write("command1\n");
    }

    HeadlessInputSource source = new HeadlessInputSource(testFile.getAbsolutePath());
    source.close();

    assertFalse(source.hasMoreCommands());
  }
}