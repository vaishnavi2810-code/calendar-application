package calendar.factory;

import calendar.dto.CopyEventDto;
import calendar.interfacetypes.Icopy;
import calendar.strategy.CopyEventsBetweenDates;
import calendar.strategy.CopyEventsOnDate;
import calendar.strategy.CopySingleEvent;


/**
 * Factory for creating copy strategy instances based on copy operation type.
 * Provides different strategies for copying single events, events on a date
 * or events between dates.
 */
public class CopyStrategyFactory {

  /**
  * Returns the appropriate copy strategy implementation based on the specified copy type.
  * Supports copying single events, all events on a specific date
  * or all events within a date range.
  *
  * @param type the type of copy operation to perform
  * @return the corresponding copy strategy implementation
  * @throws IllegalArgumentException if the copy type is unknown or not supported
  */
  public static Icopy getStrategy(CopyEventDto.CopyType type) {
    switch (type) {
      case COPY_SINGLE_EVENT:
        return new CopySingleEvent();
      case COPY_EVENTS_ON_DATE:
        return new CopyEventsOnDate();
      case COPY_EVENTS_BETWEEN_DATES:
        return new CopyEventsBetweenDates();
      default:
        throw new IllegalArgumentException("Unknown copy command type: " + type);
    }
  }
}