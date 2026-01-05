package calendar.controller;

import static calendar.util.EventFinder.findBySubjectAndStart;

import calendar.command.CalendarCommand;
import calendar.command.CreateEventCommand;
import calendar.command.EditEventCommand;
import calendar.command.QueryEventCommand;
import calendar.dto.CalendarDto;
import calendar.dto.CreateEventDto;
import calendar.dto.EditEventDto;
import calendar.dto.QueryEventDto;
import calendar.dto.QueryResultDto;
import calendar.interfacetypes.IcreateEventDialogData;
import calendar.interfacetypes.IeditEventDialogData;
import calendar.interfacetypes.IguiViewCalendar;
import calendar.interfacetypes.IresultDto;
import calendar.model.Calendar;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.service.GuiDtoBuilderService;
import calendar.view.BulkEditEventDialog;
import calendar.view.CreateCalendarDialog;
import calendar.view.CreateEventDialog;
import calendar.view.EditCalendarDialog;
import calendar.view.EditEventDialog;
import calendar.view.SearchEditEventDialog;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Simple controller for calendar operations only.
 * Step 1: Start small, build incrementally.
 */
public class SimpleGuiController {

  private final CalendarModel service;
  private final IguiViewCalendar view;
  private final GuiDtoBuilderService guiBuilder;
  private YearMonth currentMonth;
  private LocalDate selectedDate;

  /**
   * Constructs the controller and initializes the application state.
   * Sets up the default calendar, wires listeners, and loads initial data.
   *
   * @param service the model service handling business logic
   * @param view    the GUI view component
   */
  public SimpleGuiController(CalendarModel service, IguiViewCalendar view) {
    this.service = service;
    this.view = view;
    this.guiBuilder = new GuiDtoBuilderService();
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();
    initializeDefaultCalendar();
    wireUpEventListeners();
    updateViewAndLoadData();
    refreshCalendarList();
  }

  /**
   * Wires up all GUI event listeners to their respective controller actions.
   * Handles navigation, selection, and button clicks.
   */
  private void wireUpEventListeners() {
    view.getCalendarDropdown().addActionListener(e -> {
      String selected = (String) view.getCalendarDropdown().getSelectedItem();
      if (selected != null && !selected.equals("(No calendars)")) {
        switchCalendar(selected);
      }
    });
    view.addNavigationListeners(
            e -> {
              currentMonth = currentMonth.minusMonths(1);
              updateViewAndLoadData();
            },
            e -> {
              currentMonth = currentMonth.plusMonths(1);
              updateViewAndLoadData();
            },
            e -> {
              currentMonth = YearMonth.now();
              selectedDate = LocalDate.now();
              updateViewAndLoadData();
            }
    );
    view.setDateSelectionListener(date -> {
      this.selectedDate = date;
      view.updateCalendarView(currentMonth, selectedDate);
      loadEventsForDate(date);
    });
    view.getCreateCalendarButton().addActionListener(e -> showCreateCalendarDialog());
    view.getEditCalendarButton().addActionListener(e -> showEditCalendarDialog());
    view.getCreateEventButton().addActionListener(e -> showCreateEventDialog());
    view.getEditEventButton().addActionListener(e -> showEditEventDialog());
    view.getEditEventsBySearchButton().addActionListener(e -> showEditEventsBySearchDialog());
  }

  /**
   * Displays the dialog for creating a new event.
   * If confirmed, triggers the event creation logic.
   */
  private void showCreateEventDialog() {
    LocalDate selectedDate = view.getSelectedDate();
    if (selectedDate == null) {
      selectedDate = LocalDate.now();
    }
    CreateEventDialog dialog = new CreateEventDialog(view.getFrame(), selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      handleCreateEvent(dialog);
    }
  }

