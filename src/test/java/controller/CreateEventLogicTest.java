package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.SimpleGuiController;
import calendar.dto.QueryResultDto;
import calendar.interfacetypes.IcreateEventDialogData;
import calendar.interfacetypes.IguiViewCalendar;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.service.GuiDtoBuilderService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import org.junit.Before;
import org.junit.Test;

/**
 * Focused unit tests for the handleCreateEvent method logic and Controller initialization.
 */
public class CreateEventLogicTest {

  private SimpleGuiController controller;
  private CalendarModel service;
  private TestGuiView mockView;
  private GuiDtoBuilderService guiBuilder;

  /**
   * sets up calemdarservice,controller and the gui for testing.
   */
  @Before
  public void setUp() {
    service = new CalendarModel(new CalendarCollection());
    mockView = new TestGuiView();
    controller = new SimpleGuiController(service, mockView);
    guiBuilder = new GuiDtoBuilderService();
  }

  @Test
  public void testControllerInitialization() throws Exception {
    assertTrue("Default calendar should be created", service.checkCalendarModel("Default"));
    assertEquals("Active calendar should be Default", "Default", service.getActiveCalendar());
    assertNotNull("View should have received an update "
            +
            "for the calendar grid", mockView.lastUpdatedDate);
    assertNotNull("View calendar list should have been updated",
            mockView.lastCalendarList);
    assertTrue("Calendar list should contain Default",
            mockView.lastCalendarList.contains("Default"));
  }

