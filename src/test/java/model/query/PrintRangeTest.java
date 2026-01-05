package model.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import calendar.dto.CreateEventDto;
import calendar.dto.QueryEventDto;
import calendar.interfacetypes.Icreate;
import calendar.interfacetypes.Iquery;
import calendar.model.Event;
import calendar.strategy.CreateEventSingle;
import calendar.strategy.PrintInRange;
import calendar.strategy.PrintOnDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import model.util.TestDtoBuilder;
import model.util.TestQueryDtoBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the PrintInRange query class.
 * Verifies finding events within date-time ranges.
 */
public class PrintRangeTest {

  private Set<Event> existingEvents;
  private Iquery queryService;
  private Iquery queryService2;
  private ZoneId zoneId;

  /**
   * Sets up test fixtures before each test.
   * Creates sample events on different dates and times.
   *
   * @throws Exception if event creation fails
   */
  @Before
  public void setUp() throws Exception {
    existingEvents = new HashSet<>();
    zoneId = ZoneId.of("America/New_York");
    Icreate createService = new CreateEventSingle();
    queryService = new PrintInRange();
    queryService2 = new PrintOnDate();
    CreateEventDto event1 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Meeting",
        "2025-11-15T10:00",
        "2025-11-15T11:00"
    );
    existingEvents.addAll(createService.create(event1, existingEvents, zoneId));
    CreateEventDto event2 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Lunch",
        "2025-11-15T12:00",
        "2025-11-15T13:00"
    );
    existingEvents.addAll(createService.create(event2, new HashSet<>(existingEvents), zoneId));
    CreateEventDto event3 = TestDtoBuilder.createTimedSingleDtoWithQuotes(
        "Review",
        "2025-11-16T14:00",
        "2025-11-16T15:00"
    );
    existingEvents.addAll(createService.create(event3, new HashSet<>(existingEvents), zoneId));
  }

  @Test
  public void testPrintInRangeMultipleEvents() throws Exception {
    QueryEventDto dto = TestQueryDtoBuilder.createPrintInRangeDto(
                "2025-11-15T09:00",
                "2025-11-15T14:00"
    );

    Set<Event> result = queryService.find(dto, existingEvents, zoneId);

    assertNotNull("Result should not be null", result);
    assertEquals("Should find 2 events", 2, result.size());

    System.out.println("✓ TEST PASSED: Found " + result.size() + " events in range");
  }


  @Test
  public void testPrintInRangeNoEvents() throws Exception {
    QueryEventDto dto = TestQueryDtoBuilder.createPrintInRangeDto(
                "2025-11-17T09:00",
                "2025-11-17T14:00"
    );

    Set<Event> result = queryService.find(dto, existingEvents, zoneId);

    assertNotNull("Result should not be null", result);
    assertEquals("Should find 0 events", 0, result.size());

    System.out.println("✓ TEST PASSED: Found no events in empty range");
  }

  @Test
  public void testPrintInRangeMultipleDays() throws Exception {
    QueryEventDto dto = TestQueryDtoBuilder.createPrintInRangeDto(
                "2025-11-15T00:00",
                "2025-11-16T23:59"
    );

    Set<Event> result = queryService.find(dto, existingEvents, zoneId);

    assertNotNull("Result should not be null", result);
    assertEquals("Should find all 3 events", 3, result.size());

    System.out.println("✓ TEST PASSED: Found " + result.size() + " events across multiple days");
  }

  @Test(expected = Exception.class)
  public void testPrintInRangeNullStart() throws Exception {
    QueryEventDto dto = TestQueryDtoBuilder.createPrintInRangeDto(
            null,
            "2025-11-15T14:00"
    );
    queryService.find(dto, existingEvents, zoneId);
  }

  @Test(expected = Exception.class)
  public void testPrintInRangeNullEnd() throws Exception {
    QueryEventDto dto = TestQueryDtoBuilder.createPrintInRangeDto(
            "2025-11-15T09:00",
            null
    );
    queryService.find(dto, existingEvents, zoneId);
  }

  @Test(expected = Exception.class)
  public void testPrintInRangeStartAfterEnd() throws Exception {
    QueryEventDto dto = TestQueryDtoBuilder.createPrintInRangeDto(
            "2025-11-15T14:00",  // start AFTER end
            "2025-11-15T09:00"
    );
    queryService.find(dto, existingEvents, zoneId);
  }

  @Test(expected = Exception.class)
  public void testPrintInRangeNullEnd2() throws Exception {
    QueryEventDto dto = QueryEventDto.forRange(
            "2025-11-15T09:00",
            null  // null end time
    );

    queryService.find(dto, existingEvents, zoneId);
  }

  @Test(expected = Exception.class)
  public void testPrintInRangeNullStart3() throws Exception {
    QueryEventDto dto = QueryEventDto.forRange(
            null,  // null start time
            "2025-11-15T14:00"
    );

    queryService.find(dto, existingEvents, zoneId);
  }

  @Test
  public void testPrintOnDateNullDateMessage() {
    QueryEventDto dto = QueryEventDto.forDate(null);

    Exception exception = assertThrows(Exception.class, () ->
            queryService2.find(dto, existingEvents, zoneId)
    );
    assertEquals("Invalid query: missing date.", exception.getMessage());
  }
}