  /**
   * Shows dialog to search and edit events by subject and start datetime.
   * Handles the flow of searching, validating results, and performing bulk updates.
   */
  private void showEditEventsBySearchDialog() {
    try {
      String calendarName = service.getActiveCalendar();
      String calendarZoneId = service.getCalendarTimezone(calendarName);
      ZoneId calendarZone = ZoneId.of(calendarZoneId);
      SearchEditEventDialog searchDialog = new SearchEditEventDialog(view.getFrame());
      searchDialog.setVisible(true);
      if (!searchDialog.isConfirmed()) {
        return;
      }
      String subject = searchDialog.getSubject();
      ZonedDateTime startDateTime = searchDialog.getStartDateTime(calendarZone);
      Calendar calendarModel = service.calendarModel(calendarName);
      Set<Event> allEvents = calendarModel.getEvents();
      List<Event> matchingEvents = findBySubjectAndStart(subject, startDateTime, allEvents);
      if (matchingEvents.isEmpty()) {
        view.showError("No events found with subject '" + subject
                + "' starting at " + startDateTime);
        return;
      }
      matchingEvents.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));
      view.showSuccess("Found " + matchingEvents.size() + " matching event(s).");
      BulkEditEventDialog bulkDialog = new BulkEditEventDialog(
              view.getFrame(),
              matchingEvents, calendarZone);
      bulkDialog.setVisible(true);
      if (bulkDialog.isConfirmed()) {
        if (!matchingEvents.isEmpty()) {
          Event templateEvent = matchingEvents.get(0);
          updateEventWithChanges(
                  templateEvent,
                  bulkDialog.getSubject(),
                  bulkDialog.getStartDate(),
                  bulkDialog.getStartTime(),
                  bulkDialog.getEndDate(),
                  bulkDialog.getEndTime(),
                  bulkDialog.getEventLocation(),
                  bulkDialog.getDescription(),
                  "all"
          );
        }
        view.showSuccess("All matching events updated successfully!");
        loadEventsForDate(view.getSelectedDate());
      }
    } catch (Exception e) {
      view.showError("Failed to search/edit events: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Processes the data from the create event dialog and executes the creation command.
   * Validates input such as empty subjects or invalid time ranges.
   *
   * @param dialog the data interface containing user input from the dialog
   */
  public void handleCreateEvent(IcreateEventDialogData dialog) {
    String subject = dialog.getSubject();
    if (subject.isEmpty()) {
      view.showError("Event name cannot be empty!");
      return;
    }
    try {
      CreateEventDto dto;
      if (dialog.isAllDay()) {
        LocalDate date = dialog.getSelectedDate();
        if (!dialog.isRecurring()) {
          dto = guiBuilder.buildAllDaySingleEventDto(subject, date);
        } else {
          Set<DayOfWeek> weekdays = dialog.getSelectedWeekdays();
          if (weekdays.isEmpty()) {
            view.showError("Please select at least one weekday for recurring events!");
            return;
          }
          if (dialog.isRepeatForTimes()) {
            int times = dialog.getRepeatTimes();
            dto = guiBuilder.buildAllDayRecurringForDto(subject, date, weekdays, times);
          } else {
            LocalDate until = dialog.getRepeatUntilDate();
            dto = guiBuilder.buildAllDayRecurringUntilDto(subject, date, weekdays, until);
          }
        }
      } else {
        LocalTime startTime = dialog.getStartTime();
        LocalTime endTime = dialog.getEndTime();
        LocalDate startDate = dialog.getSelectedDate();
        LocalDate endDate = dialog.getEndDate();

        LocalDateTime start = LocalDateTime.of(startDate, startTime);
        LocalDateTime end = LocalDateTime.of(endDate, endTime);

        if (end.isBefore(start) || end.equals(start)) {
          view.showError("End time must be after start time!");
          return;
        }
        if (!dialog.isRecurring()) {
          dto = guiBuilder.buildTimedSingleEventDto(subject, start, end);
        } else {
          Set<DayOfWeek> weekdays = dialog.getSelectedWeekdays();
          if (weekdays.isEmpty()) {
            view.showError("Please select at least one weekday for recurring events!");
            return;
          }
          if (dialog.isRepeatForTimes()) {
            int times = dialog.getRepeatTimes();
            dto = guiBuilder.buildTimedRecurringForDto(subject, start, end, weekdays, times);
          } else {
            LocalDate until = dialog.getRepeatUntilDate();
            dto = guiBuilder.buildTimedRecurringUntilDto(subject, start, end, weekdays, until);
          }
        }
      }
      CreateEventCommand cmd = new CreateEventCommand(dto, service);
      cmd.execute();
      view.setStatus("Event '" + subject + "' created!");
      view.showSuccess("Event created successfully!");
    } catch (Exception e) {
      view.showError("Failed to create event: " + e.getMessage());
    }
  }

  /**
   * Shows dialog to edit the active calendar.
   * Allows modification of the calendar name and timezone.
   */
  private void showEditCalendarDialog() {
    try {
      String activeCalendar = service.getActiveCalendar();
      if (activeCalendar == null) {
        view.showError("No calendar selected!");
        return;
      }
      String currentTimezone = service.getCalendarTimezone(activeCalendar);
      EditCalendarDialog dialog = new EditCalendarDialog(view.getFrame(),
              activeCalendar, currentTimezone);
      dialog.setVisible(true);
      if (dialog.isConfirmed()) {
        String newName = dialog.getNewName();
        String newTimezone = dialog.getNewTimezone();

        if (newName.isEmpty()) {
          view.showError("Calendar name cannot be empty!");
          return;
        }

        boolean nameChanged = !newName.equals(activeCalendar);
        boolean timezoneChanged = !newTimezone.equals(currentTimezone);

        if (!nameChanged && !timezoneChanged) {
          return;
        }
        if (timezoneChanged) {
          editCalendarTimezone(activeCalendar, newTimezone);
        }
        if (nameChanged) {
          editCalendarName(activeCalendar, newName);
        } else {
          refreshCalendarList();
        }
      }
    } catch (Exception e) {
      view.showError("Failed to edit calendar: " + e.getMessage());
    }
  }

  /**
   * Edits the calendar name.
   *
   * @param currentName the current name of the calendar
   * @param newName     the new name to assign
   */
  public void editCalendarName(String currentName, String newName) {
    try {
      CalendarDto dto = guiBuilder.buildEditCalendarDto(currentName, "name", newName);
      CalendarCommand cmd = new CalendarCommand(dto, service);
      cmd.execute();
      refreshCalendarList();
      view.setStatus("Calendar renamed to '" + newName + "'");
      view.showSuccess("Calendar renamed successfully!");
      switchCalendar(newName);
    } catch (Exception e) {
      view.showError("Failed to rename calendar: " + e.getMessage());
    }
  }

  /**
   * Edits the calendar timezone.
   *
   * @param calendarName the name of the calendar to update
   * @param newTimezone  the new timezone string
   */
  public void editCalendarTimezone(String calendarName, String newTimezone) {
    try {
      CalendarDto dto = guiBuilder.buildEditCalendarDto(calendarName, "timezone", newTimezone);
      CalendarCommand cmd = new CalendarCommand(dto, service);
      cmd.execute();
      refreshCalendarList();
      view.setStatus("Calendar timezone updated!");
      view.showSuccess("Calendar timezone updated successfully!");
    } catch (Exception e) {
      view.showError("Failed to update timezone: " + e.getMessage());
    }
  }


  /**
   * Creates a default calendar if none exists in the system.
   * Uses the system's default timezone.
   */
  private void initializeDefaultCalendar() {
    try {
      Set<String> calendars = service.getAllCalendarNames();
      if (calendars == null || calendars.isEmpty()) {
        String timezone = ZoneId.systemDefault().getId();
        createCalendar("Default", timezone);
        view.setStatus("Default calendar created!");
      }
    } catch (Exception e) {
      view.setStatus("Ready");
    }
  }

  /**
   * Shows dialog to create a new calendar.
   */
  private void showCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(view.getFrame());
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      String name = dialog.getCalendarName();
      String timezone = dialog.getTimezone();
      if (name.isEmpty()) {
        view.showError("Calendar name cannot be empty!");
        return;
      }
      createCalendar(name, timezone);
    }
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the name of the new calendar
   * @param timezone the timezone for the new calendar
   */
  public void createCalendar(String name, String timezone) {
    try {
      CalendarDto dto = guiBuilder.buildCreateCalendarDto(name, timezone);
      CalendarCommand cmd = new CalendarCommand(dto, service);
      cmd.execute();
      refreshCalendarList();
      view.setStatus("Calendar '" + name + "' created!");
      view.showSuccess("Calendar '" + name + "' created successfully!");
      switchCalendar(name);
    } catch (Exception e) {
      view.showError("Failed to create calendar: " + e.getMessage());
    }
  }

  /**
   * Switches the active calendar to the specified one.
   *
   * @param name the name of the calendar to activate
   */
  public void switchCalendar(String name) {
    try {
      CalendarDto dto = guiBuilder.buildUseCalendarDto(name);
      CalendarCommand cmd = new CalendarCommand(dto, service);
      cmd.execute();
      view.setStatus("Active calendar: " + name);
    } catch (Exception e) {
      view.showError("Failed to switch calendar: " + e.getMessage());
    }
  }

  /**
   * Refreshes the calendar list in the view dropdown.
   * Retrieves all available calendar names from the service.
   */
  public void refreshCalendarList() {
    try {
      Set<String> calendarNames = service.getAllCalendarNames();
      String activeCalendar = service.getActiveCalendar();
      view.updateCalendarList(new ArrayList<>(calendarNames), activeCalendar);
    } catch (Exception e) {
      view.showError("Failed to load calendars: " + e.getMessage());
    }
  }

  /**
   * Helper method to update the calendar grid and load data.
   */
  private void updateViewAndLoadData() {
    view.updateCalendarView(currentMonth, selectedDate);
  }

  /**
   * Loads and displays events for a specific date in the view.
   *
   * @param date the date for which to load events
   */
  public void loadEventsForDate(LocalDate date) {
    try {
      String activeCalendar = service.getActiveCalendar();
      if (activeCalendar == null) {
        view.showError("No calendar selected! Please select or create a calendar.");
        return;
      }
      QueryEventDto dto = guiBuilder.buildQueryForDate(date);
      QueryEventCommand cmd = new QueryEventCommand(service, dto);
      IresultDto result = cmd.execute();
      if (result instanceof QueryResultDto) {
        QueryResultDto queryResult = (QueryResultDto) result;
        view.displayEventsForQueryResult(date, queryResult);
      } else {
        view.showError("Unexpected result type from query");
      }
    } catch (Exception e) {
      e.printStackTrace();
      String errorMsg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getName();
      view.showError("Failed to load events: " + errorMsg);
    }
  }

  /**
   * Shows dialog to edit an event on the selected date.
   * If multiple events exist, prompts the user to select one.
   */
  private void showEditEventDialog() {
    LocalDate selectedDate = view.getSelectedDate();
    if (selectedDate == null) {
      view.showError("Please select a date first");
      return;
    }
    try {
      QueryEventDto queryDto = guiBuilder.buildQueryForDate(selectedDate);
      QueryEventCommand queryCmd = new QueryEventCommand(service, queryDto);
      IresultDto result = queryCmd.execute();
      if (!(result instanceof QueryResultDto)) {
        view.showError("Failed to load events");
        return;
      }
      QueryResultDto queryResult = (QueryResultDto) result;
      if (queryResult.getEvents() == null || queryResult.getEvents().isEmpty()) {
        view.showError("No events on this date to edit");
        return;
      }
      Event selectedEvent = selectEventFromList(queryResult.getEvents());
      if (selectedEvent == null) {
        return;
      }
      EditEventDialog dialog = new EditEventDialog(view.getFrame(), selectedEvent);
      dialog.setVisible(true);
      if (dialog.isConfirmed()) {
        handleEditEvent(dialog);
      }
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Shows a selection dialog for choosing which event to edit from a list.
   *
   * @param events the set of events to choose from
   * @return the selected Event object, or null if cancelled
   */
  private Event selectEventFromList(Set<Event> events) {
    Event[] eventArray = events.toArray(new Event[0]);
    String[] eventNames = new String[eventArray.length];
    java.time.format.DateTimeFormatter formatter =
            java.time.format.DateTimeFormatter.ofPattern("HH:mm");
    for (int i = 0; i < eventArray.length; i++) {
      Event e = eventArray[i];
      String time = e.getStartDateTime().format(formatter) + " - "
              + e.getEndDateTime().format(formatter);
      eventNames[i] = time + " - " + e.getSubject();
    }
    String selected = (String) javax.swing.JOptionPane.showInputDialog(
            view.getFrame(),
            "Select event to edit:",
            "Select Event",
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            eventNames,
            eventNames[0]
    );
    if (selected == null) {
      return null;
    }
    for (int i = 0; i < eventNames.length; i++) {
      if (eventNames[i].equals(selected)) {
        return eventArray[i];
      }
    }
    return null;
  }

  /**
   * Applies the changes to a given event.
   * Determines the scope of the edit (single, series, future) and executes the update.
   *
   * @param originalEvent  the event being modified
   * @param newSubject     the updated subject
   * @param newStartDate   the updated start date
   * @param newStartTime   the updated start time
   * @param newEndDate     the updated end date
   * @param newEndTime     the updated end time
   * @param newLocation    the updated location
   * @param newDescription the updated description
   * @param editScope      the scope of the edit ("single", "series", etc.)
   */
  private void updateEventWithChanges(Event originalEvent,
                                      String newSubject,
                                      LocalDate newStartDate,
                                      LocalTime newStartTime,
                                      LocalDate newEndDate,
                                      LocalTime newEndTime,
                                      String newLocation,
                                      String newDescription,
                                      String editScope) {

    try {
      if (!newEndTime.isAfter(newStartTime)) {
        view.showError("End time must be after start time!");
        return;
      }
      LocalDateTime newStart = LocalDateTime.of(newStartDate, newStartTime);
      LocalDateTime newEnd = LocalDateTime.of(newEndDate, newEndTime);
      Map<String, String> changes = new HashMap<>();
      java.time.format.DateTimeFormatter formatter = java.time
              .format
              .DateTimeFormatter
              .ofPattern("yyyy-MM-dd'T'HH:mm");
      if (!originalEvent.getSubject().equals(newSubject)) {
        changes.put("subject", newSubject);
      }
      if (!originalEvent.getStartDateTime().toLocalDateTime().equals(newStart)) {
        changes.put("start", newStart.format(formatter));
      }
      if (!originalEvent.getEndDateTime().toLocalDateTime().equals(newEnd)) {
        changes.put("end", newEnd.format(formatter));
      }
      String oldLocation = originalEvent.getLocation() != null ? originalEvent.getLocation() : "";
      if (!oldLocation.equals(newLocation)) {
        changes.put("location", newLocation);
      }
      String oldDescription = originalEvent.getDescription() != null
              ? originalEvent.getDescription() : "";
      if (!oldDescription.equals(newDescription)) {
        changes.put("description", newDescription);
      }
      if (changes.isEmpty()) {
        return;
      }
      EditEventDto dto;
      if (editScope.equals("single")) {
        dto = guiBuilder.buildEditSingleEventDto(
                originalEvent.getSubject(),
                originalEvent.getStartDateTime().toLocalDateTime(),
                originalEvent.getEndDateTime().toLocalDateTime(),
                changes
        );
      } else if (editScope.equals("series")) {
        dto = guiBuilder.buildEditSeriesDto(
                originalEvent.getSubject(),
                originalEvent.getStartDateTime().toLocalDateTime(),
                changes
        );
      } else {
        dto = guiBuilder.buildEditForwardDto(
                originalEvent.getSubject(),
                originalEvent.getStartDateTime().toLocalDateTime(),
                changes
        );
      }
      EditEventCommand cmd = new EditEventCommand(dto, service);
      cmd.execute();
    } catch (Exception e) {
      view.showError("Failed to update event: " + e.getMessage());
      e.printStackTrace();
    }
  }


  /**
   * Handles the edit event action after the dialog is confirmed.
   * Extracts data from the dialog and triggers the update logic.
   *
   * @param dialog the data interface containing edits from the dialog
   */
  public void handleEditEvent(IeditEventDialogData dialog) {
    Event originalEvent = dialog.getOriginalEvent();
    updateEventWithChanges(
            originalEvent,
            dialog.getSubject(),
            dialog.getStartDate(),
            dialog.getStartTime(),
            dialog.getEndDate(),
            dialog.getEndTime(),
            dialog.getEventLocation(),
            dialog.getDescription(),
            dialog.getEditScope()
    );
    view.showSuccess("Event updated successfully!");
    loadEventsForDate(view.getSelectedDate());
  }
}