package constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.constants.ExportConstants;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;

/**
 * Tests the ExportConstants class to verify date/time format patterns
 * and CSV header values are correct.
 */
public class ExportConstantsTest {

  @Test
  public void testGoogleDateFormatPattern() {
    LocalDate date = LocalDate.of(2025, 12, 5);
    String formatted = date.format(ExportConstants.GOOGLE_DATE_FORMAT);
    assertEquals("12/05/2025", formatted);
  }

  @Test
  public void testGoogleTimeFormatPattern() {
    LocalTime time = LocalTime.of(9, 30);
    String formatted = time.format(ExportConstants.GOOGLE_TIME_FORMAT);
    assertEquals("09:30 AM", formatted);
  }

  @Test
  public void testCsvHeaderValue() {
    assertEquals(
        "Subject,Start Date,Start Time,End Date,End Time\n",
        ExportConstants.CSV_HEADER
    );
  }

  @Test
  public void testDateFormat() {
    LocalDate date = LocalDate.of(2025, 12, 31);
    String formatted = ExportConstants.GOOGLE_DATE_FORMAT.format(date);
    assertEquals("12/31/2025", formatted);
  }

  @Test
  public void testTimeFormat() {
    LocalTime time = LocalTime.of(14, 30); // 2:30 PM
    String formatted = ExportConstants.GOOGLE_TIME_FORMAT.format(time);
    assertEquals("02:30 PM", formatted);
  }

  @Test
  public void testConstructorForCoverage() {
    ExportConstants constants = new ExportConstants();
    assertNotNull(constants);
  }

}
