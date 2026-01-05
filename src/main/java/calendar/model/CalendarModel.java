package calendar.model;

import calendar.dto.CopyEventDto;
import calendar.dto.CreateEventDto;
import calendar.dto.EditEventDto;
import calendar.dto.ExportEventDto;
import calendar.dto.QueryEventDto;
import calendar.factory.CopyStrategyFactory;
import calendar.factory.CreateStrategyFactory;
import calendar.factory.EditStrategyFactory;
import calendar.factory.ExporterFactory;
import calendar.factory.QueryStrategyFactory;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.Icopy;
import calendar.interfacetypes.Icreate;
import calendar.interfacetypes.Iedit;
import calendar.interfacetypes.Iexport;
import calendar.interfacetypes.Iquery;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Model class that provides operations for managing calendars and events.
 */
public class CalendarModel {

  private final Icalendarcollection repository;
  private String activeCalendarName;

  /**
   * Constructs a CalendarModel with the specified repository.
   *
   * @param repository the repository to use for storing and retrieving calendars
   */
  public CalendarModel(Icalendarcollection repository) {
    this.repository = repository;
  }

  /**
   * Sets the active calendar by name.
   *
   * @param name the name of the calendar to set as active
   * @throws Exception if the calendar does not exist
   */
  public void setActiveCalendar(String name) throws Exception {
    if (!repository.existsByName(name)) {
      throw new Exception("Error: Calendar '" + name + "' not found.");
    }
    this.activeCalendarName = name;
  }

  /**
   * Retrieves the name of the currently active calendar.
   *
   * @return the name of the active calendar
   * @throws Exception if an error occurs while retrieving the active calendar
   */
  public String getActiveCalendar() throws Exception {
    return activeCalendarName;
  }

  /**
   * Creates an event in the active calendar.
   *
   * @param dto the event data transfer object containing event details
   * @throws Exception if the active calendar is not found or creation fails
   */
  public void createEvent(CreateEventDto dto) throws Exception {
    Calendar activeModel = repository.findByName(activeCalendarName);
    if (activeModel == null) {
      throw new Exception("Calendar not found.");
    }
    Set<Event> mergedEvents = new HashSet<>(activeModel.getEvents());
    ZoneId timezone = activeModel.getTimezone();
    Icreate strategy = CreateStrategyFactory.getStrategy(dto.getType());
    Set<Event> newEvents = strategy.create(dto, mergedEvents, timezone);
    mergedEvents.addAll(newEvents);
    Calendar updatedModel = new CalendarModelBuilder()
            .setName(activeModel.getName())
            .setTimeZone(activeModel.getTimezone())
            .setEvents(mergedEvents)
            .build();
    repository.save(updatedModel);
  }

  /**
   * Edits an existing event in the active calendar.
   *
   * @param dto the edit data transfer object containing modifications
   * @throws Exception if the active calendar is not found or edit fails
   */
  public void editEvent(EditEventDto dto) throws Exception {
    Calendar activeModel = repository.findByName(activeCalendarName);
    if (activeModel == null) {
      throw new Exception("Calendar not found.");
    }
    Set<Event> eventsCopy = new HashSet<>(activeModel.getEvents());
    ZoneId timezone = activeModel.getTimezone();
    Iedit strategy = EditStrategyFactory.getStrategy(dto.getEditType());
    strategy.edit(dto, eventsCopy, timezone);
    Calendar updatedModel = new CalendarModelBuilder()
            .setName(activeModel.getName())
            .setTimeZone(activeModel.getTimezone())
            .setEvents(eventsCopy)
            .build();
    repository.save(updatedModel);
  }

  /**
   * Queries events from the active calendar using a specific strategy.
   *
   * @param dto the query data transfer object containing query criteria
   * @return a set of events matching the query
   * @throws Exception if the active calendar is not found
   */
  public Set<Event> queryEvent(QueryEventDto dto) throws Exception {
    Calendar activeModel = repository.findByName(activeCalendarName);
    if (activeModel == null) {
      throw new Exception("Calendar not found.");
    }
    Set<Event> existingEvents = activeModel.getEvents();
    ZoneId timezone = activeModel.getTimezone();
    Iquery strategy = QueryStrategyFactory.getStrategy(dto.getType());
    return strategy.find(dto, existingEvents, timezone);
  }

