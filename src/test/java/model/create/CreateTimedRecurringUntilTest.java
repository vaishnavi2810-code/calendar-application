package model.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.strategy.CreateTimedRecurringUntil;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite for the CreateTimedRecurringUntil class.
 * Verifies the creation of recurring timed events that continue until a specified end date,
 * including successful creation with correct times and weekdays, collision detection,
 * and validation of time constraints (end after start, same-day events, valid until date).
 */
public class CreateTimedRecurringUntilTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateTimedRecurringUntilSuccess() throws Exception {
    Icreate createService = new CreateTimedRecurringUntil();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createTimedRecurringUntilDtoWithQuotes(
                "Class",
                "2025-11-11T14:00",
                "2025-11-11T16:00",
                "TR",
                "2025-12-10"
    );

    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertEquals("Should create 9 events", 9, result.size());

    String seriesId = result.iterator().next().getSeriesId();
    assertNotNull("seriesId should not be null", seriesId);

    for (Event event : result) {
      assertEquals("Subject should match", "Class", event.getSubject());
      assertEquals("Start hour should be 14", 14, event.getStartDateTime().getHour());
      assertEquals("End hour should be 16", 16, event.getEndDateTime().getHour());
      assertEquals("All events should have the same seriesId", seriesId, event.getSeriesId());
      DayOfWeek day = event.getStartDateTime().getDayOfWeek();
      assertTrue("Day should be Tuesday or Thursday",
                    day == DayOfWeek.TUESDAY
                            ||
                            day == DayOfWeek.THURSDAY);
    }
    System.out.println("✓ TEST PASSED: Created " + result.size() + " recurring timed events.");
  }

  @Test
  public void testCreateTimedRecurringUntilCollision() throws Exception {
    Icreate createService = new CreateTimedRecurringUntil();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createTimedRecurringUntilDtoWithQuotes(
                "Class",
                "2025-11-11T14:00",
                "2025-11-11T16:00",
                "TR",
                "2025-12-10"
    );

    Set<Event> firstSet = createService.create(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(firstSet);

    thrown.expect(Exception.class);
    thrown.expectMessage("Event already exists");

    createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void testCreateTimedRecurringUntilEndBeforeStart() throws Exception {
    Icreate createService = new CreateTimedRecurringUntil();
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: Event end time cannot be before its start time.");

    CreateEventDto dto = TestDtoBuilder.createTimedRecurringUntilDtoWithQuotes(
                "Bad Event",
                "2025-11-11T11:00",
                "2025-11-11T10:00",
                "T",
                "2025-12-10"
    );
    createService.create(dto, new HashSet<>(), ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void testCreateTimedRecurringUntilSpansMultipleDays() throws Exception {
    Icreate createService = new CreateTimedRecurringUntil();
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: Recurring events must start and end on the same day.");

    CreateEventDto dto = TestDtoBuilder.createTimedRecurringUntilDtoWithQuotes(
                "Bad Event",
                "2025-11-11T10:00",
                "2025-11-12T10:00",
                "T",
                "2025-12-10"
    );
    createService.create(dto, new HashSet<>(), ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void testCreateTimedRecurringUntilUntilDateBeforeStartDate() throws Exception {
    Icreate createService = new CreateTimedRecurringUntil();
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: 'until' date cannot be before the event's start date.");

    CreateEventDto dto = TestDtoBuilder.createTimedRecurringUntilDtoWithQuotes(
                "Bad Event",
                "2025-11-11T10:00",
                "2025-11-11T11:00",
                "T",
                "2025-11-10"
    );
    createService.create(dto, new HashSet<>(), ZoneId.of("America/Los_Angeles"));
  }
}