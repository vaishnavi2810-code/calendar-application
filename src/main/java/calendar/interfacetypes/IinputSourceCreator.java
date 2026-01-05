package calendar.interfacetypes;

/**
 * Interface for creating input sources.
 * Follows the Strategy Pattern for creating different types of input sources.
 */
public interface IinputSourceCreator {

  /**
   * Creates an input source.
   *
   * @param filename the filename (may be null for some modes)
   * @return the created input source
   * @throws Exception if creation fails
   */
  IinputSource create(String filename) throws Exception;
}