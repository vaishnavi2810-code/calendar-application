package model.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.dto.CreateEventDto;
import calendar.interfacetypes.Icreate;
import calendar.model.Event;
import calendar.strategy.CreateAllDayRecurringUntil;
import calendar.strategy.CreateAllDaySingle;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite for the CreateAllDayRecurringUntil class.
 * Verifies the creation of recurring all-day events that continue until a specified end date,
 * including successful creation with quoted and unquoted subjects, and collision detection.
 */
public class CreateAllDayRecurringUntilTest {

  /**
   * Tests successful creation of recurring all-day events with a quoted multi-word subject.
   * Verifies that events are created on the specified weekdays (Saturday and Sunday) from
   * the start date until the end date, all events have correct all-day times (8 AM to 5 PM),
   * and the multi-word subject is properly preserved.
   */
  @Test
  public void testCreateAllDayRecurringUntilWithQuotes() throws Exception {
    Icreate createService = new CreateAllDayRecurringUntil();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createAllDayRecurringDtoUntilWithQuotes(
                "Weekend Cleanup",
                "2025-11-01",
                "SU",
                "2025-11-30"
    );

    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));

    assertNotNull("Result should not be null", result);

    assertEquals("Should create 10 events", 10, result.size());


    for (Event event : result) {
      assertEquals("All events should have subject 'Weekend Cleanup'",
                   "Weekend Cleanup",
                    event.getSubject());

      assertEquals("All-day should start at 8am", 8, event.getStartDateTime().getHour());
      assertEquals("All-day should end at 5pm", 17, event.getEndDateTime().getHour());

      DayOfWeek day = event.getStartDateTime().getDayOfWeek();
      assertTrue(
          "Event should be on a Saturday or Sunday, but was: " + day,
          day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
      );
    }

    System.out.println("✓ TEST PASSED: Created " + result.size() + " all-day events");
    System.out.println("  Subject: Weekend Cleanup");
    System.out.println("  Days: Sundays from Nov 1 to Nov 30");
  }

  @Test
  public void testCreateAllDayRecurringUntilWithoutQuotes() throws Exception {
    Icreate createService = new CreateAllDayRecurringUntil();
    Set<Event> existingEvents = new HashSet<>();
    CreateEventDto dto = TestDtoBuilder.createAllDayRecurringDtoUntilWithoutQuotes(
                "Cleanup",
                "2025-11-01",
                "SU",
                "2025-11-30"
    );

    Set<Event> result = createService.create(dto, existingEvents, ZoneId.of("America/Los_Angeles"));

    assertEquals("Should create 10 events", 10, result.size());

    Event firstEvent = result.iterator().next();
    assertEquals("Subject should be 'Cleanup'", "Cleanup", firstEvent.getSubject());

    System.out.println("✓ TEST PASSED: Created " + result.size() + " all-day events");
    System.out.println("  Subject: Cleanup");
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateAllDayRecurringUntilErrors() throws Exception {
    Set<Event> existingEvents = new HashSet<>();
    thrown.expect(Exception.class);
    thrown.expectMessage("Event already exists");
    Icreate createService2 = new CreateAllDayRecurringUntil();
    Icreate createService1 = new CreateAllDaySingle();
    CreateEventDto dto = TestDtoBuilder.createAllDaySingleDtoWithQuotes(
                "Cleanup",
                "2025-11-01"
    );
    Set<Event> result = createService1.create(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    existingEvents.addAll(result);
    CreateEventDto dto1 = TestDtoBuilder.createAllDayRecurringDtoUntilWithoutQuotes(
                "Cleanup",
                "2025-11-01",
                "SU",
                "2025-11-30"
    );
    Set<Event> result1 = createService2.create(dto1, existingEvents,
            ZoneId.of("America/Los_Angeles"));
    assertEquals("Should create 10 events", 10, result.size());
    Event firstEvent = result.iterator().next();
    assertEquals("Subject should be 'Cleanup'", "Cleanup", firstEvent.getSubject());
    System.out.println("✓ TEST PASSED: Created " + result.size() + " all-day events");
    System.out.println("  Subject: Cleanup");
  }
}