  /**
   * Exports events from the active calendar to a specified format.
   *
   * @param dto the export data transfer object containing export details
   * @return the path or result of the export operation
   * @throws Exception if the active calendar is not found
   */
  public String exportEvent(ExportEventDto dto) throws Exception {
    Calendar activeModel = repository.findByName(activeCalendarName);
    if (activeModel == null) {
      throw new Exception("Calendar not found.");
    }
    Set<Event> existingEvents = activeModel.getEvents();
    Iexport exporter = ExporterFactory.getExporter(dto);
    return exporter.export(existingEvents, dto.getFileName());
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name    the name of the new calendar
   * @param zoneStr the timezone string for the calendar
   * @throws Exception if the calendar already exists or the timezone is invalid
   */
  public void createNewCalendar(String name, String zoneStr) throws Exception {
    if (repository.existsByName(name)) {
      throw new Exception("Error: A calendar with the name '" + name + "' already exists.");
    }

    ZoneId zone;
    try {
      zone = ZoneId.of(zoneStr);
    } catch (DateTimeException e) {
      throw new Exception("Error: Invalid Time Zone '" + zoneStr + "'.");
    }

    Calendar newModel = new CalendarModelBuilder()
            .setName(name)
            .setTimeZone(zone)
            .setEvents(new HashSet<>())
            .build();
    repository.save(newModel);
  }

  /**
   * Updates the timezone of an existing calendar.
   *
   * @param calendarName the name of the calendar to update
   * @param zoneStr      the new timezone string
   * @throws Exception if the calendar is not found, the timezone is invalid, or conversion fails
   */
  public void updateCalendarTimezone(String calendarName, String zoneStr) throws Exception {
    Calendar originalModel = repository.findByName(calendarName);
    if (originalModel == null) {
      throw new Exception("Calendar not found.");
    }
    ZoneId newZone;
    try {
      newZone = ZoneId.of(zoneStr);
    } catch (DateTimeException e) {
      throw new Exception("Error: Invalid Time Zone '" + zoneStr + "'.");
    }
    if (originalModel.getTimezone().equals(newZone)) {
      return;
    }
    Set<Event> newEventSet = new HashSet<>();
    for (Event oldEvent : originalModel.getEvents()) {
      ZonedDateTime newStart = oldEvent.getStartDateTime().withZoneSameInstant(newZone);
      ZonedDateTime newEnd = oldEvent.getEndDateTime().withZoneSameInstant(newZone);
      if (!newStart.toLocalDate().equals(newEnd.toLocalDate())) {
        throw new Exception("Error: Start Date and End Date should "
                +
                "not differ for a recurring event.");
      }

      DayOfWeek oldDay = oldEvent.getStartDateTime().getDayOfWeek();
      DayOfWeek newDay = newStart.getDayOfWeek();
      if (!oldDay.equals(newDay)) {
        throw new Exception("Error: Timezone change would cause event '"
                + oldEvent.getSubject() + "' to shift from " + oldDay
                + " to " + newDay + ".");
      }
      Event newEvent = new EventBuilder()
              .setSubject(oldEvent.getSubject())
              .setStartDateTime(newStart)
              .setEndDateTime(newEnd)
              .setLocation(oldEvent.getLocation())
              .setDescription(oldEvent.getDescription())
              .setStatus(oldEvent.getStatus())
              .setSeriesId(oldEvent.getSeriesId())
              .build();
      newEventSet.add(newEvent);
    }
    Calendar updatedModel = new CalendarModelBuilder()
            .setName(originalModel.getName())
            .setTimeZone(newZone)
            .setEvents(newEventSet)
            .build();
    repository.save(updatedModel);
  }

  /**
   * Updates the name of an existing calendar.
   *
   * @param currentName the current name of the calendar
   * @param newName     the new name to assign
   * @throws Exception if the calendar is not found or the new name already exists
   */
  public void updateCalendarName(String currentName, String newName) throws Exception {
    Calendar originalModel = repository.findByName(currentName);
    if (originalModel == null) {
      throw new Exception("Calendar not found.");
    }
    if (repository.existsByName(newName)) {
      throw new Exception("New name already exists.");
    }
    Calendar updatedModel = new CalendarModelBuilder()
            .setName(newName)
            .setTimeZone(originalModel.getTimezone())
            .setEvents(originalModel.getEvents())
            .build();
    repository.deleteByName(currentName);
    repository.save(updatedModel);
  }

  /**
   * Checks if a calendar exists by name.
   *
   * @param name the name of the calendar to check
   * @return true if the calendar exists, false otherwise
   * @throws Exception if a repository error occurs
   */
  public boolean checkCalendarModel(String name) throws Exception {
    return repository.existsByName(name);
  }

  /**
   * Retrieves a calendar model by name.
   *
   * @param name the name of the calendar
   * @return the calendar model
   * @throws Exception if the calendar is not found
   */
  public Calendar calendarModel(String name) throws Exception {
    return repository.findByName(name);
  }

  /**
   * Copies events from the active calendar to a target calendar based on the copy type.
   *
   * @param dto the copy data transfer object containing copy details
   * @throws Exception if the active calendar is not found or copy fails
   */
  public void copyEvent(CopyEventDto dto) throws Exception {
    Calendar activeModel = repository.findByName(activeCalendarName);
    if (activeModel == null) {
      throw new Exception("No calendar is currently selected.");
    }

    Calendar targetCalendar = repository.findByName(dto.getTargetCalendarName());
    if (targetCalendar == null) {
      throw new Exception("Target calendar '" + dto.getTargetCalendarName() + "' not found.");
    }

    Icopy strategy = CopyStrategyFactory.getStrategy(dto.getCopyType());
    Set<Event> updatedTargetEvents = strategy.copy(dto, activeModel, targetCalendar);

    Calendar updatedTargetCalendar = new CalendarModelBuilder()
            .setName(targetCalendar.getName())
            .setTimeZone(targetCalendar.getTimezone())
            .setEvents(updatedTargetEvents)
            .build();

    repository.save(updatedTargetCalendar);
  }

  /**
   * Retrieves the names of all available calendars.
   *
   * @return a set containing the names of all calendars
   */
  public Set<String> getAllCalendarNames() {
    return repository.getAllCalendarNames();
  }

  /**
   * Retrieves the timezone string for the specified calendar.
   *
   * @param calendarName the name of the calendar
   * @return the string representation of the calendar's timezone
   * @throws Exception if the calendar is not found
   */
  public String getCalendarTimezone(String calendarName) throws Exception {
    Calendar obj = this.calendarModel(calendarName);
    if (obj == null) {
      throw new Exception("Calendar not found.");
    } else {
      return obj.getTimezone().toString();
    }
  }
}