package model.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.strategy.CreateAllDayRecurringN;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite for the CreateAllDayRecurringN class.
 * Verifies the creation of recurring all-day events with a fixed number of occurrences,
 * including successful creation, collision detection, and edge cases.
 */
public class CreateAllDayRecurringnTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Tests successful creation of recurring all-day events with a fixed number of occurrences.
   * Verifies that the correct number of events are created on the specified weekdays,
   * all events share the same series ID, have correct all-day times (8 AM to 5 PM),
   * and match the expected subject and day of week.
   */
  @Test
  public void testCreateAllDayRecurringForSuccess() throws Exception {
    Icreate createService = new CreateAllDayRecurringN();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createAllDayRecurringForDtoWithQuotes(
                "Volunteering",
                "2025-11-15",
                "S",
                "3"
    );

    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertEquals("Should create 3 events", 3, result.size());
    String seriesId = result.iterator().next().getSeriesId();
    assertNotNull("seriesId should not be null", seriesId);

    for (Event event : result) {
      assertEquals("Subject should match", "Volunteering", event.getSubject());
      assertEquals("All-day should start at 8am", 8, event.getStartDateTime().getHour());
      assertEquals("All-day should end at 5pm", 17, event.getEndDateTime().getHour());
      assertEquals("All events should have the same seriesId", seriesId, event.getSeriesId());
      assertEquals("Day should be Saturday", DayOfWeek.SATURDAY,
          event.getStartDateTime().getDayOfWeek());
    }
    System.out.println("✓ TEST PASSED: Created " + result.size() + " recurring all-day events.");
  }

  @Test
  public void testCreateAllDayRecurringForCollision() throws Exception {
    Icreate createService = new CreateAllDayRecurringN();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createAllDayRecurringForDtoWithQuotes(
                "Volunteering",
                "2025-11-15",
                "S",
                "3"
    );

    Set<Event> firstSet = createService.create(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(firstSet);

    thrown.expect(Exception.class);
    thrown.expectMessage("Event already exists");

    createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void testCreateAllDayRecurringForOneTime() throws Exception {
    Icreate createService = new CreateAllDayRecurringN();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createAllDayRecurringForDtoWithQuotes(
                "One Time Thing",
                "2025-11-18",
                "TR",
                "1"
    );

    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertEquals("Should create 1 event", 1, result.size());
    Event event = result.iterator().next();
    assertEquals("Subject should match", "One Time Thing", event.getSubject());
    assertEquals("Day should be Tuesday", DayOfWeek.TUESDAY,
        event.getStartDateTime().getDayOfWeek());
  }
}