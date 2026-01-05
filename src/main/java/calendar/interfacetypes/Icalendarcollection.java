package calendar.interfacetypes;

import calendar.model.Calendar;
import java.util.Set;

/**
 * Interface for repository operations related to calendars.
 */
public interface Icalendarcollection {

  /**
   * Finds a calendar by its name.
   *
   * @param name the name of the calendar to find
   * @return the calendar model if found, otherwise null
   */
  Calendar findByName(String name);

  /**
   * Saves or updates a calendar model in the repository.
   *
   * @param calendar the calendar model to save
   */
  void save(Calendar calendar);

  /**
   * Checks whether a calendar exists by its name.
   *
   * @param name the name of the calendar
   * @return true if the calendar exists, false otherwise
   */
  boolean existsByName(String name);

  /**
   * Deletes a calendar by its name.
   *
   * @param name the name of the calendar to delete
   */
  void deleteByName(String name);

  /**
   * Retrieves the names of all calendars in the repository.
   *
   * @return a set containing all calendar names
   */
  Set<String> getAllCalendarNames();
}
