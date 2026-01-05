package calendar.factory;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.strategy.CreateAllDayRecurringN;
import calendar.strategy.CreateAllDayRecurringUntil;
import calendar.strategy.CreateAllDaySingle;
import calendar.strategy.CreateEventSingle;
import calendar.strategy.CreateTimedRecurringFor;
import calendar.strategy.CreateTimedRecurringUntil;

/**
 * Factory for creating event creation strategy instances based on command type.
 * Provides different strategies for creating single events, all-day events, and recurring events.
 */
public class CreateStrategyFactory {

  /**
  * Returns the appropriate Icreate strategy based on the command type.
   *
  * @param type The type of event creation command.
  * @return The concrete Icreate strategy.
  */
  public static Icreate getStrategy(CreateEventDto.CommandType type) {
    switch (type) {
      case ALL_DAY_SINGLE:
        return new CreateAllDaySingle();
      case ALL_DAY_RECURRING_FOR:
        return new CreateAllDayRecurringN();
      case TIMED_SINGLE:
        return new CreateEventSingle();
      case TIMED_RECURRING_FOR:
        return new CreateTimedRecurringFor();
      case TIMED_RECURRING_UNTIL:
        return new CreateTimedRecurringUntil();
      case ALL_DAY_RECURRING_UNTIL:
        return new CreateAllDayRecurringUntil();
      default:
        throw new IllegalArgumentException("Unknown creation command type: " + type);
    }
  }
}
