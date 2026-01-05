package calendar.input;

import calendar.interfacetypes.IinputSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Input source for headless mode reading from a file.
 * Reads all commands from a file and queues them for processing.
 */
public class HeadlessInputSource implements IinputSource {

  private final Queue<String> commands;

  /**
   * Creates a headless input source from a file.
   *
   * @param filename the path to the file containing commands
   * @throws IOException if the file cannot be read
   */
  public HeadlessInputSource(String filename) throws IOException {
    this.commands = new LinkedList<>();
    loadCommandsFromFile(filename);
  }

  private void loadCommandsFromFile(String filename) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
          commands.add(trimmed);
        }
      }
    }
  }

  @Override
  public String getNextCommand() {
    if (commands.isEmpty()) {
      return null;
    }
    String command = commands.poll();
    System.out.println("> " + command);
    return command;
  }

  @Override
  public boolean hasMoreCommands() {
    return !commands.isEmpty();
  }

  @Override
  public void close() {
    commands.clear();
  }
}