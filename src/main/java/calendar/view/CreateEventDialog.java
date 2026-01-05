package calendar.view;

import calendar.interfacetypes.IcreateEventDialogData;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for creating a new event.
 * Supports timed and all-day events, single and recurring.
 */
public class CreateEventDialog extends JDialog implements IcreateEventDialogData {

  private JTextField subjectField;
  private JCheckBox allDayCheckbox;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JCheckBox recurringCheckbox;
  private JCheckBox mondayBox;
  private JCheckBox tuesdayBox;
  private JCheckBox wednesdayBox;
  private JCheckBox thursdayBox;
  private JCheckBox fridayBox;
  private JCheckBox saturdayBox;
  private JCheckBox sundayBox;
  private JRadioButton timesRadio;
  private JRadioButton untilRadio;
  private JSpinner timesSpinner;
  private JSpinner untilDateSpinner;
  private JPanel timePanel;
  private JPanel recurringPanel;
  private final LocalDate selectedDate;
  private boolean confirmed = false;
  private JSpinner endDateSpinner;

  /**
   * Creates a dialog for creating an event on a specific date.
   *
   * @param parent       the parent frame to which this dialog is attached
   * @param selectedDate the initial date selected for the event
   */
  public CreateEventDialog(JFrame parent, LocalDate selectedDate) {
    super(parent, "Create Event", true);
    this.selectedDate = selectedDate;
    initComponents();
    layoutComponents();
    wireUpListeners();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initComponents() {
    subjectField = new JTextField(20);
    allDayCheckbox = new JCheckBox("All-day event");
    SpinnerDateModel startModel = new SpinnerDateModel();
    startTimeSpinner = new JSpinner(startModel);
    JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startEditor);
    startTimeSpinner.setValue(java.util.Date.from(
            LocalTime.of(9, 0).atDate(LocalDate.now())
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()
    ));

    SpinnerDateModel endDateDateModel = new SpinnerDateModel();
    endDateSpinner = new JSpinner(endDateDateModel);
    JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
    endDateSpinner.setEditor(endDateEditor);
    endDateSpinner.setValue(java.sql.Date.valueOf(selectedDate));
    SpinnerDateModel endModel = new SpinnerDateModel();
    endTimeSpinner = new JSpinner(endModel);
    JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endEditor);
    endTimeSpinner.setValue(java.util.Date.from(
            LocalTime.of(10, 0).atDate(LocalDate.now())
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()
    ));
    recurringCheckbox = new JCheckBox("Recurring event");
    mondayBox = new JCheckBox("Mon");
    tuesdayBox = new JCheckBox("Tue");
    wednesdayBox = new JCheckBox("Wed");
    thursdayBox = new JCheckBox("Thu");
    fridayBox = new JCheckBox("Fri");
    saturdayBox = new JCheckBox("Sat");
    sundayBox = new JCheckBox("Sun");

    timesRadio = new JRadioButton("Repeat", true);
    untilRadio = new JRadioButton("Repeat until");
    ButtonGroup group = new ButtonGroup();
    group.add(timesRadio);
    group.add(untilRadio);

    timesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    SpinnerDateModel untilModel = new SpinnerDateModel();
    untilDateSpinner = new JSpinner(untilModel);
    JSpinner.DateEditor untilEditor = new JSpinner.DateEditor(untilDateSpinner, "yyyy-MM-dd");
    untilDateSpinner.setEditor(untilEditor);
    untilDateSpinner.setValue(java.util.Date.from(
            selectedDate.plusMonths(1).atStartOfDay()
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()
    ));
  }

  private void layoutComponents() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    JPanel dateInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dateInfoPanel.add(new JLabel("Creating event on: "));
    JLabel dateLabel = new JLabel(selectedDate.toString());
    dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD));
    dateInfoPanel.add(dateLabel);
    mainPanel.add(dateInfoPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    subjectPanel.add(new JLabel("Event Name:"));
    subjectPanel.add(subjectField);
    mainPanel.add(subjectPanel);
    mainPanel.add(Box.createVerticalStrut(5));
    JPanel allDayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    allDayPanel.add(allDayCheckbox);
    mainPanel.add(allDayPanel);
    timePanel = new JPanel(new GridLayout(3, 2, 5, 5));
    timePanel.add(new JLabel("Start Time:"));
    timePanel.add(startTimeSpinner);
    timePanel.add(new JLabel("End Date:"));
    timePanel.add(endDateSpinner);
    timePanel.add(new JLabel("End Time:"));
    timePanel.add(endTimeSpinner);
    mainPanel.add(timePanel);
    mainPanel.add(Box.createVerticalStrut(10));
    JPanel recurPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    recurPanel.add(recurringCheckbox);
    mainPanel.add(recurPanel);
    recurringPanel = new JPanel();
    recurringPanel.setLayout(new BoxLayout(recurringPanel, BoxLayout.Y_AXIS));
    recurringPanel.setBorder(BorderFactory.createTitledBorder("Recurrence Options"));
    JPanel weekdayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    weekdayPanel.add(new JLabel("Repeat on:"));
    weekdayPanel.add(mondayBox);
    weekdayPanel.add(tuesdayBox);
    weekdayPanel.add(wednesdayBox);
    weekdayPanel.add(thursdayBox);
    weekdayPanel.add(fridayBox);
    weekdayPanel.add(saturdayBox);
    weekdayPanel.add(sundayBox);
    recurringPanel.add(weekdayPanel);
    JPanel timesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    timesPanel.add(timesRadio);
    timesPanel.add(timesSpinner);
    timesPanel.add(new JLabel("times"));
    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    untilPanel.add(untilRadio);
    untilPanel.add(untilDateSpinner);
    JPanel repetitionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    repetitionPanel.add(timesPanel);
    repetitionPanel.add(new JLabel());
    repetitionPanel.add(untilPanel);
    repetitionPanel.add(new JLabel());
    recurringPanel.add(repetitionPanel);
    recurringPanel.setVisible(false);
    mainPanel.add(recurringPanel);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");
    createButton.addActionListener(e -> {
      confirmed = true;
      dispose();
    });
    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });
    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private void wireUpListeners() {
    allDayCheckbox.addActionListener(e -> {
      timePanel.setVisible(!allDayCheckbox.isSelected());
      pack();
    });
    recurringCheckbox.addActionListener(e -> {
      recurringPanel.setVisible(recurringCheckbox.isSelected());
      pack();
    });
    timesRadio.addActionListener(e -> {
      timesSpinner.setEnabled(true);
      untilDateSpinner.setEnabled(false);
    });
    untilRadio.addActionListener(e -> {
      timesSpinner.setEnabled(false);
      untilDateSpinner.setEnabled(true);
    });
    timesSpinner.setEnabled(true);
    untilDateSpinner.setEnabled(false);
  }

  /**
   * Checks if the user confirmed the dialog action (clicked "Create").
   *
   * @return true if confirmed, false otherwise
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Retrieves the subject entered for the event.
   *
   * @return the event name/subject
   */
  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Checks if the event is marked as all-day.
   *
   * @return true if the all-day checkbox is selected
   */
  public boolean isAllDay() {
    return allDayCheckbox.isSelected();
  }

  /**
   * Retrieves the start time specified in the spinner.
   *
   * @return the selected start time
   */
  public LocalTime getStartTime() {
    java.util.Date date = (java.util.Date) startTimeSpinner.getValue();
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
  }

  /**
   * Retrieves the end time specified in the spinner.
   *
   * @return the selected end time
   */
  public LocalTime getEndTime() {
    java.util.Date date = (java.util.Date) endTimeSpinner.getValue();
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
  }

  /**
   * Checks if the event is marked as recurring.
   *
   * @return true if the recurring checkbox is selected
   */
  public boolean isRecurring() {
    return recurringCheckbox.isSelected();
  }

  /**
   * Retrieves the set of days selected for recurrence.
   *
   * @return a Set of DayOfWeek enums representing the checked days
   */
  public Set<DayOfWeek> getSelectedWeekdays() {
    Set<DayOfWeek> weekdays = new HashSet<>();
    if (mondayBox.isSelected()) {
      weekdays.add(DayOfWeek.MONDAY);
    }
    if (tuesdayBox.isSelected()) {
      weekdays.add(DayOfWeek.TUESDAY);
    }
    if (wednesdayBox.isSelected()) {
      weekdays.add(DayOfWeek.WEDNESDAY);
    }
    if (thursdayBox.isSelected()) {
      weekdays.add(DayOfWeek.THURSDAY);
    }
    if (fridayBox.isSelected()) {
      weekdays.add(DayOfWeek.FRIDAY);
    }
    if (saturdayBox.isSelected()) {
      weekdays.add(DayOfWeek.SATURDAY);
    }
    if (sundayBox.isSelected()) {
      weekdays.add(DayOfWeek.SUNDAY);
    }
    return weekdays;
  }

  /**
   * Checks if the recurrence is limited by a number of occurrences.
   *
   * @return true if "Repeat" (times) is selected, false if "Repeat until" is selected
   */
  public boolean isRepeatForTimes() {
    return timesRadio.isSelected();
  }

  /**
   * Retrieves the number of times the event should repeat.
   *
   * @return the recurrence count
   */
  public int getRepeatTimes() {
    return (Integer) timesSpinner.getValue();
  }

  /**
   * Retrieves the cutoff date for the recurrence.
   *
   * @return the date until which the event repeats
   */
  public LocalDate getRepeatUntilDate() {
    java.util.Date date = (java.util.Date) untilDateSpinner.getValue();
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
  }

  /**
   * Retrieves the initial date selected for the event.
   *
   * @return the selected date
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Retrieves the end date specified in the spinner.
   * This is used for events that span multiple days or strict end definitions.
   *
   * @return the end date
   */
  public LocalDate getEndDate() {
    java.util.Date date = (java.util.Date) endDateSpinner.getValue();
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
  }
}