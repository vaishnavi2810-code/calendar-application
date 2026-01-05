package calendar.interfacetypes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Interface for event creation dialog data.
 * Enables testing without GUI instantiation.
 */
public interface IcreateEventDialogData {
  /**
   * Returns the subject (name) of the event entered by the user.
   *
   * @return the trimmed subject string
   */
  String getSubject();

  /**
   * Returns whether the "All-day event" checkbox is selected.
   *
   * @return true if the event is all-day, false otherwise
   */
  boolean isAllDay();

  /**
   * Returns the selected date for the event creation.
   * This is the date passed into the dialog constructor.
   *
   * @return the creation date
   */
  LocalDate getSelectedDate();
  /**
   * Returns the end date selected in the spinner.
   *
   * @return the selected end date
   */

  LocalDate getEndDate();

  /**
   * Returns the start time selected in the spinner.
   *
   * @return the start time
   */
  LocalTime getStartTime();

  /**
   * Returns the end time selected in the spinner.
   *
   * @return the end time
   */
  LocalTime getEndTime();

  /**
   * Returns whether the "Recurring event" checkbox is selected.
   *
   * @return true if the event is recurring, false otherwise
   */
  boolean isRecurring();

  /**
   * Returns the set of weekdays selected for recurrence.
   *
   * @return a set of DayOfWeek enums representing the selected days
   */
  Set<DayOfWeek> getSelectedWeekdays();

  /**
   * Returns whether the recurrence is set to repeat for a specific number of times.
   *
   * @return true if "Repeat" (for N times) is selected, false otherwise
   */
  boolean isRepeatForTimes();

  /**
   * Returns the number of times the event should repeat.
   * Only valid if isRepeatForTimes() returns true.
   *
   * @return the number of repetitions
   */
  int getRepeatTimes();

  /**
   * Returns the date until which the event should repeat.
   * Only valid if isRepeatForTimes() returns false (meaning "Repeat until" is selected).
   *
   * @return the recurrence end date
   */
  LocalDate getRepeatUntilDate();
}