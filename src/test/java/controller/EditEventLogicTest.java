package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.SimpleGuiController;
import calendar.dto.CreateEventDto;
import calendar.dto.QueryResultDto;
import calendar.interfacetypes.IeditEventDialogData;
import calendar.interfacetypes.IguiViewCalendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.service.GuiDtoBuilderService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for SimpleGuiController.
 * Covers all public methods including Calendar management and Event editing.
 */
public class EditEventLogicTest {

  private SimpleGuiController controller;
  private CalendarModel service;
  private TestGuiView mockView;
  private GuiDtoBuilderService guiBuilder;

  /**
   * Sets up the test environment before each test case is executed.
   *
   * <p>This method initializes fresh instances of the core components used
   * by the GUI controller tests:
   * <ul>
   *   <li>{@code CalendarService} backed by an {@code InMemoryCalendarRepository}
   *       to provide isolated, non-persistent test data.</li>
   *   <li>{@code TestGuiView} as a mock GUI view implementation for verifying
   *       controller-to-view interactions without rendering actual UI.</li>
   *   <li>{@code GuiBuilderService} used by components that construct GUI
   *       elements during the tests.</li>
   *   <li>{@code SimpleGuiController}, the controller under test, wired with
   *       the initialized service and mock view.</li>
   * </ul>
   *
   * <p>This method runs automatically before every {@code @Test}, ensuring a clean,
   * deterministic environment for each test case.
   *
   * @throws Exception if any component required for test setup fails to initialize
   */
  @Before
  public void setUp() throws Exception {
    service = new CalendarModel(new CalendarCollection());
    mockView = new TestGuiView();
    guiBuilder = new GuiDtoBuilderService();
    controller = new SimpleGuiController(service, mockView);
  }

  @Test
  public void testInitializationCreatesDefaultCalendar() throws Exception {
    assertTrue("Should have created Default calendar", service.checkCalendarModel("Default"));
    assertEquals("Active calendar should be Default", "Default", service.getActiveCalendar());
    assertEquals("Status should be updated",
            "Active Calendar: Default",
            mockView.lastStatusMessage);
  }

  @Test
  public void testCreateCalendar() throws Exception {
    controller.createCalendar("Work", "UTC");
    assertNull("Should not have error", mockView.lastErrorMessage);
    assertTrue("Service should have Work calendar", service.checkCalendarModel("Work"));
    assertEquals("Work", service.getActiveCalendar());
    assertEquals("Active calendar: Work", mockView.lastStatusMessage);
  }

  @Test
  public void testCreateCalendarErrorHandling() {
    controller.createCalendar("", "UTC");
    assertNotNull("Should have shown error for empty name", mockView.lastErrorMessage);
    assertTrue(mockView.lastErrorMessage.contains("empty"));
  }

  @Test
  public void testEditCalendarName() throws Exception {
    controller.createCalendar("OldName", "UTC");
    controller.editCalendarName("OldName", "NewName");
    assertTrue("NewName should exist", service.checkCalendarModel("NewName"));
    assertEquals("NewName", service.getActiveCalendar());
    assertEquals("Active calendar: NewName", mockView.lastStatusMessage);
  }

  @Test
  public void testEditCalendarTimezoneFailure() throws Exception {
    controller.createCalendar("TestZone", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 24, 23, 0);
    LocalDateTime end = start.plusHours(1);
    CreateEventDto dto = guiBuilder.buildTimedSingleEventDto("Late", start, end);
    service.createEvent(dto);
    controller.editCalendarTimezone("TestZone", "Asia/Tokyo");
    assertNotNull("Should have shown error", mockView.lastErrorMessage);
    assertTrue("Error should mention day shift",
            mockView.lastErrorMessage.contains("Timezone change would cause event"));
  }

  @Test
  public void testEditCalendarTimezone() throws Exception {
    controller.createCalendar("Travel", "UTC");
    controller.editCalendarTimezone("Travel", "America/New_York");
    assertEquals("America/New_York", service.getCalendarTimezone("Travel"));
    assertEquals("Calendar timezone updated!", mockView.lastStatusMessage);
  }

  @Test
  public void testEditCalendarNameFailure() throws Exception {
    controller.createCalendar("A", "UTC");
    controller.createCalendar("B", "UTC");
    mockView.lastErrorMessage = null;
    controller.editCalendarName("A", "B");
    assertNotNull("Should show error", mockView.lastErrorMessage);
  }


