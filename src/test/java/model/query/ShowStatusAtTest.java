package model.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.dto.CreateEventDto;
import calendar.dto.QueryEventDto;
import calendar.interfacetypes.Icreate;
import calendar.interfacetypes.Iquery;
import calendar.model.Event;
import calendar.strategy.CreateEventSingle;
import calendar.strategy.ShowStatusAt;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import model.util.TestQueryDtoBuilder;
import org.junit.Test;

/**
 * Tests the ShowStatusAt query class.
 * Verifies availability status checks at specific times.
 */
public class ShowStatusAtTest {

  @Test
  public void testShowStatusAtBusy() throws Exception {
    Set<Event> existingEvents = new HashSet<>();
    Icreate createService = new CreateEventSingle();
    Iquery queryService = new ShowStatusAt();
    ZoneId zoneId = ZoneId.of("America/Los_Angeles");
    CreateEventDto event = TestDtoBuilder.createTimedSingleDtoWithQuotes(
                "Meeting",
                "2025-11-22T10:00",
                "2025-11-22T11:00"
    );
    existingEvents.addAll(createService.create(event, existingEvents, zoneId));

    QueryEventDto dto = TestQueryDtoBuilder.createShowStatusAtDto("2025-11-22T10:30");

    Set<Event> result = queryService.find(dto, existingEvents, zoneId);

    assertNotNull("Result should not be null", result);
    assertEquals("Should find 1 active event (busy)", 1, result.size());
    assertTrue("User should be busy", !result.isEmpty());

    System.out.println("✓ TEST PASSED: Status is BUSY at specified time");
  }

  @Test
  public void testShowStatusAtAvailable() throws Exception {
    Set<Event> existingEvents = new HashSet<>();
    Icreate createService = new CreateEventSingle();
    Iquery queryService = new ShowStatusAt();
    ZoneId zoneId = ZoneId.of("America/Los_Angeles");
    CreateEventDto event = TestDtoBuilder.createTimedSingleDtoWithQuotes(
                "Meeting",
                "2025-11-22T10:00",
                "2025-11-22T11:00"
    );
    existingEvents.addAll(createService.create(event, existingEvents, zoneId));
    
    QueryEventDto dto = TestQueryDtoBuilder.createShowStatusAtDto("2025-11-22T12:00");

    Set<Event> result = queryService.find(dto, existingEvents, zoneId);

    assertNotNull("Result should not be null", result);
    assertEquals("Should find 0 active events (available)", 0, result.size());
    assertTrue("User should be available", result.isEmpty());

    System.out.println("✓ TEST PASSED: Status is AVAILABLE at specified time");
  }

  @Test
  public void testShowStatusAtEdgeCaseStartTime() throws Exception {
    Set<Event> existingEvents = new HashSet<>();
    Icreate createService = new CreateEventSingle();
    Iquery queryService = new ShowStatusAt();

    CreateEventDto event = TestDtoBuilder.createTimedSingleDtoWithQuotes(
              "Meeting",
              "2025-11-22T10:00",
              "2025-11-22T11:00"
    );
    existingEvents.addAll(createService.create(event, existingEvents,
            ZoneId.of("America/Los_Angeles")));
    QueryEventDto dto = TestQueryDtoBuilder.createShowStatusAtDto("2025-11-22T10:00");

    Set<Event> result = queryService.find(dto, existingEvents, ZoneId.of("America/Los_Angeles"));

    assertNotNull("Result should not be null", result);
    assertTrue("Should be busy at exact start time", !result.isEmpty());

    System.out.println("✓ TEST PASSED: Busy at exact start time");
  }

  @Test
  public void testShowStatusAtMultipleOverlappingEvents() throws Exception {
    Set<Event> existingEvents = new HashSet<>();
    Icreate createService = new CreateEventSingle();
    Iquery queryService = new ShowStatusAt();
    CreateEventDto event1 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
                "Meeting 1",
                "2025-11-22T10:00",
                "2025-11-22T11:00"
    );
    existingEvents.addAll(createService.create(event1, existingEvents,
            ZoneId.of("America/Los_Angeles")));

    CreateEventDto event2 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
                "Meeting 2",
                "2025-11-22T10:30",
                "2025-11-22T11:30"
    );
    existingEvents.addAll(createService.create(event2, new HashSet<>(existingEvents),
            ZoneId.of("America/Los_Angeles")));
    QueryEventDto dto = TestQueryDtoBuilder.createShowStatusAtDto("2025-11-22T10:45");

    Set<Event> result = queryService.find(dto, existingEvents,
            ZoneId.of("America/Los_Angeles"));

    assertNotNull("Result should not be null", result);
    assertEquals("Should find 2 overlapping events", 2, result.size());

    System.out.println("✓ TEST PASSED: Found " + result.size() + " overlapping events");
  }

  @Test
  public void testFindThrowsOnMissingDateTime() {
    Iquery queryService = new ShowStatusAt();
    QueryEventDto invalidDto = calendar.dto.QueryEventDto.forStatus(null);
    Exception exception = assertThrows(Exception.class, () -> {
      queryService.find(invalidDto, new HashSet<>(), ZoneId.of("UTC"));
    });
    assertEquals("Invalid query: missing date-time.", exception.getMessage());
  }

  /**
   * Verifies that the query throws a DateTimeParseException when the date string is malformed.
   * Targets the block: LocalDateTime.parse(dtStr, DATETIME_FORMATTER)
   */
  @Test
  public void testFindThrowsOnInvalidDateFormat() {
    Iquery queryService = new ShowStatusAt();
    QueryEventDto invalidDto = calendar.dto.QueryEventDto.forStatus("Not-A-Date-String");
    assertThrows(DateTimeParseException.class, () -> {
      queryService.find(invalidDto, new HashSet<>(), ZoneId.of("UTC"));
    });
  }

}