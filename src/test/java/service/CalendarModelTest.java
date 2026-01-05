package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.dto.CreateEventDto;
import calendar.dto.QueryEventDto;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.service.GuiDtoBuilderService;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CalendarModel.
 * Verifies the core service logic for managing calendars, events, and state consistency,
 * including exception handling for invalid operations and missing resources.
 */
public class CalendarModelTest {
  private CalendarModel service;
  private CalendarCollection repository;
  private GuiDtoBuilderService guiBuilder;

  /**
   * Initializes the test environment.
   * Creates a fresh repository, service, and DTO builder before each test execution
   * to ensure test isolation.
   */
  @Before
  public void setUp() {
    repository = new CalendarCollection();
    service = new CalendarModel(repository);
    guiBuilder = new GuiDtoBuilderService();
  }

  /**
   * Verifies that the active calendar name is correctly retrieved after being set.
   *
   * @throws Exception if creating or setting the calendar fails
   */
  @Test
  public void testGetActiveCalendar() throws Exception {
    String calName = "ActiveCal";
    service.createNewCalendar(calName, "UTC");
    service.setActiveCalendar(calName);
    String result = service.getActiveCalendar();
    assertEquals("Should return the correctly set active calendar name", calName, result);
  }

  /**
   * Verifies that attempting to create an event without an active calendar
   * throws the expected exception.
   */
  @Test
  public void testCreateEventNoActiveCalendarThrowsException() {
    LocalDateTime now = LocalDateTime.now();
    CreateEventDto dto = guiBuilder.buildTimedSingleEventDto("Test", now, now.plusHours(1));
    Exception exception = assertThrows(Exception.class, () -> {
      service.createEvent(dto);
    });
    assertEquals("Calendar not found.", exception.getMessage());
  }

  /**
   * Verifies exception handling when trying to edit an event in a calendar
   * that has been deleted after being set as active.
   *
   * @throws Exception if setup fails
   */
  @Test
  public void testEditEventCalendarDeletedAfterSelectionThrowsException() throws Exception {
    String calName = "ToBeDeleted";
    service.createNewCalendar(calName, "UTC");
    service.setActiveCalendar(calName);
    repository.deleteByName(calName);
    Exception exception = assertThrows(Exception.class, () -> {
      service.editEvent(null);
    });
    assertEquals("Calendar not found.", exception.getMessage());
  }

  /**
   * Verifies that querying events throws an exception if no calendar is currently selected.
   */
  @Test
  public void testQueryEventNoActiveCalendarThrowsException() {
    QueryEventDto dto = guiBuilder.buildQueryForDate(java.time.LocalDate.now());

    Exception exception = assertThrows(Exception.class, () -> {
      service.queryEvent(dto);
    });
    assertEquals("Calendar not found.", exception.getMessage());
  }

  /**
   * Verifies that attempting to rename a non-existent calendar throws the correct exception.
   */
  @Test
  public void testUpdateCalendarNameSourceNotFoundThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      service.updateCalendarName("NonExistent", "NewName");
    });
    assertEquals("Calendar not found.", exception.getMessage());
  }

  /**
   * Verifies that renaming a calendar to a name that already exists causes a
   * naming conflict exception.
   *
   * @throws Exception if creating the initial calendars fails
   */
  @Test
  public void testUpdateCalendarNameTargetNameExistsThrowsException() throws Exception {
    service.createNewCalendar("Cal1", "UTC");
    service.createNewCalendar("Cal2", "UTC");
    Exception exception = assertThrows(Exception.class, () -> {
      service.updateCalendarName("Cal1", "Cal2");
    });
    assertEquals("New name already exists.", exception.getMessage());
  }

  /**
   * Verifies the retrieval of all calendar names from the repository.
   * Checks that the set size and contents match the created calendars.
   *
   * @throws Exception if calendar creation fails
   */
  @Test
  public void testGetAllCalendarNames() throws Exception {
    service.createNewCalendar("Alpha", "UTC");
    service.createNewCalendar("Beta", "UTC");
    Set<String> names = service.getAllCalendarNames();
    assertNotNull(names);
    assertEquals(2, names.size());
    assertTrue(names.contains("Alpha"));
    assertTrue(names.contains("Beta"));
  }

  /**
   * Verifies that requesting the timezone of a non-existent calendar throws an exception.
   */
  @Test
  public void testGetCalendarTimezoneNotFoundThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> {
      service.getCalendarTimezone("GhostCalendar");
    });
    assertEquals("Calendar not found.", exception.getMessage());
  }
}