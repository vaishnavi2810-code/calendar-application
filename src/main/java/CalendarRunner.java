import calendar.CalendarApp;
import calendar.controller.EventController;
import calendar.controller.SimpleGuiController;
import calendar.factory.InputSourceFactory;
import calendar.interfacetypes.Icalendarcollection;
import calendar.interfacetypes.Icontroller;
import calendar.interfacetypes.IinputSource;
import calendar.interfacetypes.Iview;
import calendar.model.CalendarCollection;
import calendar.model.CalendarModel;
import calendar.service.CommandParserService;
import calendar.view.ConsoleView;
import calendar.view.GuiViewCalendar;
import javax.swing.SwingUtilities;

/**
 * Main runner class for the MVCalendar application.
 * Entry point that creates all components and starts the application.
 * Handles command-line argument parsing.
 */
public class CalendarRunner {

  /**
   * Main entry point for the application.
   * Parses command-line arguments, creates components, and starts the application.
   *
   * @param args command-line arguments (--mode interactive|headless [filename])
   */
  public static void main(String[] args) {
    try {
      if (args.length == 0) {
        SwingUtilities.invokeLater(() -> {
          Icalendarcollection repository = new CalendarCollection();
          CalendarModel service = new CalendarModel(repository);
          GuiViewCalendar view = new GuiViewCalendar();
          SimpleGuiController controller = new SimpleGuiController(service, view);
          view.setVisible(true);
        });
        return;
      }
      if (args.length < 2) {
        throw new Exception("Missing arguments");
      }
      if (!args[0].equalsIgnoreCase("--mode")) {
        throw new Exception("First argument must be --mode");
      }

      String mode = args[1].toLowerCase();
      String filename = null;

      // Validate mode-specific arguments
      if (mode.equals("interactive")) {
        if (args.length != 2) {
          throw new Exception("Interactive mode takes no additional arguments");
        }
        System.out.println("Starting in interactive mode...");
      } else if (mode.equals("headless")) {
        if (args.length != 3) {
          throw new Exception("Headless mode requires exactly one filename argument");
        }
        filename = args[2];
        System.out.println("Starting in headless mode with file: " + filename);
      } else {
        throw new Exception("Invalid mode: " + mode);
      }
      Icalendarcollection repository = new CalendarCollection();
      CalendarModel calendarModel = new CalendarModel(repository);
      CommandParserService parserService = new CommandParserService();
      Iview view = new ConsoleView();
      IinputSource inputSource = InputSourceFactory.createInputSource(mode, filename);
      Icontroller controller = new EventController(inputSource, calendarModel,
          parserService, view);
      CalendarApp calendar = new CalendarApp(controller);
      calendar.start();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      printUsage();
    }
  }

  /**
   * Prints usage information to standard error.
   */
  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  java -jar calendar.jar --mode interactive");
    System.err.println("  java -jar calendar.jar --mode headless <commands_file.txt>");
  }
}