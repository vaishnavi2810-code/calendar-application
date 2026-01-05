package mocks;

import calendar.interfacetypes.Icommand;
import calendar.interfacetypes.Iparser;
import calendar.model.CalendarModel;
import calendar.service.CommandParserService;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock CommandParserService for testing.
 * Allows injection of test parsers to control parser behavior.
 */
public class MockCommandParserService extends CommandParserService {

  private final List<Iparser> testParsers;
  private boolean useTestParsers = true;

  /**
  * ok.
  */
  public MockCommandParserService() {
    super();
    this.testParsers = new ArrayList<>();
  }

  /**
  * Adds a parser to the test parser list.
  */
  public void addParser(Iparser parser) {
    testParsers.add(parser);
  }

  /**
  * Clears all test parsers.
  */
  public void clearParsers() {
    testParsers.clear();
  }

  /**
  * Sets whether to use test parsers or default parsers.
  */
  public void setUseTestParsers(boolean useTestParsers) {
    this.useTestParsers = useTestParsers;
  }

  @Override
  public Icommand parse(String command, CalendarModel service) throws Exception {
    if (useTestParsers) {
      for (Iparser parser : testParsers) {
        if (parser.canHandle(command)) {
          return parser.parse(command, service);
        }
      }
      throw new Exception("Error: Unknown command.");
    } else {
      return super.parse(command, service);
    }
  }
}