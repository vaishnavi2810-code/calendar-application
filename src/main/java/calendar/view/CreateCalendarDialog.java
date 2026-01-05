package calendar.view;



import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for creating a new calendar.
 * Collects calendar name and timezone from user.
 */
public class CreateCalendarDialog extends JDialog {

  private JTextField nameField;
  private JComboBox<String> timezoneBox;
  private boolean confirmed = false;

  /**
   * ok.
   *
   * @param parent ok.
   */
  public CreateCalendarDialog(JFrame parent) {
    super(parent, "Create New Calendar", true);
    initComponents();
    layoutComponents();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initComponents() {
    nameField = new JTextField(15);
    String[] timezones = ZoneId.getAvailableZoneIds().toArray(new String[0]);
    timezoneBox = new JComboBox<>(timezones);
    timezoneBox.setSelectedItem(ZoneId.systemDefault().getId());
  }

  private void layoutComponents() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(new JLabel("Calendar Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    panel.add(timezoneBox);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("OK");
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
    add(panel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
  * Returns the calendar name entered by user.
  */
  public String getCalendarName() {
    return nameField.getText().trim();
  }

  /**
  * Returns the timezone selected by user.
  */
  public String getTimezone() {
    return (String) timezoneBox.getSelectedItem();
  }

  /**
  * Returns true if user clicked OK, false if cancelled.
  */
  public boolean isConfirmed() {
    return confirmed;
  }
}