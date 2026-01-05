package calendar.factory;

import calendar.dto.QueryEventDto;
import calendar.interfacetypes.Iquery;
import calendar.strategy.PrintInRange;
import calendar.strategy.PrintOnDate;
import calendar.strategy.ShowStatusAt;

/**
 * Factory for creating query strategy instances based on query type.
 * Provides different strategies for querying events on a date, within a range, or checking status.
 */
public class QueryStrategyFactory {

  /**
   * Returns the appropriate query strategy based on the specified query type.
   * Supports printing events on a specific date, printing events within a date range,
   * and showing event status at a particular time.
   *
   * @param type the type of query operation to perform
   * @return the corresponding query strategy implementation
   * @throws IllegalArgumentException if the query type is unknown or not supported
   */
  public static Iquery getStrategy(QueryEventDto.QueryType type) {
    switch (type) {
      case PRINT_ON_DATE:
        return new PrintOnDate();
      case PRINT_IN_RANGE:
        return new PrintInRange();
      case SHOW_STATUS_AT:
        return new ShowStatusAt();
      default:
        throw new IllegalArgumentException("Unknown query command type: " + type);
    }
  }
}
