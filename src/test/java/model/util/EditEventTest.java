package model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.dto.EditEventDto;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.util.EditEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * EditEvent util test.
 */
public class EditEventTest {

  private ZoneId zone;
  private Event originalEvent;

  /**
   * setup for the test.
   */
  @Before
  public void setUp() {
    zone = ZoneId.of("UTC");
    originalEvent = new EventBuilder()
                .setSubject("Original")
                .setStartDateTime(ZonedDateTime.of(2023, 1, 1, 10, 0, 0, 0, zone))
                .setEndDateTime(ZonedDateTime.of(2023, 1, 1, 11, 0, 0, 0, zone))
                .build();
  }

  /**
  * Scenario 1: Tests logic for 'existingEvents' loop.
  * If we try to rename 'Original' to 'Conflict', and 'Conflict' already exists
  * in the calendar, it should throw an exception.
  */
  @Test
  public void testCreateModifiedEventMultiThrowsOnExistingDuplicate() {
    Event conflictEvent = new EventBuilder()
                .setSubject("Conflict")
                .setStartDateTime(originalEvent.getStartDateTime())
                .setEndDateTime(originalEvent.getEndDateTime())
                .build();
    Set<Event> existingEvents = new HashSet<>();
    existingEvents.add(conflictEvent);
    Map<String, String> changes = new HashMap<>();
    changes.put("subject", "Conflict");
    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEventMulti(
                    originalEvent,
                    changes,
                    null,
                    zone,
                    existingEvents,
                    new HashSet<>(),
                    new HashSet<>()
      );
    });
    assertTrue(ex.getMessage().contains("already exists"));
  }

  /**
  * Scenario 2: Tests the 'continue' branch.
  * "if (eventsToRemove.contains(existing)) { continue; }"
  * If the conflicting event is also marked for deletion (eventsToRemove),
  * the operation should SUCCESS (no exception).
  */
  @Test
  public void testCreateModifiedEventMultiIgnoresRemovedEvents() throws Exception {
    Event conflictEvent = new EventBuilder()
                .setSubject("Conflict")
                .setStartDateTime(originalEvent.getStartDateTime())
                .setEndDateTime(originalEvent.getEndDateTime())
                .build();

    Set<Event> existingEvents = new HashSet<>();
    existingEvents.add(conflictEvent);
    Set<Event> eventsToRemove = new HashSet<>();
    eventsToRemove.add(conflictEvent);
    Map<String, String> changes = new HashMap<>();
    changes.put("subject", "Conflict");
    Event result = EditEvent.createModifiedEventMulti(
                originalEvent,
                changes,
                null,
                zone,
                existingEvents,
                eventsToRemove,
                new HashSet<>()
    );
    assertNotNull(result);
    assertEquals("Conflict", result.getSubject());
  }

  /**
  * Scenario 3: Tests logic for 'eventsToCommit' loop.
  * If we try to create two identical events in the same batch (e.g., repeating series),
  * it should throw an exception to prevent self-collision.
  */
  @Test
  public void testCreateModifiedEventMultiThrowsOnBatchDuplicate() {
    Event newBatchEvent = new EventBuilder()
                .setSubject("New Batch Event")
                .setStartDateTime(originalEvent.getStartDateTime())
                .setEndDateTime(originalEvent.getEndDateTime())
                .build();

    Set<Event> eventsToCommit = new HashSet<>();
    eventsToCommit.add(newBatchEvent);
    Map<String, String> changes = new HashMap<>();
    changes.put("subject", "New Batch Event");
    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEventMulti(
                    originalEvent,
                    changes,
                    null,
                    zone,
                    new HashSet<>(),
                    new HashSet<>(),
                    eventsToCommit
      );
    });
    assertTrue(ex.getMessage().contains("Would create duplicate events"));
  }

  /**
  * Tests the condition:
  * if (!newStart.toLocalDate().equals(original.getEndDateTime().toLocalDate()))
  * * Scenario: We move the START time to the previous day (Dec 31st).
  * Since the END time is still Jan 1st, the event now spans two days.
  * Logic should forbid this for series events.
  */
  @Test
  public void testEditStartFailsIfSeriesEventSpansMidnight() {
    EditEventDto dto = EditEventDto.editSingle(
                "Series Event",
                "2023-01-01T10:00",
                "2023-01-01T11:00",
                Map.of("start", "2022-12-31T23:00")
    );

    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEvent(originalEvent, dto, "series-123", zone);
    });

    assertTrue(ex.getMessage().contains("must start and end on the same day"));
    assertTrue(ex.getMessage().contains("2022-12-31")); // Start date
    assertTrue(ex.getMessage().contains("2023-01-01")); // End date
  }

  /**
  * Tests the condition:
  * if (!original.getStartDateTime().toLocalDate().equals(newEnd.toLocalDate()))
  * * Scenario: We move the END time to the next day (Jan 2nd).
  * Since the START time is still Jan 1st, the event now spans two days.
  * Logic should forbid this for series events.
  */
  @Test
  public void testEditEndFailsIfSeriesEventSpansMidnight() {
    EditEventDto dto = EditEventDto.editSingle(
                "Series Event",
                "2023-01-01T10:00",
                "2023-01-01T11:00",
                Map.of("end", "2023-01-02T01:00")
    );

    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEvent(originalEvent, dto, "series-123", zone);
    });

    assertTrue(ex.getMessage().contains("must start and end on the same day"));
    assertTrue(ex.getMessage().contains("2023-01-01")); // Start date
    assertTrue(ex.getMessage().contains("2023-01-02")); // End date
  }

  /**
  * Tests the branch where BOTH start and end are modified, but invalid.
  * Expects: "Both start and end times were modified"
  */
  @Test
  public void testMultiEditBothModifiedInvalidOrder() {
    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2023-01-01T12:00");
    changes.put("end", "2023-01-01T11:30");
    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEventMulti(
                    originalEvent,
                    changes,
                    null,
                    zone,
                    new HashSet<>(),
                    new HashSet<>(),
                    new HashSet<>()
      );
    });
    assertTrue(ex.getMessage().contains("Both start and end times were modified"));
  }

  /**
  * Tests the branch where ONLY START is modified to be after the existing end.
  * Expects: "Start time was modified"
  */
  @Test
  public void testMultiEditStartModifiedInvalidOrder() {
    Map<String, String> changes = new HashMap<>();
    changes.put("start", "2023-01-01T11:30");
    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEventMulti(
                    originalEvent,
                    changes,
                    null,
                    zone,
                    new HashSet<>(),
                    new HashSet<>(),
                    new HashSet<>()
      );
    });
    assertTrue(ex.getMessage().contains("Start time was modified"));
  }

  /**
  * Tests the branch where ONLY END is modified to be before the existing start.
  * Expects: "End time was modified"
  */
  @Test
  public void testMultiEditEndModifiedInvalidOrder() {
    Map<String, String> changes = new HashMap<>();
    changes.put("end", "2023-01-01T09:00");
    Exception ex = assertThrows(Exception.class, () -> {
      EditEvent.createModifiedEventMulti(
                    originalEvent,
                    changes,
                    null,
                    zone,
                    new HashSet<>(),
                    new HashSet<>(),
                    new HashSet<>()
      );
    });
    assertTrue(ex.getMessage().contains("End time was modified"));
  }
}