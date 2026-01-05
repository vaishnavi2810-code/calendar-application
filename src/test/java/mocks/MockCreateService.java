package mocks;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * Mock create service for testing commands.
 */
public class MockCreateService implements Icreate {

  private Set<Event> eventsToReturn = new HashSet<>();
  private Exception exceptionToThrow = null;
  private int createCallCount = 0;
  private CreateEventDto lastDto;

  /**
   * Sets the events this mock should return.
   */
  public void setEventsToReturn(Set<Event> events) {
    this.eventsToReturn = events;
  }

  /**
   * Sets exception to throw when create is called.
   */
  public void setExceptionToThrow(Exception e) {
    this.exceptionToThrow = e;
  }

  @Override
  public Set<Event> create(CreateEventDto data, Set<Event> list, ZoneId zoneId) throws Exception {
    createCallCount++;
    lastDto = data;

    if (exceptionToThrow != null) {
      throw exceptionToThrow;
    }

    return eventsToReturn;
  }

  /**
   * Gets number of times create was called.
   */
  public int getCreateCallCount() {
    return createCallCount;
  }

  /**
   * Gets the last DTO passed to create.
   */
  public CreateEventDto getLastDto() {
    return lastDto;
  }
}