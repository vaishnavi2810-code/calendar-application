package calendar.strategy;

import calendar.model.Event;
import calendar.util.EventFinder;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Implements the "edit events" (forward) logic.
 * Modifies ALL events (single or series) that match the subject/start criteria.
 * Breaks the series by assigning a new UUID if 'start' time is modified.
 */
public class EditForward extends AbstractEditSeries {

  @Override
  protected List<Event> getEventsToEdit(String seriesId, ZonedDateTime targetStart,
                                        Set<Event> allEvents) {
    return EventFinder.findSeriesFrom(seriesId, targetStart, allEvents);
  }
}
