package calendar.model;

import calendar.interfacetypes.Icalendarcollection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * repository class that handles the hashmap of objects.
 */
public class CalendarCollection implements Icalendarcollection {
  private final Map<String, Calendar> calendarMap = new HashMap<>();

  @Override
  public Calendar findByName(String name) {
    return calendarMap.get(name);
  }

  @Override
  public void save(Calendar calendar) {
    calendarMap.put(calendar.getName(), calendar);
  }

  @Override
  public boolean existsByName(String name) {
    return calendarMap.containsKey(name);
  }

  @Override
  public void deleteByName(String name) {
    calendarMap.remove(name);
  }

  @Override
  public Set<String> getAllCalendarNames() {
    return calendarMap.keySet();
  }
}
