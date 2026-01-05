package classevent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;


/**
 * Tests the equals() and hashCode() methods of the Event class.
 * Verifies that Event equality is based on subject, start time, and end time only.
 */
public class EventEqualsTest {

  private Event makeEvent(String subject, String start, String end) {
    return new EventBuilder()
        .setSubject(subject)
        .setStartDateTime(ZonedDateTime.parse(start))
        .setEndDateTime(ZonedDateTime.parse(end))
        .setSeriesId("ABC123")
        .setDescription("Some desc")
        .setLocation("Room A")
        .setStatus("Confirmed")
        .build();
  }

  @Test
  public void testEqualsSameObject() {
    Event e = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    assertTrue(e.equals(e));
  }

  @Test
  public void testEqualsNull() {
    Event e = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    assertFalse(e.equals(null));
  }

  @Test
  public void testEqualsDifferentClass() {
    Event e = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    assertFalse(e.equals("Not an Event"));
  }

  @Test
  public void testEqualsSameValues() {
    Event e1 = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    Event e2 = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    assertTrue(e1.equals(e2));
    assertTrue(e2.equals(e1));
  }

  @Test
  public void testEqualsDifferentSubject() {
    Event e1 = makeEvent("Meeting A", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    Event e2 = makeEvent("Meeting B", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    assertFalse(e1.equals(e2));
  }

  @Test
  public void testEqualsDifferentStartTime() {
    Event e1 = makeEvent("Meeting", "2025-12-01T09:00Z", "2025-12-01T11:00Z");
    Event e2 = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    assertFalse(e1.equals(e2));
  }

  @Test
  public void testEqualsDifferentEndTime() {
    Event e1 = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T11:00Z");
    Event e2 = makeEvent("Meeting", "2025-12-01T10:00Z", "2025-12-01T12:00Z");
    assertNotEquals(e1, e2);
  }


  @Test
  public void testHashCodeDifferentEventsHaveDifferentHashCodes() {
    ZonedDateTime start = ZonedDateTime.parse("2025-11-15T10:00:00Z");
    ZonedDateTime end = ZonedDateTime.parse("2025-11-15T11:00:00Z");

    Event event1 = new EventBuilder()
            .setSubject("Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();

    Event event2 = new EventBuilder()
            .setSubject("Lunch")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();

    Event event3 = new EventBuilder()
            .setSubject("Meeting")
            .setStartDateTime(start.plusHours(1))
            .setEndDateTime(end)
            .build();

    Event event4 = new EventBuilder()
            .setSubject("Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end.plusHours(1))
            .build();

    Set<Integer> hashCodes = new HashSet<>();
    hashCodes.add(event1.hashCode());
    hashCodes.add(event2.hashCode());
    hashCodes.add(event3.hashCode());
    hashCodes.add(event4.hashCode());
    assertTrue("Different events should have different hash codes",
            hashCodes.size() > 1);
  }

  @Test
  public void testEqualsIgnoresOtherFields() {
    Event e1 = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(ZonedDateTime.parse("2025-12-01T10:00Z"))
        .setEndDateTime(ZonedDateTime.parse("2025-12-01T11:00Z"))
        .setSeriesId("A")
        .setDescription("Desc A")
        .setLocation("Room A")
        .setStatus("Tentative")
        .build();

    Event e2 = new EventBuilder()
        .setSubject("Meeting")
        .setStartDateTime(ZonedDateTime.parse("2025-12-01T10:00Z"))
        .setEndDateTime(ZonedDateTime.parse("2025-12-01T11:00Z"))
        .setSeriesId("B")
        .setDescription("Desc B")
        .setLocation("Room B")
        .setStatus("Confirmed")
        .build();
    assertTrue(e1.equals(e2));
  }
}
