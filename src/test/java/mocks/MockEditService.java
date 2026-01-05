package mocks;

import calendar.dto.EditEventDto;
import calendar.interfacetypes.Iedit;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.Set;

/**
 * Mock edit service for testing commands.
 */
public class MockEditService implements Iedit {

  private Exception exceptionToThrow = null;
  private int editCallCount = 0;
  private EditEventDto lastDto;

  /**
   * Sets exception to throw when edit is called.
   */
  public void setExceptionToThrow(Exception e) {
    this.exceptionToThrow = e;
  }

  @Override
  public void edit(EditEventDto dto, Set<Event> allEvents, ZoneId timezone) throws Exception {
    editCallCount++;
    lastDto = dto;

    if (exceptionToThrow != null) {
      throw exceptionToThrow;
    }
  }

  /**
   * Gets number of times edit was called.
   */
  public int getEditCallCount() {
    return editCallCount;
  }

  /**
   * Gets the last DTO passed to edit.
   */
  public EditEventDto getLastDto() {
    return lastDto;
  }
}