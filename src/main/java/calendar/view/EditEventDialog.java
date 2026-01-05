package calendar.view;

import calendar.interfacetypes.IeditEventDialogData;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;


/**
 * Dialog for editing an existing event.
 * Supports editing single event, entire series, or forward events.
 */
public class EditEventDialog extends JDialog implements IeditEventDialogData {

  private JTextField startDateField;
  private JTextField endDateField;
  private final Event eventToEdit;
  private JTextField subjectField;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JRadioButton editSingleRadio;
  private JRadioButton editSeriesRadio;
  private JRadioButton editForwardRadio;
  private JPanel editScopePanel;
  private boolean confirmed = false;
  private String editScope = "single";
  private boolean isPopulating = false;
  /**
   * Creates a dialog for editing an event.
   *
   * @param parent parent frame
   * @param event the event to edit
   */

  public EditEventDialog(JFrame parent, Event event) {
    super(parent, "Edit Event", true);
    this.eventToEdit = event;

    initComponents();
    layoutComponents();
    populateFields();
    wireUpListeners();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initComponents() {
    subjectField = new JTextField(20);
    startDateField = new JTextField(10);
    endDateField = new JTextField(10);

    SpinnerDateModel startModel = new SpinnerDateModel();
    startTimeSpinner = new JSpinner(startModel);
    JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startEditor);

    SpinnerDateModel endModel = new SpinnerDateModel();
    endTimeSpinner = new JSpinner(endModel);
    JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endEditor);
    locationField = new JTextField(20);
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    editSingleRadio = new JRadioButton("Edit this event only", true);
    editSeriesRadio = new JRadioButton("Edit all events in series");
    editForwardRadio = new JRadioButton("Edit this and following events");
    ButtonGroup scopeGroup = new ButtonGroup();
    scopeGroup.add(editSingleRadio);
    scopeGroup.add(editSeriesRadio);
    scopeGroup.add(editForwardRadio);
  }

  private void layoutComponents() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    JPanel eventInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    eventInfoPanel.add(new JLabel("Editing event on: "));
    LocalDate eventDate = eventToEdit.getStartDateTime().toLocalDate();
    JLabel dateLabel = new JLabel(eventDate.toString());
    dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD));
    eventInfoPanel.add(dateLabel);
    mainPanel.add(eventInfoPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    subjectPanel.add(new JLabel("Event Name:"));
    subjectPanel.add(subjectField);
    mainPanel.add(subjectPanel);
    mainPanel.add(Box.createVerticalStrut(5));
    JPanel dateTimePanel = new JPanel(new GridLayout(4, 2, 5, 5));
    dateTimePanel.setBorder(BorderFactory.createTitledBorder("Date and Time"));

    dateTimePanel.add(new JLabel("Start Date (yyyy-MM-dd):"));
    dateTimePanel.add(startDateField);

    dateTimePanel.add(new JLabel("Start Time:"));
    dateTimePanel.add(startTimeSpinner);

    dateTimePanel.add(new JLabel("End Date (yyyy-MM-dd):"));
    dateTimePanel.add(endDateField);

    dateTimePanel.add(new JLabel("End Time:"));
    dateTimePanel.add(endTimeSpinner);

    mainPanel.add(dateTimePanel);
    mainPanel.add(Box.createVerticalStrut(5));
    JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    locationPanel.add(new JLabel("Location:"));
    locationPanel.add(locationField);
    mainPanel.add(locationPanel);
    mainPanel.add(Box.createVerticalStrut(5));
    JPanel descriptionPanel = new JPanel(new BorderLayout());
    descriptionPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
    descriptionPanel.add(new javax.swing.JScrollPane(descriptionArea), BorderLayout.CENTER);
    descriptionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    mainPanel.add(descriptionPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    boolean isRecurring = eventToEdit.getSeriesId() != null
        && !eventToEdit.getSeriesId().isEmpty();

    if (isRecurring) {
      editScopePanel = new JPanel();
      editScopePanel.setLayout(new BoxLayout(editScopePanel, BoxLayout.Y_AXIS));
      editScopePanel.setBorder(BorderFactory.createTitledBorder("Edit Scope"));
      JPanel singlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      singlePanel.add(editSingleRadio);
      editScopePanel.add(singlePanel);
      JPanel seriesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      seriesPanel.add(editSeriesRadio);
      editScopePanel.add(seriesPanel);
      JPanel forwardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      forwardPanel.add(editForwardRadio);
      editScopePanel.add(forwardPanel);
      mainPanel.add(editScopePanel);
      mainPanel.add(Box.createVerticalStrut(10));
    }
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");
    saveButton.addActionListener(e -> {
      confirmed = true;
      dispose();
    });
    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });
    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private void populateFields() {
    isPopulating = true;
    subjectField.setText(eventToEdit.getSubject());
    LocalDate startDate = eventToEdit.getStartDateTime().toLocalDate();
    startDateField.setText(startDate.toString());

    LocalDate endDate = eventToEdit.getEndDateTime().toLocalDate();
    endDateField.setText(endDate.toString());
    LocalTime startTime = eventToEdit.getStartDateTime().toLocalTime();
    java.util.Date startTimeValue = java.util.Date.from(
        startTime.atDate(LocalDate.now())
            .atZone(java.time.ZoneId.systemDefault()).toInstant()
    );
    startTimeSpinner.setValue(startTimeValue);
    LocalTime endTime = eventToEdit.getEndDateTime().toLocalTime();
    java.util.Date endTimeValue = java.util.Date.from(
        endTime.atDate(LocalDate.now())
            .atZone(java.time.ZoneId.systemDefault()).toInstant()
    );
    endTimeSpinner.setValue(endTimeValue);
    if (eventToEdit.getLocation() != null) {
      locationField.setText(eventToEdit.getLocation());
    }
    if (eventToEdit.getDescription() != null) {
      descriptionArea.setText(eventToEdit.getDescription());
    }
    isPopulating = false;
  }

  private void wireUpListeners() {
    editSingleRadio.addActionListener(e -> editScope = "single");
    editSeriesRadio.addActionListener(e -> editScope = "series");
    editForwardRadio.addActionListener(e -> editScope = "forward");
    editSingleRadio.addActionListener(e -> editScope = "single");
    editSeriesRadio.addActionListener(e -> editScope = "series");
    editForwardRadio.addActionListener(e -> editScope = "forward");
    startDateField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      @Override
      public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateEndDateBasedOnStartDate();
      }

      @Override
      public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateEndDateBasedOnStartDate();
      }

      @Override
      public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateEndDateBasedOnStartDate();
      }
    });
  }


  public boolean isConfirmed() {
    return confirmed;
  }

  public String getEditScope() {
    return editScope;
  }

  public String getSubject() {
    return subjectField.getText().trim();
  }

  public String getEventLocation() {
    return locationField.getText().trim();
  }

  public String getDescription() {
    return descriptionArea.getText().trim();
  }

  /**
   * Gets the start time from the spinner.
   *
   * @return the start time
   */
  public LocalTime getStartTime() {
    java.util.Date date = (java.util.Date) startTimeSpinner.getValue();
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
  }

  /**
   * Gets the end time from the spinner.
   *
   * @return the end time
   */
  public LocalTime getEndTime() {
    java.util.Date date = (java.util.Date) endTimeSpinner.getValue();
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
  }

  public Event getOriginalEvent() {
    return eventToEdit;
  }

  public LocalDate getEventDate() {
    return eventToEdit.getStartDateTime().toLocalDate();
  }

  /**
   * this function returns the start date of an event.
   *
   * @return LocalDate
   */
  public LocalDate getStartDate() {
    String startDateText = startDateField.getText().trim();
    try {
      LocalDate parsed = LocalDate.parse(startDateText);
      return parsed;
    } catch (Exception e) {
      e.printStackTrace();
      return eventToEdit.getStartDateTime().toLocalDate();
    }
  }

  /**
   * this function returns the end date of an event.
   *
   * @return LocalDate
   */
  public LocalDate getEndDate() {
    String endDateText = endDateField.getText().trim();
    try {
      LocalDate parsed = LocalDate.parse(endDateText);
      return parsed;
    } catch (Exception e) {
      e.printStackTrace();
      return eventToEdit.getEndDateTime().toLocalDate();
    }
  }

  /**
   * Automatically updates the end date field when start date changes.
   * Maintains the original duration (days between start and end).
   */
  private void updateEndDateBasedOnStartDate() {
    if (isPopulating) {
      return;
    }
    try {
      String startDateText = startDateField.getText().trim();
      if (startDateText.isEmpty()) {
        return;
      }
      LocalDate newStartDate = LocalDate.parse(startDateText);
      LocalDate originalStartDate = eventToEdit.getStartDateTime().toLocalDate();
      LocalDate originalEndDate = eventToEdit.getEndDateTime().toLocalDate();
      long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(
          originalStartDate,
          originalEndDate
      );
      LocalDate newEndDate = newStartDate.plusDays(daysDifference);
      String currentEndDateText = endDateField.getText().trim();
      if (currentEndDateText.isEmpty()
              ||
          currentEndDateText.equals(originalEndDate.toString())) {
        endDateField.setText(newEndDate.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}