  @Test
  public void testDropdownListenerTriggersSwitchCalendar() throws Exception {
    controller.createCalendar("Work", "UTC");
    assertNotNull("Dropdown listener should be registered", mockView.dropdownListener);
    mockView.selectedDropdownItem = "Work";
    mockView.dropdownListener
            .actionPerformed(new ActionEvent(mockView.getCalendarDropdown(),
            1, "select"));
    assertEquals("Work", service.getActiveCalendar());
    assertEquals("Active calendar: Work", mockView.lastStatus);
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
  public void testHandleCreateEventAllDayRecurringBranchCoverage() throws Exception {
    LocalDate today = LocalDate.now();
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Recurring All Day";
    dialog.allDay = true;
    dialog.recurring = true;
    dialog.weekdays = Set.of(DayOfWeek.MONDAY);
    dialog.repeatForTimes = true;
    dialog.repeatTimes = 3;
    controller.handleCreateEvent(dialog);
    assertEquals("Event 'Recurring All Day' created!", mockView.lastStatus);
  }

  @Test
  public void testHandleCreateEventAllDayRecurringUntilBranchCoverage() throws Exception {
    LocalDate today = LocalDate.now();
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Recurring Until";
    dialog.allDay = true;
    dialog.recurring = true;
    dialog.weekdays = Set.of(DayOfWeek.MONDAY);
    dialog.repeatForTimes = false;
    dialog.repeatUntilDate = today.plusDays(10);
    controller.handleCreateEvent(dialog);
    assertEquals("Event 'Recurring Until' created!", mockView.lastStatus);
  }

  @Test
  public void testHandleCreateEventTimedRecurringBranchCoverage() throws Exception {
    LocalDate today = LocalDate.now();
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Timed Recurring";
    dialog.allDay = false;
    dialog.recurring = true;
    dialog.weekdays = Set.of(DayOfWeek.TUESDAY);
    dialog.repeatForTimes = true;
    dialog.repeatTimes = 2;
    dialog.startTime = LocalTime.of(10, 0);
    dialog.endTime = LocalTime.of(11, 0);
    dialog.endDate = today;
    controller.handleCreateEvent(dialog);
    assertEquals("Event 'Timed Recurring' created!", mockView.lastStatus);
  }

  @Test
  public void testHandleCreateEventAllDaySingle() throws Exception {
    LocalDate today = LocalDate.now();
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Holiday";
    dialog.allDay = true;
    dialog.recurring = false;
    controller.handleCreateEvent(dialog);
    assertEquals("Event 'Holiday' created!", mockView.lastStatus);
    assertNotNull("Should have a success message", mockView.lastSuccess);
  }

  @Test
  public void testHandleCreateEventTimedSingle() {
    LocalDate today = LocalDate.now();
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Meeting";
    dialog.allDay = false;
    dialog.recurring = false;
    dialog.startTime = LocalTime.of(10, 0);
    dialog.endTime = LocalTime.of(11, 0);
    dialog.endDate = today;
    controller.handleCreateEvent(dialog);
    assertEquals("Event 'Meeting' created!", mockView.lastStatus);
  }

  @Test
  public void testHandleCreateEventValidationEmptySubject() {
    TestDialogData dialog = new TestDialogData(LocalDate.now());
    dialog.subject = "";
    controller.handleCreateEvent(dialog);
    assertNotNull("Should show error", mockView.lastError);
    assertTrue(mockView.lastError.contains("cannot be empty"));
  }

  @Test
  public void testButtonListenersWiring() {
    JButton createBtn = mockView.getCreateEventButton();
    assertEquals("Should have 1 listener", 1, createBtn.getActionListeners().length);
  }

  @Test
  public void testHandleCreateEventAllDayRecurringBranchCoverage2() throws Exception {
    LocalDate today = LocalDate.now().with(java.time.temporal
            .TemporalAdjusters
            .nextOrSame(DayOfWeek.MONDAY));
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Recurring Count";
    dialog.allDay = true;
    dialog.recurring = true;
    dialog.weekdays = Set.of(DayOfWeek.MONDAY);
    dialog.repeatForTimes = true;
    dialog.repeatTimes = 3;
    dialog.repeatUntilDate = today.plusWeeks(10);
    controller.handleCreateEvent(dialog);
    assertEquals("Event 'Recurring Count' created!", mockView.lastStatus);
    int eventCount = 0;
    for (int i = 0; i < 15; i++) {
      LocalDate dateToCheck = today.plusWeeks(i);
      var events = service.queryEvent(guiBuilder.buildQueryForDate(dateToCheck));
      boolean eventExists = events.stream()
              .anyMatch(e -> e.getSubject().equals("Recurring Count"));

      if (eventExists) {
        eventCount++;
      }
    }
    assertEquals("Should have created exactly 3 events based on repeatTimes", 3, eventCount);
  }

  @Test
  public void testHandleCreateEventValidationEndBeforeStart() {
    LocalDate today = LocalDate.now();
    TestDialogData dialog = new TestDialogData(today);
    dialog.subject = "Bad Meeting";
    dialog.allDay = false;
    dialog.startTime = LocalTime.of(12, 0);
    dialog.endTime = LocalTime.of(11, 0); // End before start
    dialog.endDate = today;
    controller.handleCreateEvent(dialog);
    assertNotNull("Should show error", mockView.lastError);
    assertTrue(mockView.lastError.contains("End time must be after start time"));
  }

  @Test
  public void testHandleCreateEventRecurringNoWeekdays() {
    TestDialogData dialog = new TestDialogData(LocalDate.now());
    dialog.subject = "Ghost Event";
    dialog.allDay = true;
    dialog.recurring = true;
    dialog.weekdays = Set.of();
    controller.handleCreateEvent(dialog);
    assertNotNull("Should show error", mockView.lastError);
    assertTrue(mockView.lastError.contains("select at least one weekday"));
  }

  private static class TestDialogData implements IcreateEventDialogData {
    String subject = "Test";
    boolean allDay = false;
    boolean recurring = false;
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate selectedDate;
    LocalDate endDate;
    Set<DayOfWeek> weekdays = Set.of(DayOfWeek.MONDAY);
    boolean repeatForTimes = true;
    int repeatTimes = 1;
    LocalDate repeatUntilDate;

    public TestDialogData(LocalDate date) {
      this.selectedDate = date;
      this.endDate = date;
      this.repeatUntilDate = date.plusWeeks(1);
    }

    @Override public String getSubject() {
      return subject;
    }

    @Override public boolean isAllDay() {
      return allDay;
    }

    @Override public boolean isRecurring() {
      return recurring;
    }

    @Override public LocalTime getStartTime() {
      return startTime;
    }

    @Override public LocalTime getEndTime() {
      return endTime;
    }

    @Override public LocalDate getSelectedDate() {
      return selectedDate;
    }

    @Override public LocalDate getEndDate() {
      return endDate;
    }

    @Override public Set<DayOfWeek> getSelectedWeekdays() {
      return weekdays;
    }

    @Override public boolean isRepeatForTimes() {
      return repeatForTimes;
    }

    @Override public int getRepeatTimes() {
      return repeatTimes;
    }

    @Override public LocalDate getRepeatUntilDate() {
      return repeatUntilDate;
    }
  }

  /**
  * Enhanced Mock View that captures Listeners to allow test invocation.
  */
  private static class TestGuiView implements IguiViewCalendar {
    String lastError;
    String lastStatus;
    String lastSuccess;
    LocalDate lastUpdatedDate;
    List<String> lastCalendarList;
    QueryResultDto lastQueryResult;
    ActionListener dropdownListener;
    Consumer<LocalDate> dateSelectionListener;
    String selectedDropdownItem;

    private final JComboBox<String> calendarDropdown = new JComboBox<>() {

      @Override
      public void addActionListener(ActionListener l) {
        dropdownListener = l;
      }

      @Override
      public Object getSelectedItem() {
        return selectedDropdownItem;
      }
    };

    private final JButton createCalendarBtn = new JButton();
    private final JButton editCalendarBtn = new JButton();
    private final JButton createEventBtn = new JButton();
    private final JButton editEventBtn = new JButton();
    private final JButton editSelectEventBtn = new JButton();

    @Override public void showError(String m) {
      lastError = m;
    }

    @Override public void setStatus(String m) {
      lastStatus = m;
    }

    @Override public void showSuccess(String m) {
      lastSuccess = m;
    }

    @Override public void updateCalendarList(List<String> n, String a) {
      this.lastCalendarList = n;
      this.selectedDropdownItem = a;
    }

    @Override public void updateCalendarView(YearMonth m, LocalDate d) {
      this.lastUpdatedDate = d;
    }

    @Override public void displayEventsForQueryResult(LocalDate d, QueryResultDto r) {
      this.lastQueryResult = r;
    }

    @Override public LocalDate getSelectedDate() {
      return lastUpdatedDate;
    }

    @Override public JComboBox<String> getCalendarDropdown() {
      return calendarDropdown;
    }

    @Override public JButton getCreateCalendarButton() {
      return createCalendarBtn;
    }

    @Override public JButton getEditCalendarButton() {
      return editCalendarBtn;
    }

    @Override public JButton getCreateEventButton() {
      return createEventBtn;
    }

    @Override public JButton getEditEventButton() {
      return editEventBtn;
    }

    @Override public JFrame getFrame() {
      return null;
    }

    @Override public void addNavigationListeners(ActionListener p,
                                                 ActionListener n,
                                                 ActionListener t){}

    @Override public void setDateSelectionListener(Consumer<LocalDate> l) {
      this.dateSelectionListener = l;
    }

    @Override
    public JButton getEditEventsBySearchButton() {
      return  editSelectEventBtn;
    }
  }
}