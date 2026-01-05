package constants;

import static calendar.constants.CreateCalendarConstants.parseWeekdays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.constants.CreateCalendarConstants;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.util.Set;
import org.junit.Test;

/**
 * Tests the CreateCalendarConstants utility class,
 * specifically the parseWeekdays method and constructor access.
 */
public class CreateCalendarConstantsTests {

  @Test
  public void testParseWeekdaysEmptyString() {
    Set<DayOfWeek> days = parseWeekdays("");
    assertTrue(days.isEmpty());
  }

  @Test
  public void testParseWeekdaysNull() {
    Set<DayOfWeek> days = parseWeekdays(null);
    assertTrue(days.isEmpty());
  }

  @Test
  public void testParseWeekdaysSingleValidDay() {
    Set<DayOfWeek> days = parseWeekdays("M");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testParseWeekdaysAllValidDays() {
    // Assuming DAY_CHAR_MAP: M,T,W,R,F,S,U
    Set<DayOfWeek> days = parseWeekdays("MTWRFSU");
    assertEquals(7, days.size());
    assertTrue(days.containsAll(Set.of(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )));
  }

  @Test
  public void testParseWeekdaysLowercaseInput() {
    Set<DayOfWeek> days = parseWeekdays("mtw");
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testParseWeekdaysWithInvalidCharacters() {
    Set<DayOfWeek> days = parseWeekdays("MXZ");
    // M is valid, X and Z should be ignored
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testParseWeekdaysWithDuplicates() {
    Set<DayOfWeek> days = parseWeekdays("MMTT");
    // Set removes duplicates
    assertEquals(2, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
  }

  @Test
  public void testParseWeekdaysMixedCaseAndNoise() {
    Set<DayOfWeek> days = parseWeekdays("mTx@w#");
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testConstructorIsPrivate() throws Exception {
    Constructor<CreateCalendarConstants> constructor =
        CreateCalendarConstants.class.getDeclaredConstructor();
    assertTrue("Constructor should be private",
        java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected AssertionError to be thrown");
    } catch (InvocationTargetException e) {
      assertTrue("Expected AssertionError",
          e.getTargetException() instanceof AssertionError);
      assertEquals("Cannot instantiate constants class",
          e.getTargetException().getMessage());
    }
  }
}
