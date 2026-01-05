package input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.input.HeadlessInputSourceCreator;
import calendar.interfacetypes.IinputSource;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * headlessinputsource creator.
 */
public class HeadlessInputSourceCreatorTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private HeadlessInputSourceCreator creator;

  /**
  * setup.
  */
  @Before
  public void setUp() {
    creator = new HeadlessInputSourceCreator();
  }

  @Test
  public void testCreateWithNullFilenameThrowsException() {
    Exception ex = assertThrows(Exception.class, () -> {
      creator.create(null);
    });
    assertEquals("Filename required for headless mode", ex.getMessage());
  }

  @Test
  public void testCreateWithEmptyFilenameThrowsException() {
    Exception ex = assertThrows(Exception.class, () -> {
      creator.create("");
    });
    assertEquals("Filename required for headless mode", ex.getMessage());
  }

  @Test
  public void testCreateWithValidFile() throws Exception {
    File testFile = tempFolder.newFile("valid.txt");
    IinputSource source = creator.create(testFile.getAbsolutePath());
    assertNotNull("Should return a valid source object", source);
  }

  @Test
  public void testCreateWithMissingFileThrowsWrappedException() {
    Exception ex = assertThrows(Exception.class, () -> {
      creator.create("non_existent_file.txt");
    });
    assertTrue(ex.getMessage().contains("Failed to read commands file"));
  }
}