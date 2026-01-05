package command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.command.CalendarCommand;
import calendar.dto.CalendarDto;
import calendar.dto.SimpleMessageDto;
import calendar.interfacetypes.IresultDto;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * calendar command testing script.
 */
public class CalendarCommandTest {

  private CalendarModel service;
  private CalendarCollection repository;

  /**
  * to set up test cases.
  */
  @Before
  public void setUp() {
    repository = new CalendarCollection();
    service = new CalendarModel(repository);
  }

  @Test
  public void testExecuteCreateCalendar() throws Exception {
    CalendarDto dto = CalendarDto.createCalendar("Work", "UTC");
    CalendarCommand command1 = new CalendarCommand(dto, service);
    IresultDto result1 = command1.execute();
    CalendarDto dto1 = CalendarDto.useCalendar("Work");
    CalendarCommand command2 = new CalendarCommand(dto1, service);
    IresultDto result2 = command2.execute();
    assertTrue("Calendar should exist in repository", repository.existsByName("Work"));
    assertEquals("Active calendar should be set", "Work", service.getActiveCalendar());
    assertNotNull(result1);
    assertEquals("Calendar 'Work' created.", ((SimpleMessageDto) result1).getMessage());
  }

  @Test
  public void testExecuteUseCalendar() throws Exception {
    service.createNewCalendar("Personal", "UTC");
    CalendarDto dto = CalendarDto.useCalendar("Personal");
    CalendarCommand command = new CalendarCommand(dto, service);
    IresultDto result = command.execute();
    assertEquals("Active calendar should be switched", "Personal", service.getActiveCalendar());
    assertEquals("Active calendar set to 'Personal'.", ((SimpleMessageDto) result).getMessage());
  }

  @Test
  public void testExecuteEditCalendar() throws Exception {
    service.createNewCalendar("OldName", "UTC");
    CalendarDto dto = CalendarDto.editCalendar("OldName", "name", "NewName");
    CalendarCommand command = new CalendarCommand(dto, service);
    IresultDto result = command.execute();
    assertTrue("New name should exist", repository.existsByName("NewName"));
    assertEquals("Calendar 'OldName' updated.", ((SimpleMessageDto) result).getMessage());
  }

  @Test
  public void testExecuteThrowsExceptionWhenStrategyIsMissing() throws Exception {
    CalendarDto dto = CalendarDto.createCalendar("TestCal", "UTC");
    CalendarCommand command = new CalendarCommand(dto, service);
    Field mapField = CalendarCommand.class.getDeclaredField("strategyMap");
    mapField.setAccessible(true);
    Map<?, ?> internalMap = (Map<?, ?>) mapField.get(command);
    internalMap.clear();
    Exception exception = assertThrows(Exception.class, () -> {
      command.execute();
    });
    assertTrue(exception.getMessage().contains("No strategy found"));
    assertTrue(exception.getMessage().contains("CREATE_CALENDAR"));
  }
}
