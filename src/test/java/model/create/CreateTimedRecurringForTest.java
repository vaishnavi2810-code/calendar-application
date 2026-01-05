package model.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.strategy.CreateTimedRecurringFor;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite for the CreateTimedRecurringFor class.
 * Verifies the creation of recurring timed events with a fixed number of occurrences,
 * including successful creation with correct times and weekdays, collision detection,
 * and validation of time constraints (end after start, same-day events).
 */
public class CreateTimedRecurringForTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateTimedRecurringForSuccess() throws Exception {
    Icreate createService = new CreateTimedRecurringFor();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createTimedRecurringForDtoWithQuotes(
                "MWF Sync",
                "2025-11-10T10:00",
                "2025-11-10T11:00",
                "MWF",
                "6"
    );

    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
    assertEquals("Should create 6 events", 6, result.size());

    String seriesId = result.iterator().next().getSeriesId();
    assertNotNull("seriesId should not be null", seriesId);

    for (Event event : result) {
      assertEquals("Subject should match", "MWF Sync", event.getSubject());
      assertEquals("Start hour should be 10", 10, event.getStartDateTime().getHour());
      assertEquals("End hour should be 11", 11, event.getEndDateTime().getHour());
      assertEquals("All events should have the same seriesId", seriesId, event.getSeriesId());
      DayOfWeek day = event.getStartDateTime().getDayOfWeek();
      assertTrue("Day should be Monday, Wednesday, or Friday",
                    day == DayOfWeek.MONDAY
                            ||
                            day == DayOfWeek.WEDNESDAY
                            ||
                            day == DayOfWeek.FRIDAY);
    }
    System.out.println("✓ TEST PASSED: Created "
            +
            result.size()
            +
            " recurring timed events.");
  }

  @Test
  public void testCreateTimedRecurringForCollision() throws Exception {
    Icreate createService = new CreateTimedRecurringFor();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createTimedRecurringForDtoWithQuotes(
               "MWF Sync",
                "2025-11-10T10:00",
                "2025-11-10T11:00",
                "MWF",
                "6"
    );
    Set<Event> firstSet = createService.create(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(firstSet);
    thrown.expect(Exception.class);
    thrown.expectMessage("Event already exists");
    createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void testCreateTimedRecurringForEndBeforeStart() throws Exception {
    Icreate createService = new CreateTimedRecurringFor();
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: Event end time cannot be before its start time.");
    CreateEventDto dto = TestDtoBuilder.createTimedRecurringForDtoWithQuotes(
                "Bad Event",
                "2025-11-10T11:00",
                "2025-11-10T10:00",
                "M",
                "1"
    );
    createService.create(dto, new HashSet<>(), ZoneId.of("America/Los_Angeles"));
  }

  /**
  * Test validation: Recurring events must start and end on the same day.
  */
  @Test
  public void testCreateTimedRecurringForSpansMultipleDays() throws Exception {
    Icreate createService = new CreateTimedRecurringFor();
    thrown.expect(Exception.class);
    thrown.expectMessage("Date should be the same");

    CreateEventDto dto = TestDtoBuilder.createTimedRecurringForDtoWithQuotes(
                "Bad Event",
                "2025-11-10T10:00",
                "2025-11-11T10:00",
                "M",
                "1"
    );
    createService.create(dto, new HashSet<>(),  ZoneId.of("America/Los_Angeles"));
  }
}