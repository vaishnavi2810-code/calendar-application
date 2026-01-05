package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.EventController;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import mocks.MockCommand;
import mocks.MockCommandParserService;
import mocks.MockParser;
import org.junit.Before;
import org.junit.Test;

/**
 * test to check controller loop.
 */
public class EventControllerRunLoopTest {

  private EventController controller;
  private CapturingView mockView;
  private ScriptedInputSource mockInput;
  private MockCommandParserService parserService;
  private CalendarModel service;

  /**
   * setup.
   */
  @Before
  public void setUp() {
    mockView = new CapturingView();
    mockInput = new ScriptedInputSource();
    service = new CalendarModel(new CalendarCollection());
    parserService = new MockCommandParserService();
    controller = new EventController(mockInput, service, parserService, mockView);
  }

  @Test
  public void testRunDisplaysWelcomeAndExits() {
    mockInput.addCommand("exit");
    controller.run();
    assertEquals("Welcome to MVCalendar!", mockView.displayMessages.get(0));
    assertEquals("Enter commands, or 'exit' to quit.", mockView.displayMessages.get(1));
    assertEquals("Goodbye!", mockView.displayMessages.get(2));
    assertTrue("Input source should be closed", mockInput.isClosed);
  }


  @Test
  public void testRunDisplaysErrorForException() {
    mockInput.addCommand("fail command");
    mockInput.addCommand("exit");
    MockCommand failCmd = new MockCommand();
    failCmd.setExceptionToThrow(new RuntimeException("Custom Error Message"));
    MockParser parser = new MockParser("fail", failCmd);
    parserService.addParser(parser);
    controller.run();
    assertNotNull("Should have displayed an error", mockView.lastErrorMessage);
    assertEquals("Custom Error Message", mockView.lastErrorMessage);
  }

  @Test
  public void testRunHandlesNullCommandFromInput() {
    IinputSource brokenInput = new IinputSource() {
      @Override public boolean hasMoreCommands() {
        return true;
      }

      @Override public String getNextCommand() {
        return null;
      }

      @Override public void close() {}
    };
    EventController brokenController = new EventController(
        brokenInput, service, parserService, mockView);
    brokenController.run();
    assertEquals("Welcome to MVCalendar!", mockView.displayMessages.get(0));
    assertEquals(2, mockView.displayMessages.size());
  }

  /**
  * Mock View that captures output for verification.
  */
  private static class CapturingView implements Iview {
    List<String> displayMessages = new ArrayList<>();
    String lastErrorMessage;
    IresultDto lastResult;

    @Override
    public void display(String message) {
      displayMessages.add(message);
    }

    @Override
    public void displayError(String message) {
      lastErrorMessage = message;
    }

    @Override
    public void displayResult(IresultDto result) {
      lastResult = result;
    }
  }

  /**
  * Mock Input that replies with a scripted list of commands.
  */
  private static class ScriptedInputSource implements IinputSource {
    private final Queue<String> commands = new LinkedList<>();
    boolean isClosed = false;

    void addCommand(String cmd) {
      commands.add(cmd);
    }

    @Override
    public String getNextCommand() {
      return commands.poll();
    }

    @Override
    public boolean hasMoreCommands() {
      return !commands.isEmpty();
    }

    @Override
    public void close() {
      isClosed = true;
    }
  }
}