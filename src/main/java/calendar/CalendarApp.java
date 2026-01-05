package calendar;

import calendar.interfacetypes.Icontroller;

/**
 * Main application class for the MVCalendar system.
 * Simple facade that delegates to the controller.
 */
public class CalendarApp {

  private final Icontroller controller;

  /**
   * Creates a CalendarApp with the specified controller.
   *
   * @param controller the controller that will handle the application
   */
  public CalendarApp(Icontroller controller) {
    this.controller = controller;
  }

  /**
   * Starts the calendar application by running the controller.
   */
  public void start() {
    controller.run();
  }
}