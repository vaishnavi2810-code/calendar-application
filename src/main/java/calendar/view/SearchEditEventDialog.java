package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

/**
 * Dialog for searching events by subject and start datetime.
 * Used for bulk editing multiple events that match the search criteria.
 */
public class SearchEditEventDialog extends JDialog {

  private JTextField subjectField;
  private JTextField startDateField;
  private JSpinner startTimeSpinner;
  private boolean confirmed = false;

  /**
   * Creates a search dialog.
   *
   * @param parent parent frame
   */
  public SearchEditEventDialog(JFrame parent) {
    super(parent, "Search Events", true);

    initComponents();
    layoutComponents();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initComponents() {
    subjectField = new JTextField(20);
    startDateField = new JTextField(15);
    LocalTime defaultTime = LocalTime.of(0, 0);
    LocalDate today = LocalDate.now();
    java.util.Date defaultDate =
        java.util.Date.from(LocalDateTime.of(today, defaultTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant());
    SpinnerDateModel timeModel = new SpinnerDateModel();
    timeModel.setValue(defaultDate);
    startTimeSpinner = new JSpinner(timeModel);
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(timeEditor);
  }

  private void layoutComponents() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    JLabel titleLabel = new JLabel("Search for events to edit");
    titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titlePanel.add(titleLabel);
    mainPanel.add(titlePanel);
    mainPanel.add(Box.createVerticalStrut(10));
    JPanel criteriaPanel = new JPanel(new GridLayout(3, 2, 5, 5));
    criteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
    criteriaPanel.add(new JLabel("Event Subject:"));
    criteriaPanel.add(subjectField);
    criteriaPanel.add(new JLabel("Start Date (yyyy-MM-dd):"));
    criteriaPanel.add(startDateField);
    criteriaPanel.add(new JLabel("Start Time:"));
    criteriaPanel.add(startTimeSpinner);
    mainPanel.add(criteriaPanel);
    mainPanel.add(Box.createVerticalStrut(15));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton searchButton = new JButton("Search");
    JButton cancelButton = new JButton("Cancel");
    searchButton.addActionListener(e -> {
      if (subjectField.getText().trim().isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Please enter an event subject to search for.",
            "Search Error",
            javax.swing.JOptionPane.WARNING_MESSAGE
        );
        return;
      }

      if (startDateField.getText().trim().isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Please enter a start date.",
            "Search Error",
            javax.swing.JOptionPane.WARNING_MESSAGE
        );
        return;
      }

      confirmed = true;
      dispose();
    });

    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });

    buttonPanel.add(searchButton);
    buttonPanel.add(cancelButton);

    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * returns a ZoneDateTime version of the startdate..
   */
  public ZonedDateTime getStartDateTime(ZoneId calendarZone) {
    LocalDate date = LocalDate.parse(startDateField.getText().trim());
    Date spinnerDate = (Date) startTimeSpinner.getValue();
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(spinnerDate);
    int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
    int minute = cal.get(java.util.Calendar.MINUTE);
    LocalTime time = LocalTime.of(hour, minute);
    return ZonedDateTime.of(date, time, calendarZone);
  }
}