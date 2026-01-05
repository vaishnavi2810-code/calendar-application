package command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.command.EditEventCommand;
import calendar.dto.EditEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import org.junit.Test;

/**
 * edit event command test class.
 */
public class EditEventCommandTest {

  @Test
  public void testExecuteReturnsSuccessMessage() throws Exception {
    CalendarModel safeService = new CalendarModel(new CalendarCollection()) {
      @Override
      public void editEvent(EditEventDto dto) {
      }
    };
    EditEventCommand command = new EditEventCommand(null, safeService);
    IresultDto result = command.execute();
    assertNotNull("Command result should not be null", result);
    assertEquals("Event edited successfully.", ((SimpleMessageDto) result).getMessage());
  }
}