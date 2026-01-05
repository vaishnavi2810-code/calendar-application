package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for editing an existing calendar.
 * "Dumb" View: It simply exposes fields for Name and Timezone.
 * It does not enforce which one must be edited.
 */
public class EditCalendarDialog extends JDialog {

  private JTextField nameField;
  private JComboBox<String> timezoneBox;
  private boolean confirmed = false;

  /**
   * Creates a dialog for editing a calendar.
   *
   * @param parent The parent frame
   * @param currentCalendarName The name of the calendar being edited
   * @param currentTimezone The current timezone of the calendar
   */
  public EditCalendarDialog(JFrame parent, String currentCalendarName, String currentTimezone) {
    super(parent, "Edit Calendar", true);
    initComponents(currentCalendarName, currentTimezone);
    layoutComponents(currentCalendarName);
    pack();
    setLocationRelativeTo(parent);
  }

  private void initComponents(String currentName, String currentTimezone) {
    nameField = new JTextField(currentName, 15);
    String[] timezones = ZoneId.getAvailableZoneIds().toArray(new String[0]);
    timezoneBox = new JComboBox<>(timezones);
    timezoneBox.setSelectedItem(Objects
            .requireNonNullElseGet(currentTimezone,
                    () -> ZoneId.systemDefault().getId()));
  }

  private void layoutComponents(String currentCalendarName) {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    infoPanel.add(new JLabel("Editing Calendar: "));
    JLabel calendarLabel = new JLabel(currentCalendarName);
    calendarLabel.setFont(calendarLabel.getFont().deriveFont(Font.BOLD));
    infoPanel.add(calendarLabel);
    mainPanel.add(infoPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    JPanel fieldPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    fieldPanel.add(new JLabel("Name:"));
    fieldPanel.add(nameField);
    fieldPanel.add(new JLabel("Timezone:"));
    fieldPanel.add(timezoneBox);
    mainPanel.add(fieldPanel);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("Save"); // Changed "OK" to "Save" to imply action
    JButton cancelButton = new JButton("Cancel");

    okButton.addActionListener(e -> {
      confirmed = true;
      dispose();
    });

    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Returns the name currently in the text field.
   * The View does NOT check if this is different from the original.
   * That is the Controller's job.
   */
  public String getNewName() {
    return nameField.getText().trim();
  }

  /**
   * Returns the timezone currently selected.
   */
  public String getNewTimezone() {
    return (String) timezoneBox.getSelectedItem();
  }

  /**
   * Returns true if user clicked Save, false if cancelled.
   */
  public boolean isConfirmed() {
    return confirmed;
  }
}