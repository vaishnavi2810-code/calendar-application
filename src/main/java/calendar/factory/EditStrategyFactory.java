package calendar.factory;

import calendar.dto.EditEventDto;
import calendar.interfacetypes.Iedit;
import calendar.strategy.EditForward;
import calendar.strategy.EditSeries;
import calendar.strategy.EditSingle;

/**
 * Factory for edit event creation strategy instances based on command type.
 * Provides different strategies for editing events, EDIT_SINGLE, EDIT_FORWARD, EDIT_SERIES.
 */
public class EditStrategyFactory {

  /**
  * Returns the appropriate edit strategy based on the specified edit type.
  * Supports editing a single event occurrence, all future occurrences in a series,
  * or all occurrences in an entire series.
  *
  * @param type the type of edit operation to perform
  * @return the corresponding edit strategy implementation
  * @throws IllegalArgumentException if the edit type is unknown or not supported
  */
  public static Iedit getStrategy(EditEventDto.EditType type) {
    switch (type) {
      case EDIT_SINGLE:
        return new EditSingle();
      case EDIT_FORWARD:
        return new EditForward();
      case EDIT_SERIES:
        return new EditSeries();
      default:
        throw new IllegalArgumentException("Unknown edit command type: " + type);
    }
  }
}
