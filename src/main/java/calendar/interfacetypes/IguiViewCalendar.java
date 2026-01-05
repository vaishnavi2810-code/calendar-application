package calendar.interfacetypes;

import calendar.dto.QueryResultDto;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

/**
 * Interface for GUI view operations.
 * Enables testing without JFrame instantiation by abstracting view behavior.
 */
public interface IguiViewCalendar {

  /**
   * Returns the frame for dialog parenting.
   * May return null in test environments.
   *
   * @return the JFrame instance, or null if not available
   */
  JFrame getFrame();

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  void showError(String message);

  /**
   * Displays a success message to the user.
   *
   * @param message the success message to display
   */
  void showSuccess(String message);

  /**
   * Updates the status bar message.
   *
   * @param message the status message to display
   */
  void setStatus(String message);

  /**
   * Updates the calendar dropdown list with available calendars.
   *
   * @param names list of calendar names
   * @param active the currently active calendar name
   */
  void updateCalendarList(List<String> names, String active);

  /**
   * Updates the calendar grid view for the specified month and selected date.
   *
   * @param month the year-month to display
   * @param selectedDate the currently selected date
   */
  void updateCalendarView(YearMonth month, LocalDate selectedDate);

  /**
   * Displays events for a query result on the specified date.
   *
   * @param date the date for which events are displayed
   * @param result the query result containing events
   */
  void displayEventsForQueryResult(LocalDate date, QueryResultDto result);

  /**
   * Returns the currently selected date in the calendar view.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate();

  /**
   * Returns the calendar selection dropdown component.
   *
   * @return the calendar dropdown
   */
  JComboBox<String> getCalendarDropdown();

  /**
   * Returns the create calendar button component.
   *
   * @return the create calendar button
   */
  JButton getCreateCalendarButton();

  /**
   * Returns the edit calendar button component.
   *
   * @return the edit calendar button
   */
  JButton getEditCalendarButton();

  /**
   * Returns the create event button component.
   *
   * @return the create event button
   */
  JButton getCreateEventButton();

  /**
   * Returns the edit event button component.
   *
   * @return the edit event button
   */
  JButton getEditEventButton();

  /**
   * Registers navigation button listeners for calendar navigation.
   *
   * @param prev listener for previous month button
   * @param next listener for next month button
   * @param today listener for today button
   */
  void addNavigationListeners(ActionListener prev, ActionListener next, ActionListener today);

  /**
   * Sets the listener for date selection events in the calendar grid.
   *
   * @param listener consumer that receives the selected date
   */
  void setDateSelectionListener(Consumer<LocalDate> listener);

  /**
   * Returns the edit selcted event button component.
   *
   * @return the edit selcted event button
   */
  JButton getEditEventsBySearchButton();
}