package command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.command.ExportEventCommand;
import calendar.controller.EventController;
import calendar.dto.ExportEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.service.CommandParserService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ExportEventCommand class.
 * This test verifies the command's ability to correctly
 * call the service and return an IResultDto.
 */
public class ExportEventCommandTest {

  private CalendarModel calendarModel;
  private Icalendarcollection repository;
  private EventController eventController;
  private CommandParserService commandParserService;

  /**
   * Sets up test fixtures before each test.
   * Initializes a real repository, service, and controller,
   * then creates and sets a default calendar.
   */
  @Before
  public void setUp() throws Exception {
    repository = new CalendarCollection();
    calendarModel = new CalendarModel(repository);
    commandParserService = new CommandParserService();
    Iview mockView = new calendar.test.MockView();
    IinputSource mockInputSource = new calendar.test.MockInputSource();
    eventController = new EventController(mockInputSource, calendarModel,
        commandParserService, mockView);
    eventController.processCommand("create calendar --name \"Test Calendar\""
        + " --timezone America/Los_Angeles");
    eventController.processCommand("use calendar --name \"Test Calendar\"");
  }

  /**
   * Cleans up test files.
   */
  @After
  public void tearDown() throws Exception {
    Files.deleteIfExists(Path.of("events.csv"));
    Files.deleteIfExists(Path.of("empty.csv"));
    Files.deleteIfExists(Path.of("data.csv"));
    Files.deleteIfExists(Path.of("data.ical"));
  }

  @Test
  public void testExecuteSuccessfulExport() throws Exception {
    String fileName = "events.csv";
    ExportEventDto dto = new ExportEventDto(fileName);
    ExportEventCommand command = new ExportEventCommand(dto, calendarModel);
    IresultDto result = command.execute();
    assertNotNull(result);
    assertTrue("Result should be a SimpleMessageDto",
            result instanceof SimpleMessageDto);

    String message = ((SimpleMessageDto) result).getMessage();
    assertTrue("Should show success message",
            message.contains("File successfully created at"));
    assertTrue("Output path should contain the filename",
            message.contains(fileName));

    Files.deleteIfExists(Path.of(fileName));
  }

  @Test
  public void testExecuteEmptyEventsSet() throws Exception {
    String fileName = "empty.csv";
    ExportEventDto dto = new ExportEventDto(fileName);
    ExportEventCommand command = new ExportEventCommand(dto, calendarModel);

    IresultDto result = command.execute();

    assertNotNull(result);
    assertTrue(result instanceof SimpleMessageDto);
    String message = ((SimpleMessageDto) result).getMessage();
    assertTrue("Should show success even with empty events",
            message.contains("File successfully created at"));
    assertTrue("Output path should contain the filename",
            message.contains(fileName));

    Files.deleteIfExists(Path.of(fileName));
  }

  @Test
  public void testExecuteVariousFileExtensions() throws Exception {
    testFileExtension("data.csv");
    testFileExtension("data.ical");
  }

  private void testFileExtension(String fileName) throws Exception {
    ExportEventDto dto = new ExportEventDto(fileName);
    ExportEventCommand command = new ExportEventCommand(dto, calendarModel);

    IresultDto result = command.execute();
    String message = ((SimpleMessageDto) result).getMessage();

    assertTrue("Should handle " + fileName,
            message.contains("File successfully created at"));
    assertTrue("Output path should contain the filename",
            message.contains(fileName));

    Files.deleteIfExists(Path.of(fileName));
  }

  @Test(expected = Exception.class)
  public void testExecuteUnsupportedFileExtension() throws Exception {
    String fileName = "data.txt";
    ExportEventDto dto = new ExportEventDto(fileName);
    ExportEventCommand command = new ExportEventCommand(dto, calendarModel);

    try {
      command.execute();
    } catch (Exception e) {
      assertTrue("Should show error for unsupported type",
              e.getMessage().contains("File type '.txt' is not supported."));
      throw e;
    } finally {
      Files.deleteIfExists(Path.of(fileName));
    }
  }
}