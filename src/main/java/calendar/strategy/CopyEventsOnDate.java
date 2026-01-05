package calendar.strategy;

import calendar.dto.CopyEventDto;
import calendar.model.Calendar;
import calendar.model.Event;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for copying all events from a specific date to another date.
 * The target date is used exactly as specified (no weekday adjustment).
 */
public class CopyEventsOnDate extends AbstractCopyEventsByDate {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Override
  protected List<Event> getEventsInDateRange(CopyEventDto dto, Calendar sourceCalendar)
      throws Exception {
    LocalDate sourceDate = LocalDate.parse(dto.getSourceDate(), FORMATTER);

    return sourceCalendar.getEvents().stream()
        .filter(e -> e.getStartDateTime().toLocalDate().equals(sourceDate))
        .collect(Collectors.toList());
  }

  @Override
  protected LocalDate calculateTargetDate(CopyEventDto dto, LocalDate firstEventDate)
      throws Exception {
    return LocalDate.parse(dto.getTargetDate(), FORMATTER);
  }

  @Override
  protected String getNoEventsFoundMessage(CopyEventDto dto) {
    return "No events found on " + dto.getSourceDate();
  }
}