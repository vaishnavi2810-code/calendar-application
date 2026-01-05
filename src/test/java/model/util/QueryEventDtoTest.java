package calendar.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests for the QueryEventDto factory methods.
 * Verifies that the static factory methods correctly populate the DTO fields
 * and assign the correct QueryType.
 */
public class QueryEventDtoTest {
  @Test
  public void testForDateFactory() {
    String date = "2023-12-25";
    QueryEventDto dto = QueryEventDto.forDate(date);
    assertEquals("Type should be PRINT_ON_DATE",
                QueryEventDto.QueryType.PRINT_ON_DATE, dto.getType());
    assertEquals(date, dto.getOnDate());
    assertNull(dto.getRangeStart());
    assertNull(dto.getRangeEnd());
    assertNull(dto.getAtInstant());
  }

  @Test
  public void testForRangeFactory() {
    String start = "2023-01-01T00:00";
    String end = "2023-01-31T23:59";

    QueryEventDto dto = QueryEventDto.forRange(start, end);

    assertEquals("Type should be PRINT_IN_RANGE",
                QueryEventDto.QueryType.PRINT_IN_RANGE, dto.getType());
    assertEquals(start, dto.getRangeStart());
    assertEquals(end, dto.getRangeEnd());

    assertNull(dto.getOnDate());
    assertNull(dto.getAtInstant());
  }

  @Test
  public void testForStatusFactory() {
    String instant = "2023-06-15T14:30";
    QueryEventDto dto = QueryEventDto.forStatus(instant);
    assertEquals("Type should be SHOW_STATUS_AT",
                QueryEventDto.QueryType.SHOW_STATUS_AT, dto.getType());
    assertEquals(instant, dto.getAtInstant());
    assertNull(dto.getOnDate());
    assertNull(dto.getRangeStart());
    assertNull(dto.getRangeEnd());
  }
}