package calendar.view;

import calendar.dto.QueryResultDto;
import calendar.interfacetypes.IguiViewCalendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Calendar GUI View - displays calendar and manages UI components.
 * Controller handles all business logic and event wiring.
 */
public class GuiViewCalendar extends JFrame implements IguiViewCalendar {

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private JLabel monthYearLabel;
  private JPanel calendarGrid;
  private JTextArea eventArea;
  private JButton createEventButton;
  private JButton editEventButton;
  private JComboBox<String> calendarDropdown;
  private JButton createCalendarButton;
  private JButton editCalendarButton;
  private JLabel statusLabel;
  private JButton prevBtn;
  private JButton nextBtn;
  private JButton todayBtn;
  private JButton editEventsBySearchButton;
  private Consumer<LocalDate> dateSelectionListener;
  private final ResultFormatter formatter;

  /**
   * ok.
   */
  public GuiViewCalendar() {
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    this.formatter = new ResultFormatter();
    setupWindow();
    initComponents();
    layoutComponents();
    renderCalendar();
  }

  private void setupWindow() {
    setTitle("MVCalendar - GUI View");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(900, 650);
    setLocationRelativeTo(null);
  }

  private void initComponents() {
    calendarDropdown = new JComboBox<>();
    calendarDropdown.setPreferredSize(new Dimension(200, 30));
    createEventButton = new JButton("+ Event creation");
    createCalendarButton = new JButton("+ New Calendar");
    editCalendarButton = new JButton("✎ Edit");
    editEventButton = new JButton("Edit Event");
    monthYearLabel = new JLabel("", SwingConstants.CENTER);
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 20));
    editEventsBySearchButton = new JButton("Search & Edit");

    calendarGrid = new JPanel(new GridLayout(7, 7, 2, 2));
    calendarGrid.setBackground(Color.GRAY);

    eventArea = new JTextArea();
    eventArea.setEditable(false);
    eventArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
    eventArea.setText("Click a date to see events here");

    eventArea.setLineWrap(true);
    eventArea.setWrapStyleWord(true);
    statusLabel = new JLabel("Ready");
    statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
  }

  private void layoutComponents() {
    setLayout(new BorderLayout(10, 10));

    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    JLabel title = new JLabel("My Calendar", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 24));
    topPanel.add(title, BorderLayout.NORTH);

    JPanel calendarSelectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
    calendarSelectorPanel.add(new JLabel("Active Calendar:"));
    calendarSelectorPanel.add(calendarDropdown);
    calendarSelectorPanel.add(createCalendarButton);
    calendarSelectorPanel.add(editCalendarButton);
    topPanel.add(calendarSelectorPanel, BorderLayout.CENTER);

    add(topPanel, BorderLayout.NORTH);

    JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
    centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel calendarPanel = new JPanel(new BorderLayout(5, 5));

    JPanel navPanel = new JPanel(new BorderLayout());
    calendarPanel.add(navPanel, BorderLayout.NORTH);
    calendarPanel.add(calendarGrid, BorderLayout.CENTER);
    centerPanel.add(calendarPanel, BorderLayout.CENTER);


    this.prevBtn = new JButton("◀ Previous");
    this.nextBtn = new JButton("Next ▶");
    this.todayBtn = new JButton("Today");
    JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    rightNav.add(todayBtn);
    rightNav.add(nextBtn);

    navPanel.add(prevBtn, BorderLayout.WEST);
    navPanel.add(monthYearLabel, BorderLayout.CENTER);
    navPanel.add(rightNav, BorderLayout.EAST);

    JPanel eventPanel = new JPanel(new BorderLayout());
    eventPanel.setBorder(BorderFactory.createTitledBorder("Events"));
    eventPanel.setPreferredSize(new Dimension(300, 0));
    JScrollPane eventScroll = new JScrollPane(eventArea);
    eventScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    eventScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    eventPanel.add(eventScroll, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
    JButton refreshBtn = new JButton("Refresh");
    refreshBtn.addActionListener(e -> renderCalendar());

    buttonPanel.add(createEventButton);
    buttonPanel.add(editEventButton);
    buttonPanel.add(editEventsBySearchButton);
    buttonPanel.add(refreshBtn);

    eventPanel.add(buttonPanel, BorderLayout.SOUTH);

    centerPanel.add(eventPanel, BorderLayout.EAST);

    add(centerPanel, BorderLayout.CENTER);
    add(statusLabel, BorderLayout.SOUTH);
  }

  private void renderCalendar() {
    calendarGrid.removeAll();

    String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    monthYearLabel.setText(monthName + " " + currentMonth.getYear());

    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : days) {
      JLabel header = new JLabel(day, SwingConstants.CENTER);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setOpaque(true);
      header.setBackground(new Color(200, 200, 200));
      header.setBorder(new LineBorder(Color.GRAY));
      calendarGrid.add(header);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

    for (int i = 0; i < firstDayOfWeek; i++) {
      JPanel empty = new JPanel();
      empty.setBackground(Color.LIGHT_GRAY);
      calendarGrid.add(empty);
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JPanel dayPanel = createDayPanel(date);
      calendarGrid.add(dayPanel);
    }

    int totalCells = firstDayOfWeek + daysInMonth;
    int remaining = 42 - totalCells;
    for (int i = 0; i < remaining; i++) {
      JPanel empty = new JPanel();
      empty.setBackground(Color.LIGHT_GRAY);
      calendarGrid.add(empty);
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  private JPanel createDayPanel(LocalDate date) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new LineBorder(Color.GRAY));
    panel.setPreferredSize(new Dimension(80, 60));

    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
    dayLabel.setFont(new Font("Arial", Font.BOLD, 14));
    panel.add(dayLabel, BorderLayout.NORTH);

    Color bgColor = Color.WHITE;

    if (date.equals(selectedDate)) {
      bgColor = new Color(173, 216, 230);
    } else if (date.equals(LocalDate.now())) {
      bgColor = new Color(255, 255, 200);
    } else if (date.getDayOfWeek().getValue() >= 6) {
      bgColor = new Color(245, 245, 245);
    }

    panel.setBackground(bgColor);

    panel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (dateSelectionListener != null) {
          dateSelectionListener.accept(date);
        }
      }

      public void mouseEntered(java.awt.event.MouseEvent e) {
        if (!date.equals(selectedDate)) {
          panel.setBorder(new LineBorder(Color.BLUE, 2));
        }
      }

      public void mouseExited(java.awt.event.MouseEvent e) {
        if (!date.equals(selectedDate)) {
          panel.setBorder(new LineBorder(Color.GRAY, 1));
        } else {
          panel.setBorder(new LineBorder(Color.BLUE, 3));
        }
      }
    });

    if (date.equals(selectedDate)) {
      panel.setBorder(new LineBorder(Color.BLUE, 3));
    }

    return panel;
  }

  /**
   * Updates the calendar dropdown list.
   * Called by controller when calendars change.
   */
  public void updateCalendarList(List<String> calendarNames, String activeCalendar) {
    calendarDropdown.removeAllItems();

    if (calendarNames == null || calendarNames.isEmpty()) {
      calendarDropdown.addItem("(No calendars)");
      calendarDropdown.setEnabled(false);
    } else {
      calendarDropdown.setEnabled(true);
      for (String name : calendarNames) {
        calendarDropdown.addItem(name);
      }
      if (activeCalendar != null) {
        calendarDropdown.setSelectedItem(activeCalendar);
      }
    }

    statusLabel.setText("Active Calendar: " + activeCalendar);
  }

  /**
   * Updates the status message at the bottom.
   */
  public void setStatus(String message) {
    statusLabel.setText(message);
  }

  /**
   * Shows an error dialog.
   */
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows a success message.
   */
  public void showSuccess(String message) {
    JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows a message.
   */
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Gets the calendar dropdown component.
   * Controller uses this to wire up selection listener.
   */
  public JComboBox<String> getCalendarDropdown() {
    return calendarDropdown;
  }

  /**
   * Gets the create calendar button.
   * Controller uses this to wire up click listener.
   */
  public JButton getCreateCalendarButton() {
    return createCalendarButton;
  }

  /**
   * Gets the currently selected date.
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Gets the edit calendar button.
   * Controller uses this to wire up click listener.
   */
  public JButton getEditCalendarButton() {
    return editCalendarButton;
  }

  public JButton getCreateEventButton() {
    return createEventButton;
  }

  public JButton getEditEventButton() {
    return editEventButton;
  }

  public JButton getEditEventsBySearchButton() {
    return editEventsBySearchButton;
  }

  /**
   * Called by Controller to update the calendar grid.
   */
  public void updateCalendarView(YearMonth month, LocalDate date) {
    this.currentMonth = month;
    this.selectedDate = date;
    renderCalendar();
  }

  /**
   * ok.
   *
   * @param prev ok.
   * @param next ok.
   * @param today ok.
   */
  public void addNavigationListeners(java.awt.event.ActionListener prev,
                                     java.awt.event.ActionListener next,
                                     java.awt.event.ActionListener today) {
    this.prevBtn.addActionListener(prev);
    this.nextBtn.addActionListener(next);
    this.todayBtn.addActionListener(today);
  }

  /**
   * Allows the Controller to subscribe to date click events.
   */
  public void setDateSelectionListener(Consumer<LocalDate> listener) {
    this.dateSelectionListener = listener;
  }

  /**
   * Displays events using the existing ResultFormatter.
   * Reuses console formatting logic for consistency.
   */
  public void displayEventsForQueryResult(LocalDate date, QueryResultDto result) {
    StringBuilder sb = new StringBuilder();
    sb.append("Events on ").append(date).append(":\n");
    sb.append("═".repeat(40)).append("\n\n");
    if (result == null) {
      sb.append("Error: Result is null");
    } else if (result.getEvents() == null || result.getEvents().isEmpty()) {
      sb.append("No events scheduled for this day.");
    } else {
      String formattedEvents = formatter.format(result);
      sb.append(formattedEvents);
    }
    eventArea.setText(sb.toString());
    eventArea.setCaretPosition(0);
  }

  @Override
  public JFrame getFrame() {
    return this;
  }
}