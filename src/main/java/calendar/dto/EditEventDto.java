package calendar.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Data Transfer Object for editing calendar events. Encapsulates all the parameters
 * needed to edit existing events including the scope of the edit (single instance,
 * forward occurrences, or entire series), the target event identification, the property
 * to be modified, and the new value. The DTO extracts and stores relevant information
 * from regex matcher groups based on the edit type.
 */
public class EditEventDto {

  /**
   * Enumeration of different edit scopes that determine which event occurrences
   * should be modified by the edit operation.
   */
  public enum EditType {
    EDIT_SINGLE,
    EDIT_FORWARD,
    EDIT_SERIES
  }

  private final EditType editType;
  private final String property;
  private final String targetSubject;
  private final String targetStartDateTime;
  private final String targetEndDateTime;
  private final String newValue;
  private final Map<String, String> propertyChanges;

  /**
   * Creates an EditEventDto by extracting relevant fields from the provided matcher
   * based on the edit type. The matcher should contain named groups for the event
   * identification fields (subject, start, end), the property to edit, and the new value.
   *
   * @param editType the scope of the edit operation
   * @param matcher the regex matcher containing the parsed edit command data with named groups
   */
  public EditEventDto(EditType editType, Matcher matcher) {
    this.editType = editType;
    this.property = getGroup(matcher, "property");
    this.targetSubject = getGroup(matcher, "subject");
    this.targetStartDateTime = getGroup(matcher, "start");
    this.targetEndDateTime = getGroup(matcher, "end");
    this.newValue = getGroup(matcher, "newvalue");
    this.propertyChanges = new HashMap<>();
  }

  /**
   * Creates an EditEventDto directly from parameters for GUI mode.
   * This constructor handles both single-property and multi-property edits.
   *
   * @param editType the scope of the edit operation
   * @param targetSubject the subject of the event to edit
   * @param targetStartDateTime start date-time in YYYY-MM-DDTHH:mm format
   * @param targetEndDateTime end date-time in YYYY-MM-DDTHH:mm format
   * @param propertyChanges map of property names to new values
   */
  private EditEventDto(EditType editType,
                       String targetSubject,
                       String targetStartDateTime,
                       String targetEndDateTime,
                       Map<String, String> propertyChanges) {
    this.editType = editType;
    this.targetSubject = targetSubject;
    this.targetStartDateTime = targetStartDateTime;
    this.targetEndDateTime = targetEndDateTime;
    this.propertyChanges = propertyChanges;
    if (propertyChanges != null && propertyChanges.size() == 1) {
      this.property = propertyChanges.keySet().iterator().next();
      this.newValue = propertyChanges.values().iterator().next();
    } else {
      this.property = null;
      this.newValue = null;
    }
  }

  /**
   * Extracts a named group value from the matcher and removes surrounding quotes
   * for subject and newvalue fields. Returns null if the group doesn't exist or
   * if an IllegalArgumentException is thrown.
   *
   * @param matcher the regex matcher containing named groups
   * @param groupName the name of the group to extract
   * @return the extracted group value with quotes removed if applicable, or null if not found
   */
  private String getGroup(Matcher matcher, String groupName) {
    try {
      String val = matcher.group(groupName);
      if (val == null) {
        return null;
      }
      if ((groupName.equals("subject") || groupName.equals("newvalue"))
          && val.length() >= 2
          && val.startsWith("\"")
          && val.endsWith("\"")) {
        return val.substring(1, val.length() - 1);
      }
      return val;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Creates a DTO for editing a single event instance.
   * Supports both single-property and multi-property edits.
   *
   * @param targetSubject the subject of the event to edit
   * @param targetStartDateTime start time in YYYY-MM-DDTHH:mm format
   * @param targetEndDateTime end time in YYYY-MM-DDTHH:mm format
   * @param propertyChanges map of property names to new values
   * @return configured EditEventDto
   */
  public static EditEventDto editSingle(String targetSubject,
                                        String targetStartDateTime,
                                        String targetEndDateTime,
                                        Map<String, String> propertyChanges) {
    return new EditEventDto(EditType.EDIT_SINGLE, targetSubject,
        targetStartDateTime, targetEndDateTime, propertyChanges);
  }


  /**
   * Creates a DTO for editing all events in a series.
   * Supports both single-property and multi-property edits.
   *
   * @param targetSubject the subject of the series to edit
   * @param targetStartDateTime start time of any event in the series
   * @param propertyChanges map of property names to new values
   * @return configured EditEventDto
   */
  public static EditEventDto editSeries(String targetSubject,
                                        String targetStartDateTime,
                                        Map<String, String> propertyChanges) {
    return new EditEventDto(EditType.EDIT_SERIES, targetSubject,
        targetStartDateTime, null, propertyChanges);
  }

  /**
   * Creates a DTO for editing this event and all following events in a series.
   * Supports both single-property and multi-property edits.
   *
   * @param targetSubject the subject of the series to edit
   * @param targetStartDateTime start time to begin editing from
   * @param propertyChanges map of property names to new values
   * @return configured EditEventDto
   */
  public static EditEventDto editForward(String targetSubject,
                                         String targetStartDateTime,
                                         Map<String, String> propertyChanges) {
    return new EditEventDto(EditType.EDIT_FORWARD, targetSubject,
        targetStartDateTime, null, propertyChanges);
  }

  /**
   * Checks if this DTO contains multiple property changes.
   *
   * @return true if multiple properties are being changed, false otherwise
   */
  public boolean hasMultipleProperties() {
    return propertyChanges != null && propertyChanges.size() > 1;
  }

  public Map<String, String> getPropertyChanges() {
    return propertyChanges;
  }

  public EditType getEditType() {
    return editType;
  }

  public String getProperty() {
    return property;
  }

  public String getTargetSubject() {
    return targetSubject;
  }

  public String getTargetStartDateTime() {
    return targetStartDateTime;
  }

  public String getTargetEndDateTime() {
    return targetEndDateTime;
  }

  public String getNewValue() {
    return newValue;
  }
}