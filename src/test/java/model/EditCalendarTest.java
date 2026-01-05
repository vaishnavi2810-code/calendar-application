package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.dto.CalendarDto;
import calendar.strategy.EditCalendar;
import org.junit.Test;

/**
 * class to check EditCalendarTest.
 */
public class EditCalendarTest {

  @Test
  public void testExecuteThrowsExceptionForUnknownProperty() {
    EditCalendar strategy = new EditCalendar();
    CalendarDto dto = CalendarDto.editCalendar("TestCal", "color", "Red");
    Exception ex = assertThrows(Exception.class, () -> {
      strategy.execute(dto, null);
    });
    assertEquals("Error: Unknown property", ex.getMessage());
  }
}