package calendar.interfacetypes;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface for edit event dialog data.
 * Enables testing without GUI instantiation by abstracting dialog input data.
 */
public interface IeditEventDialogData {

  /**
  * Returns the event subject/name entered by the user.
  *
  * @return the event subject
  */
  String getSubject();

  /**
  * Returns the start date of the event.
  *
  * @return the start date
  */
  LocalDate getStartDate();

  /**
  * Returns the start time of the event.
  *
  * @return the start time
  */
  LocalTime getStartTime();

  /**
  * Returns the end date of the event.
  *
  * @return the end date
  */
  LocalDate getEndDate();

  /**
  * Returns the end time of the event.
  *
  * @return the end time
  */
  LocalTime getEndTime();

  /**
  * Returns the location of the event.
  *
  * @return the event location, or empty string if not set
  */
  String getEventLocation();

  /**
  * Returns the description of the event.
  *
  * @return the event description, or empty string if not set
  */
  String getDescription();

  /**
  * Returns the edit scope selected by the user.
  * Possible values: "single", "series", "forward".
  *
  * @return the edit scope
  */
  String getEditScope();

  /**
  * Returns the original event being edited.
  *
  * @return the original event
  */
  Event getOriginalEvent();

  /**
  * Returns whether the user confirmed the edit action.
  *
  * @return true if user clicked save/confirm, false if cancelled
  */
  boolean isConfirmed();
}