package model.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.strategy.CreateEventSingle;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite for the CreateEventSingle class.
 * Verifies the creation of single timed events, including successful creation
 * with proper time validation, collision detection, and proper handling of
 * quoted vs. unquoted subjects.
 */
public class CreateEventSingleTest {

  @Test
  public void testCreateSingleEventBasicSuccessWithQuotes() throws Exception {
    Icreate createService = new CreateEventSingle();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Meeting",
        "2025-11-01T10:00",
        "2025-11-01T11:00"
    );
    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertNotNull("Result should not be null", result);
    assertEquals("Should create exactly 1 event", 1, result.size());
    Event createdEvent = result.iterator().next();
    assertEquals("Subject should be 'Meeting'", "Meeting", createdEvent.getSubject());
    assertNotNull("Start time should not be null", createdEvent.getStartDateTime());
    assertNotNull("End time should not be null", createdEvent.getEndDateTime());
    System.out.println("  Test passed!");
    System.out.println("  Created event: " + createdEvent.getSubject());
    System.out.println("  Start: " + createdEvent.getStartDateTime());
    System.out.println("  End: " + createdEvent.getEndDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSingleEventBasicSuccessWithoutQuotes() throws Exception {
    Icreate createService = new CreateEventSingle();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithoutQuotes(
            "Meeting Hello",
            "2025-11-01T10:00",
            "2025-11-01T11:00"
    );
    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertNotNull("Result should not be null", result);
    assertEquals("Should create exactly 1 event", 1, result.size());
    Event createdEvent = result.iterator().next();
    assertEquals("Subject should be 'Meeting'", "Meeting", createdEvent.getSubject());
    assertNotNull("Start time should not be null", createdEvent.getStartDateTime());
    assertNotNull("End time should not be null", createdEvent.getEndDateTime());
    System.out.println("  Test passed!");
    System.out.println("  Created event: " + createdEvent.getSubject());
    System.out.println("  Start: " + createdEvent.getStartDateTime());
    System.out.println("  End: " + createdEvent.getEndDateTime());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateSingleEvent_collision() throws Exception {
    Icreate createService = new CreateEventSingle();
    Set<Event> existingEvents = new HashSet<>();
    thrown.expect(Exception.class);
    thrown.expectMessage("Event already exists");
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
            "Meeting",
            "2025-11-01T10:00",
            "2025-11-01T11:00"
    );

    CreateEventDto dto1 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
            "Meeting",
            "2025-11-01T10:00",
            "2025-11-01T11:00"
    );
    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(result);
    Set<Event> result2 = createService.create(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));
  }

  @Test()
  public void testCreateSingleEventStartGreaterThanEnd() throws Exception {
    Icreate createService = new CreateEventSingle();
    Set<Event> existingEvents = new HashSet<>();
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: Event end time cannot be before its start time.");
    CreateEventDto dto = TestDtoBuilder.createTimedSingleDtoWithQuotes(
            "Meeting",
            "2025-11-02T10:00",
            "2025-11-01T11:00"
    );
    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(result);
  }
}