package model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.dto.CreateEventDto;
import calendar.dto.ExportEventDto;
import calendar.interfacetypes.Icreate;
import calendar.interfacetypes.Iexport;
import calendar.model.Event;
import calendar.strategy.CreateEventSingle;
import calendar.strategy.GoogleCsvExporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for GoogleCsvExporter and ExportEventDto.
 * Verifies CSV file creation, proper formatting of dates/times,
 * correct handling of special characters, Google Calendar compatibility,
 * and ExportEventDto file type detection.
 */
public class GoogleCsvExporterTest {

  private Iexport exporter;
  private Set<Event> testEvents;
  private String testFileName;

  /**
   * Sets up test fixtures before each test.
   * Initializes the exporter, test events collection, and test filename.
   */
  @Before
  public void setUp() {
    exporter = new GoogleCsvExporter();
    testEvents = new HashSet<>();
    testFileName = "test_export.csv";
  }

  /**
   * Cleans up test files after each test.
   * Deletes the test CSV file if it exists.
   *
   * @throws IOException if file deletion fails
   */
  @After
  public void tearDown() throws IOException {
    Path filePath = Paths.get(testFileName);
    if (Files.exists(filePath)) {
      Files.delete(filePath);
    }
  }

  @Test
  public void testExportDtoGetFileTypeCsv() {
    ExportEventDto dto = new ExportEventDto("events.csv");
    assertEquals("File type should be 'csv'", "csv", dto.getFileType());
    System.out.println("✓ TEST PASSED: ExportEventDto correctly identifies CSV type");
  }

  @Test
  public void testExportDtoGetFileTypeUppercase() {
    ExportEventDto dto = new ExportEventDto("events.CSV");
    assertEquals("File type should be lowercase 'csv'", "csv", dto.getFileType());
    System.out.println("✓ TEST PASSED: ExportEventDto converts extension to lowercase");
  }

  @Test
  public void testExportDtoNoExtension() {
    ExportEventDto dto = new ExportEventDto("events");
    assertEquals("File type should be empty string", "", dto.getFileType());
    System.out.println("✓ TEST PASSED: ExportEventDto handles missing extension");
  }

  @Test
  public void testExportDtoDotAtBeginning() {
    ExportEventDto dto = new ExportEventDto(".hiddenfile");
    assertEquals("File type should be empty string", "", dto.getFileType());
    System.out.println("✓ TEST PASSED: ExportEventDto handles hidden files");
  }

  @Test
  public void testExportDtoMultipleDots() {
    ExportEventDto dto = new ExportEventDto("my.events.data.csv");
    assertEquals("File type should be 'csv'", "csv", dto.getFileType());
    System.out.println("✓ TEST PASSED: ExportEventDto extracts "
        +
        "correct extension with multiple dots");
  }

  @Test
  public void testExportDtoGetFileName() {
    String fileName = "calendar_export_2025.csv";
    ExportEventDto dto = new ExportEventDto(fileName);
    assertEquals("File name should match", fileName, dto.getFileName());
    System.out.println("✓ TEST PASSED: ExportEventDto returns correct filename");
  }

