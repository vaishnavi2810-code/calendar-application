package command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.command.CreateEventCommand;
import calendar.dto.CreateEventDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import org.junit.Test;

/**
 * tests create event command.
 */
public class CreateEventCommandTest {
  @Test
  public void testExecuteReturnsSuccessMessage() throws Exception {
    CalendarModel safeService = new CalendarModel(new CalendarCollection()) {
      @Override
      public void createEvent(CreateEventDto dto) {
      }
    };

    CreateEventDto dto = CreateEventDto.allDaySingle("My Event", "2023-01-01");
    CreateEventCommand command = new CreateEventCommand(dto, safeService);
    IresultDto result = command.execute();
    assertNotNull("Command result should not be null", result);
    assertEquals("Event 'My Event' created successfully.",
            ((SimpleMessageDto) result).getMessage());
  }
}