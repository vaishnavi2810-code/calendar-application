package model.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite for the CreateAllDaySingle class.
 * Verifies the creation of single all-day events, including successful creation
 * with proper attributes and collision detection for duplicate events.
 */
public class CreateAllDaySingleTest {
  @Test
  public void createAllDaySingleTestOk() throws Exception {
    Icreate createService = new calendar.strategy.CreateAllDaySingle();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createAllDaySingleDtoWithQuotes(
            "Meeting",
            "2025-11-01"
    );
    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertNotNull("Result should not be null", result);
    assertEquals("Should create exactly 1 event", 1, result.size());
    Event createdEvent = result.iterator().next();
    assertEquals("Subject should be 'Meeting'", "Meeting", createdEvent.getSubject());
    assertNotNull("Start time should not be null", createdEvent.getStartDateTime());
    assertNotNull("End time should not be null", createdEvent.getEndDateTime());
    System.out.println("Created event: " + createdEvent.getSubject());
    System.out.println("  Test passed!");
    System.out.println("  Created event: " + createdEvent.getSubject());
    System.out.println("  Start: " + createdEvent.getStartDateTime());
    System.out.println("  End: " + createdEvent.getEndDateTime());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void createAllDaySingleTestError() throws Exception {
    Icreate createService = new calendar.strategy.CreateAllDaySingle();
    Set<Event> existingEvents = new HashSet<>();
    thrown.expect(Exception.class);
    thrown.expectMessage("Event already exists");
    CreateEventDto dto = TestDtoBuilder.createAllDaySingleDtoWithQuotes(
            "Meeting",
            "2025-11-01"
    );
    Set<Event> result = createService.create(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(result);
    CreateEventDto dto1 = TestDtoBuilder.createAllDaySingleDtoWithoutQuotes(
            "Meeting",
            "2025-11-01"
    );
    Set<Event> result1 = createService.create(dto1, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    assertNotNull("Result should not be null", result);
    assertEquals("Should create exactly 1 event", 1, result.size());
    Event createdEvent = result.iterator().next();
    assertEquals("Subject should be 'Meeting'", "Meeting", createdEvent.getSubject());
    assertNotNull("Start time should not be null", createdEvent.getStartDateTime());
    assertNotNull("End time should not be null", createdEvent.getEndDateTime());
    System.out.println("Created event: " + createdEvent.getSubject());
    System.out.println("  Test passed!");
    System.out.println("  Created event: " + createdEvent.getSubject());
    System.out.println("  Start: " + createdEvent.getStartDateTime());
    System.out.println("  End: " + createdEvent.getEndDateTime());
  }
}