  @Test
  public void testHandleEditEventScopeForward() throws Exception {
    LocalDate today = LocalDate.now().with(java.time.temporal
            .TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    LocalDateTime start = today.atTime(10, 0);
    LocalDateTime end = today.atTime(11, 0);
    CreateEventDto createDto = guiBuilder.buildTimedRecurringForDto(
            "Daily Standup", start, end,
            Set.of(DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY), 3);
    service.createEvent(createDto);
    LocalDate tuesday = today.plusDays(1);
    Event tuesdayEvent = service.queryEvent(guiBuilder
                    .buildQueryForDate(tuesday))
            .iterator().next();

    TestEditDialogData mockDialog = new TestEditDialogData(tuesdayEvent);
    mockDialog.subject = "New Standup Time";
    mockDialog.scope = "future";
    controller.handleEditEvent(mockDialog);
    Event mondayCheck = service.queryEvent(guiBuilder
                    .buildQueryForDate(today))
                    .iterator()
            .next();
    assertEquals("Daily Standup", mondayCheck.getSubject());
    Event tuesdayCheck = service.queryEvent(guiBuilder
                    .buildQueryForDate(tuesday))
            .iterator()
            .next();
    assertEquals("New Standup Time", tuesdayCheck.getSubject());
    LocalDate wednesday = today.plusDays(2);
    Event wednesdayCheck = service.queryEvent(guiBuilder
                    .buildQueryForDate(wednesday))
                    .iterator()
            .next();
    assertEquals("New Standup Time", wednesdayCheck.getSubject());
  }

  @Test
  public void testHandleEditEventValidationEndTimeBeforeStartTime() throws Exception {
    controller.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.now().plusHours(1);
    LocalDateTime end = start.plusHours(1);
    CreateEventDto createDto = guiBuilder.buildTimedSingleEventDto("Meeting", start, end);
    service.createEvent(createDto);
    Event originalEvent = service.queryEvent(guiBuilder
                    .buildQueryForDate(start.toLocalDate()))
            .iterator()
            .next();
    TestEditDialogData mockDialog = new TestEditDialogData(originalEvent);
    mockDialog.startTime = LocalTime.of(12, 0);
    mockDialog.endTime = LocalTime.of(11, 0);
    controller.handleEditEvent(mockDialog);
    assertNotNull("Should show error", mockView.lastErrorMessage);
    assertTrue("Error should be about end time",
            mockView.lastErrorMessage.contains("End time must be after start time"));
  }

  @Test
  public void testSwitchCalendar() throws Exception {
    service.createNewCalendar("Personal", "UTC");
    controller.switchCalendar("Personal");
    assertEquals("Personal", service.getActiveCalendar());
    assertEquals("Active calendar: Personal", mockView.lastStatusMessage);
  }

  @Test
  public void testRefreshCalendarList() throws Exception {
    service.createNewCalendar("Secret", "UTC");
    controller.refreshCalendarList();
    assertNotNull("Calendar list should be updated",
            mockView.lastCalendarList);
    assertTrue("Dropdown should contain newly added calendar",
        mockView.lastCalendarList.contains("Secret"));
  }

  @Test
  public void testLoadEventsForDate() throws Exception {
    controller.createCalendar("TestCal", "UTC");
    LocalDate testDate = LocalDate.now().plusDays(5);
    controller.loadEventsForDate(testDate);
    assertNotNull("Should have queried events", mockView.lastQueryResult);
    assertEquals(0, mockView.lastQueryResult.getEvents().size());
  }


  @Test
  public void testHandleEditEventDateChange() throws Exception {
    controller.createCalendar("Work", "UTC");
    LocalDate today = LocalDate.now();
    LocalDateTime start = today.atTime(10, 0);
    LocalDateTime end = today.atTime(11, 0);
    CreateEventDto createDto = guiBuilder.buildTimedSingleEventDto("MoveMe", start, end);
    service.createEvent(createDto);
    Set<Event> events = service.queryEvent(guiBuilder.buildQueryForDate(today));
    Event originalEvent = events.iterator().next();
    TestEditDialogData mockDialog = new TestEditDialogData(originalEvent);
    mockDialog.startDate = today.plusDays(1);
    mockDialog.endDate = today.plusDays(1);
    controller.handleEditEvent(mockDialog);
    Set<Event> eventsToday = service.queryEvent(guiBuilder.buildQueryForDate(today));
    assertEquals(0, eventsToday.size());
    Set<Event> eventsTomorrow = service.queryEvent(
        guiBuilder.buildQueryForDate(today.plusDays(1)));
    assertEquals(1, eventsTomorrow.size());
    assertEquals("MoveMe", eventsTomorrow.iterator().next().getSubject());
  }

  @Test
  public void testDropdownListenerTriggersSwitchCalendar() throws Exception {
    controller.createCalendar("Work", "UTC");
    assertNotNull("Dropdown listener should be registered", mockView.dropdownListener);
    mockView.selectedDropdownItem = "Work";
    mockView.dropdownListener.actionPerformed(
             new ActionEvent(mockView.getCalendarDropdown(), 1, "select"));

    assertEquals("Work", service.getActiveCalendar());
    assertEquals("Active calendar: Work", mockView.lastStatusMessage);
  }

  @Test
  public void testDateSelectionListenerTriggersLoadEvents() {
    LocalDate testDate = LocalDate.of(2025, 12, 1);
    assertNotNull("Date selection listener should be registered", mockView.dateSelectionListener);
    mockView.dateSelectionListener.accept(testDate);
    assertEquals(testDate, mockView.lastUpdatedDate);
    assertNotNull("Should attempt to display events", mockView.lastQueryResult);
  }

  @Test
  public void testSwitchCalendarFailure() {
    controller.switchCalendar("GhostCalendar");
    assertNotNull("Should have shown error message", mockView.lastErrorMessage);
    assertTrue("Error should be about failure to switch",
            mockView.lastErrorMessage.contains("Failed to switch calendar"));
  }

  /**
  * Test view implementing interface - no JFrame created.
  */
  private static class TestGuiView implements IguiViewCalendar {

    String lastErrorMessage;
    String lastSuccessMessage;
    String lastStatusMessage;
    QueryResultDto lastQueryResult;
    LocalDate lastUpdatedDate;
    YearMonth lastUpdatedMonth;
    List<String> lastCalendarList;
    String activeCalendar;
    ActionListener dropdownListener;
    Consumer<LocalDate> dateSelectionListener;
    String selectedDropdownItem;

    private final JComboBox<String> calendarDropdown = new JComboBox<String>() {

      @Override
      public void addActionListener(ActionListener l) {
        dropdownListener = l;
      }

      @Override
      public Object getSelectedItem() {
        return selectedDropdownItem;
      }
    };

    private final JButton dummyButton = new JButton();

    @Override
    public void showError(String message) {
      this.lastErrorMessage = message;
    }

    @Override
    public void showSuccess(String message) {
      this.lastSuccessMessage = message;
    }

    @Override
    public void setStatus(String message) {
      this.lastStatusMessage = message;
    }

    @Override
    public void updateCalendarList(List<String> names, String active) {
      this.lastCalendarList = names != null ? new ArrayList<>(names) : null;
      this.activeCalendar = active;
      this.selectedDropdownItem = active;
      if (active != null) {
        this.lastStatusMessage = "Active Calendar: " + active;
      }
    }

    @Override
    public void updateCalendarView(YearMonth month, LocalDate date) {
      this.lastUpdatedMonth = month;
      this.lastUpdatedDate = date;
    }

    @Override
    public void displayEventsForQueryResult(LocalDate date, QueryResultDto result) {
      this.lastQueryResult = result;
    }

    @Override
    public LocalDate getSelectedDate() {
      return lastUpdatedDate != null ? lastUpdatedDate : LocalDate.now();
    }

    @Override
    public JComboBox<String> getCalendarDropdown() {
      return calendarDropdown;
    }

    @Override
    public JButton getCreateCalendarButton() {
      return dummyButton;
    }

    @Override
    public JButton getEditCalendarButton() {
      return dummyButton;
    }

    @Override
    public JButton getCreateEventButton() {
      return dummyButton;
    }

    @Override
    public JButton getEditEventButton() {
      return dummyButton;
    }


    @Override
    public void addNavigationListeners(ActionListener prev, ActionListener next,
                                           ActionListener today) {}

    @Override
    public void setDateSelectionListener(Consumer<LocalDate> listener) {
      this.dateSelectionListener = listener;
    }

    @Override
    public JButton getEditEventsBySearchButton() {
      return dummyButton;
    }

    @Override
    public JFrame getFrame() {
      return null;
    }
  }

  /**
  * Test edit dialog data - no JDialog created.
  */
  private static class TestEditDialogData implements IeditEventDialogData {

    private final Event originalEvent;
    String subject;
    LocalDate startDate;
    LocalTime startTime;
    LocalDate endDate;
    LocalTime endTime;
    String location;
    String description;
    String scope = "single";

    TestEditDialogData(Event e) {
      this.originalEvent = e;
      this.subject = e.getSubject();
      this.startDate = e.getStartDateTime().toLocalDate();
      this.startTime = e.getStartDateTime().toLocalTime();
      this.endDate = e.getEndDateTime().toLocalDate();
      this.endTime = e.getEndDateTime().toLocalTime();
      this.location = e.getLocation() != null ? e.getLocation() : "";
      this.description = e.getDescription() != null ? e.getDescription() : "";
    }

    @Override
    public String getSubject() {
      return subject;
    }

    @Override
    public LocalDate getStartDate() {
      return startDate;
    }

    @Override
    public LocalTime getStartTime() {
      return startTime;
    }

    @Override
    public LocalDate getEndDate() {
      return endDate;
    }

    @Override
    public LocalTime getEndTime() {
      return endTime;
    }

    @Override
    public String getEventLocation() {
      return location;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public String getEditScope() {
      return scope;
    }

    @Override
    public Event getOriginalEvent() {
      return originalEvent;
    }

    @Override
    public boolean isConfirmed() {
      return true;
    }
  }
}