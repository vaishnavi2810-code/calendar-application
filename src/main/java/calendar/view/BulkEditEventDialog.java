package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
import javax.swing.border.EmptyBorder;

/**
 * Dialog to bulk edit multiple events.
 * Pre-fills fields with the first event in sorted order.
 */
public class BulkEditEventDialog extends JDialog {

  private JTextField subjectField;
  private JTextField startDateField;
  private JSpinner startTimeSpinner;
  private JTextField endDateField;
  private JSpinner endTimeSpinner;
  private JTextField locationField;
  private JTextField descriptionField;
  private boolean confirmed = false;

  /**
   * constructor to initialise dialog and its parent.
   *
   * @param parent ok.
   * @param events ok.
   * @param calendarZone ok.
   */
  public BulkEditEventDialog(JFrame parent, List<Event> events, ZoneId calendarZone) {
    super(parent, "Bulk Edit Events", true);

    if (events == null || events.isEmpty()) {
      throw new IllegalArgumentException("Events list cannot be empty");
    }

    events.sort(Comparator.comparing(Event::getStartDateTime));

    Event firstEvent = events.get(0);
    initComponents(firstEvent, calendarZone);
    layoutComponents();

    pack();
    setLocationRelativeTo(parent);
  }

  private void initComponents(Event firstEvent, ZoneId calendarZone) {
    subjectField = new JTextField(firstEvent.getSubject(), 20);

    ZonedDateTime startZdt = firstEvent.getStartDateTime();
    ZonedDateTime endZdt = firstEvent.getEndDateTime();

    startDateField = new JTextField(startZdt.toLocalDate().toString(), 15);
    endDateField = new JTextField(endZdt.toLocalDate().toString(), 15);

    startTimeSpinner = createTimeSpinner(startZdt, calendarZone);
    endTimeSpinner = createTimeSpinner(endZdt, calendarZone);

    locationField = new JTextField(firstEvent.getLocation() != null
            ? firstEvent.getLocation() : "", 20);
    descriptionField = new JTextField(firstEvent.getDescription() != null
            ? firstEvent.getDescription() : "", 20);
  }

  private JSpinner createTimeSpinner(ZonedDateTime zdt, ZoneId calendarZone) {
    LocalTime time = zdt.withZoneSameInstant(calendarZone).toLocalTime();
    LocalDate today = LocalDate.now();
    Date dateValue = Date.from(today.atTime(time).atZone(ZoneId.systemDefault()).toInstant());
    SpinnerDateModel model = new SpinnerDateModel();
    model.setValue(dateValue);
    JSpinner spinner = new JSpinner(model);
    spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
    return spinner;
  }

  private void layoutComponents() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

    JLabel title = new JLabel("Edit All Matching Events");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titlePanel.add(title);
    mainPanel.add(titlePanel);
    mainPanel.add(Box.createVerticalStrut(10));
    JPanel fieldsPanel = new JPanel(new GridLayout(7, 2, 5, 5));
    fieldsPanel.setBorder(BorderFactory.createTitledBorder("Event Details"));

    fieldsPanel.add(new JLabel("Subject:"));
    fieldsPanel.add(subjectField);

    fieldsPanel.add(new JLabel("Start Date (yyyy-MM-dd):"));
    fieldsPanel.add(startDateField);

    fieldsPanel.add(new JLabel("Start Time:"));
    fieldsPanel.add(startTimeSpinner);

    fieldsPanel.add(new JLabel("End Date (yyyy-MM-dd):"));
    fieldsPanel.add(endDateField);

    fieldsPanel.add(new JLabel("End Time:"));
    fieldsPanel.add(endTimeSpinner);

    fieldsPanel.add(new JLabel("Location:"));
    fieldsPanel.add(locationField);

    fieldsPanel.add(new JLabel("Description:"));
    fieldsPanel.add(descriptionField);

    mainPanel.add(fieldsPanel);
    mainPanel.add(Box.createVerticalStrut(15));

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

    mainPanel.add(buttonPanel);

    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSubject() {
    return subjectField.getText().trim();
  }

  public LocalDate getStartDate() {
    return LocalDate.parse(startDateField.getText().trim());
  }

  /**
   * this returns start time in localtime format.
   *
   * @return Local time .
   */
  public LocalTime getStartTime() {
    Date spinnerDate = (Date) startTimeSpinner.getValue();
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(spinnerDate);
    int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
    int minute = cal.get(java.util.Calendar.MINUTE);
    return LocalTime.of(hour, minute);
  }

  /**
   * this returns end date in localdate format.
   *
   * @return Local date .
   */
  public LocalDate getEndDate() {
    return LocalDate.parse(endDateField.getText().trim());
  }

  /**
   * this returns end time in localtime format.
   *
   * @return Local time .
   */
  public LocalTime getEndTime() {
    Date spinnerDate = (Date) endTimeSpinner.getValue();
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(spinnerDate);
    int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
    int minute = cal.get(java.util.Calendar.MINUTE);
    return LocalTime.of(hour, minute);
  }

  public String getEventLocation() {
    return locationField.getText().trim();
  }

  public String getDescription() {
    return descriptionField.getText().trim();
  }
}
