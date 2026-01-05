package model.export;

import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.strategy.IcalExporter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * this class tests icalexporter logic.
 */
public class IcalExporterTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  private final IcalExporter exporter = new IcalExporter();
  private final ZoneId zone = ZoneId.of("UTC");

  /**
  * Tests escapeIcalString: Branch 1 (field == null).
  * We verify that a NULL location becomes an empty string in the file.
  */
  @Test
  public void testExportHandlesNullStrings() throws IOException {
    Event event = new EventBuilder()
                .setSubject("Null Checks")
                .setStartDateTime(ZonedDateTime.now(zone))
                .setEndDateTime(ZonedDateTime.now(zone).plusHours(1))
                .setLocation(null) // Triggers: if (field == null)
                .setDescription(null)
                .build();

    String content = exportAndReadFile(event);
    assertTrue(content.contains("LOCATION:\n") || content.contains("LOCATION:\r\n"));
    assertTrue(content.contains("DESCRIPTION:\n") || content.contains("DESCRIPTION:\r\n"));
  }

  /**
  * Tests escapeIcalString: Branch 2 (Replacements).
  * We verify that commas, semicolons, backslashes, and newlines are escaped.
  */
  @Test
  public void testExportEscapesSpecialCharacters() throws IOException {
    String trickySubject = "Hello, World; This has a \\ backslash\nAnd a newline";
    Event event = new EventBuilder()
                .setSubject(trickySubject)
                .setStartDateTime(ZonedDateTime.now(zone))
                .setEndDateTime(ZonedDateTime.now(zone).plusHours(1))
                .build();

    String content = exportAndReadFile(event);
    assertTrue("Comma should be escaped", content.contains("Hello\\, World"));
    assertTrue("Semicolon should be escaped", content.contains("World\\; This"));
    assertTrue("Backslash should be escaped", content.contains("a \\\\ backslash"));
    assertTrue("Newline should be escaped", content.contains("backslash\\nAnd"));
  }

  /**
  * Tests mapStatus: Branch 1 (eventStatus == null).
  * Expects default "CONFIRMED".
  */
  @Test
  public void testMapStatusNull() throws IOException {
    Event event = new EventBuilder()
                .setSubject("Status Null")
                .setStartDateTime(ZonedDateTime.now(zone))
                .setEndDateTime(ZonedDateTime.now(zone).plusHours(1))
                .setStatus(null) // Triggers: if (eventStatus == null)
                .build();

    String content = exportAndReadFile(event);
    assertTrue(content.contains("STATUS:CONFIRMED"));
  }

  /**
  * Tests mapStatus: Branch 2 & 3 (TENTATIVE, CANCELLED).
  * Also tests Case-Insensitivity (toUpperCase).
  */
  @Test
  public void testMapStatusValidTypes() throws IOException {
    Event eventTentative = new EventBuilder()
                .setSubject("Tentative")
                .setStartDateTime(ZonedDateTime.now(zone))
                .setEndDateTime(ZonedDateTime.now(zone).plusHours(1))
                .setStatus("Tentative") // Mixed case
                .build();

    Event eventCancelled = new EventBuilder()
                .setSubject("Cancelled")
                .setStartDateTime(ZonedDateTime.now(zone))
                .setEndDateTime(ZonedDateTime.now(zone).plusHours(1))
                .setStatus("CANCELLED")
                .build();
    String contentT = exportAndReadFile(eventTentative);
    String contentC = exportAndReadFile(eventCancelled);
    assertTrue(contentT.contains("STATUS:TENTATIVE"));
    assertTrue(contentC.contains("STATUS:CANCELLED"));
  }

  /**
  * Tests mapStatus: Default Branch (Unknown status).
  * Expects default "CONFIRMED".
  */
  @Test
  public void testMapStatusDefault() throws IOException {
    Event event = new EventBuilder()
                .setSubject("Weird Status")
                .setStartDateTime(ZonedDateTime.now(zone))
                .setEndDateTime(ZonedDateTime.now(zone).plusHours(1))
                .setStatus("IT_MIGHT_HAPPEN") // Unknown string
                .build();

    String content = exportAndReadFile(event);
    assertTrue(content.contains("STATUS:CONFIRMED"));
  }

  private String exportAndReadFile(Event event) throws IOException {
    String uniqueFileName = "test-" + java.util.UUID.randomUUID() + ".ics";
    File file = tempFolder.newFile(uniqueFileName);
    List<Event> events = Collections.singletonList(event);
    exporter.export(events, file.getAbsolutePath());
    return Files.readString(Path.of(file.getAbsolutePath()));
  }
}