package calendar.controller;

import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Icontroller;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.IresultDto;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarModel;
import calendar.service.CommandParserService;

/**
 * Main controller for the calendar application.
 * Controller handles command processing and coordinates with View for output.
 * Does NOT handle command-line argument parsing - that's done in CalendarRunner.
 */
public class EventController implements Icontroller {

  private final CommandParserService parserService;
  private final CalendarModel service;
  private final Iview view;
  private final IinputSource inputSource;

  /**
   * Creates an EventController with the specified components.
   *
   * @param inputSource the input source for reading commands
   * @param service the calendar service (Model)
   * @param parserService the command parser service
   * @param view the view for output only
   */
  public EventController(IinputSource inputSource, CalendarModel service,
                         CommandParserService parserService, Iview view) {
    this.inputSource = inputSource;
    this.service = service;
    this.parserService = parserService;
    this.view = view;
  }

  @Override
  public void run() {
    view.display("Welcome to MVCalendar!");
    view.display("Enter commands, or 'exit' to quit.");

    while (inputSource.hasMoreCommands()) {
      try {
        String command = inputSource.getNextCommand();

        if (command == null) {
          break;
        }

        if (command.equalsIgnoreCase("exit")) {
          view.display("Goodbye!");
          break;
        }

        IresultDto result = processCommand(command);
        view.displayResult(result);

      } catch (Exception e) {
        view.displayError(e.getMessage());
      }
    }

    inputSource.close();
  }

  @Override
  public IresultDto processCommand(String command) throws Exception {
    Icommand cmd = parserService.parse(command, service);
    return cmd.execute();
  }
}