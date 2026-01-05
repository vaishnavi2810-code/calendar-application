package command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.command.CopyEventCommand;
import calendar.dto.CopyEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import org.junit.Test;

/**
 * class to test copy events.
 */
public class CopyEventCommandTest {

  @Test
  public void testExecuteReturnsSuccessMessage() throws Exception {
    CalendarModel dummyService = new CalendarModel(new CalendarCollection()) {
      @Override
      public void copyEvent(CopyEventDto dto) {
      }
    };

    CopyEventCommand command = new CopyEventCommand(null, dummyService);
    IresultDto result = command.execute();
    assertNotNull("Command should return a result object", result);
    assertEquals("Event(s) copied successfully.", ((SimpleMessageDto) result).getMessage());
  }
}