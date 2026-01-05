package calendar.strategy;

import calendar.model.Event;
import calendar.util.EventFinder;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;


/**
 * class to handle event series edit.
 */
public class EditSeries extends AbstractEditSeries {
  @Override
  protected List<Event> getEventsToEdit(String seriesId,
                                        ZonedDateTime targetStart,
                                        Set<Event> allEvents) {
    return EventFinder.findBySeries(seriesId, allEvents);
  }
}