  @Test
  public void testExportSingleEvent() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto createDto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Team Meeting",
        "2025-11-22T10:00",
        "2025-11-22T11:00"
    );
    testEvents.addAll(createService.create(createDto, testEvents,
            ZoneId.of("America/Los_Angeles")));

    ExportEventDto exportDto = new ExportEventDto(testFileName);
    assertEquals("Should identify CSV type", "csv", exportDto.getFileType());

    String filePath = exporter.export(testEvents, exportDto.getFileName());

    assertNotNull("File path should not be null", filePath);
    assertTrue("File should exist", Files.exists(Paths.get(filePath)));

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    assertEquals("Should have header + 1 event row", 2, lines.size());

    String header = lines.get(0);
    assertTrue("Header should contain 'Subject'", header.contains("Subject"));
    assertTrue("Header should contain 'Start Date'", header.contains("Start Date"));

    String eventRow = lines.get(1);
    assertTrue("Event row should contain subject", eventRow.contains("Team Meeting"));
    assertTrue("Event row should contain date", eventRow.contains("11/22/2025"));

    System.out.println("✓ TEST PASSED: Single event exported successfully");
    System.out.println("  File: " + filePath);
    System.out.println("  File Type: " + exportDto.getFileType());
  }

  @Test
  public void testExportMultipleEvents() throws Exception {
    Icreate createService = new CreateEventSingle();

    CreateEventDto dto1 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Morning Meeting",
        "2025-11-22T09:00",
        "2025-11-22T10:00"
    );
    testEvents.addAll(createService.create(dto1, testEvents, ZoneId.of("America/Los_Angeles")));

    CreateEventDto dto2 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Lunch",
        "2025-11-22T12:00",
        "2025-11-22T13:00"
    );
    testEvents.addAll(createService.create(dto2, new HashSet<>(testEvents),
            ZoneId.of("America/Los_Angeles")));

    CreateEventDto dto3 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Review Session",
        "2025-11-22T15:00",
        "2025-11-22T16:00"
    );
    testEvents.addAll(createService.create(dto3, new HashSet<>(testEvents),
            ZoneId.of("America/Los_Angeles")));

    ExportEventDto exportDto = new ExportEventDto("multi_event_export.csv");
    String filePath = exporter.export(testEvents, exportDto.getFileName());

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    assertEquals("Should have header + 3 event rows", 4, lines.size());

    Files.deleteIfExists(Paths.get("multi_event_export.csv"));

    System.out.println("✓ TEST PASSED: Multiple events exported");
    System.out.println("  Total events: " + testEvents.size());
    System.out.println("  CSV rows: " + (lines.size() - 1));
  }

  @Test
  public void testExportSubjectWithComma() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Meeting, Review, Planning",
        "2025-11-22T10:00",
        "2025-11-22T11:00"
    );
    testEvents.addAll(createService.create(dto, testEvents, ZoneId.of("America/Los_Angeles")));

    String filePath = exporter.export(testEvents, testFileName);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventRow = lines.get(1);

    assertTrue("Subject with comma should be in quotes",
        eventRow.startsWith("\"Meeting, Review, Planning\""));

    System.out.println("✓ TEST PASSED: Subject with comma properly escaped");
    System.out.println("  Row: " + eventRow);
  }

  @Test
  public void testExportSubjectWithQuotes() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "The \"Big\" Meeting",
        "2025-11-22T10:00",
        "2025-11-22T11:00"
    );
    testEvents.addAll(createService.create(dto, testEvents, ZoneId.of("America/Los_Angeles")));

    String filePath = exporter.export(testEvents, testFileName);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventRow = lines.get(1);

    assertTrue("Quotes should be escaped", eventRow.contains("\"\""));
    assertTrue("Field should be quoted", eventRow.startsWith("\""));

    System.out.println("✓ TEST PASSED: Subject with quotes properly escaped");
    System.out.println("  Row: " + eventRow);
  }

  @Test
  public void testExportEmptyCollection() throws Exception {
    ExportEventDto exportDto = new ExportEventDto(testFileName);
    String filePath = exporter.export(testEvents, exportDto.getFileName());

    assertTrue("File should exist", Files.exists(Paths.get(filePath)));

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    assertEquals("Should have only header row", 1, lines.size());

    System.out.println("✓ TEST PASSED: Empty collection exported (header only)");
  }

  @Test
  public void testDateTimeFormatting() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Format Test",
        "2025-11-22T14:30",
        "2025-11-22T15:45"
    );
    testEvents.addAll(createService.create(dto, testEvents, ZoneId.of("America/Los_Angeles")));

    String filePath = exporter.export(testEvents, testFileName);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventRow = lines.get(1);

    String[] fields = eventRow.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    assertTrue("Start date should be in MM/DD/YYYY format",
        fields[1].trim().matches("\\d{2}/\\d{2}/\\d{4}"));
    assertTrue("Start time should contain AM or PM",
        fields[2].trim().matches(".*[AP]M.*"));

    System.out.println("✓ TEST PASSED: Date/time formatting correct");
    System.out.println("  Start Date: " + fields[1].trim());
    System.out.println("  Start Time: " + fields[2].trim());
  }

  @Test
  public void testExportReturnsAbsolutePath() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Path Test",
        "2025-11-22T10:00",
        "2025-11-22T11:00"
    );
    testEvents.addAll(createService.create(dto, testEvents, ZoneId.of("America/Los_Angeles")));

    ExportEventDto exportDto = new ExportEventDto(testFileName);
    String filePath = exporter.export(testEvents, exportDto.getFileName());

    Path path = Paths.get(filePath);
    assertTrue("Returned path should be absolute", path.isAbsolute());

    System.out.println("✓ TEST PASSED: Absolute path returned");
    System.out.println("  Path: " + filePath);
  }

  @Test
  public void testCsvStructureIntegrity() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Structure Test",
        "2025-11-22T10:00",
        "2025-11-22T11:00"
    );
    testEvents.addAll(createService.create(dto, testEvents, ZoneId.of("America/Los_Angeles")));
    String filePath = exporter.export(testEvents, testFileName);
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String header = lines.get(0);
    int headerColumns = header.split(",").length;
    String eventRow = lines.get(1);
    int eventColumns = eventRow.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").length;
    assertEquals("Header and event row should have same number of columns",
        headerColumns, eventColumns);
    System.out.println("✓ TEST PASSED: CSV structure is valid");
    System.out.println("  Columns: " + headerColumns);
  }

  @Test
  public void testIntegrationExportDtoWithExporter() throws Exception {
    Icreate createService = new CreateEventSingle();
    CreateEventDto createDto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Integration Test",
        "2025-11-22T10:00",
        "2025-11-22T11:00"
    );
    testEvents.addAll(createService.create(createDto, testEvents,
            ZoneId.of("America/Los_Angeles")));
    ExportEventDto exportDto = new ExportEventDto("integration_test.csv");
    assertEquals("Should detect CSV type", "csv", exportDto.getFileType());

    String filePath = exporter.export(testEvents, exportDto.getFileName());
    assertTrue("File should exist", Files.exists(Paths.get(filePath)));

    Files.deleteIfExists(Paths.get("integration_test.csv"));

    System.out.println("✓ TEST PASSED: ExportEventDto + GoogleCsvExporter integration");
    System.out.println("  DTO File: " + exportDto.getFileName());
    System.out.println("  DTO Type: " + exportDto.getFileType());
    System.out.println("  Export Path: " + filePath);
  }
}
