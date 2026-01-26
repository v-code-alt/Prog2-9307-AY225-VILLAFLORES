package Attendance;
/*
    Attendance.java
    A simple Java Swing application for attendance tracking.

    Features:
    - JFrame window with labeled fields: Attendance Name, Course/Year, Time In, E-Signature
    - Automatically sets Time In (formatted) and generates an E-Signature (UUID)
    - "Sign In" button to record Time In and E-Signature when user signs
    - "Clear" button to clear inputs and "Exit" to close the app
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.io.*;
import java.nio.file.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List; 

/*
 * Compile & Run (from workspace root 'd:\CODING PROJECTS\JAVA_PRELIM'):
 * javac Attendance\Attendance.java
 * java Attendance.Attendance
 */

public class Attendance extends JFrame {

    // --- Input fields (user-facing) ---
    // `nameField` - Attendance Name (Lastname, Firstname or preferred format)
    private final JTextField nameField = new JTextField(30);
    // `courseField` - Course or program identifier (Course/Year label in UI)
    private final JTextField courseField = new JTextField(20);
    // `yearLevelCombo` - Year level selector (1-4)
    private final JComboBox<String> yearLevelCombo = new JComboBox<>(new String[] {"1","2","3","4"});
    // `timeInField` - Program-set Time In value (read-only)
    private final JTextField timeInField = new JTextField(20);
    // `eSignatureField` - Program-generated E-Signature (UUID); use a wider field for visibility (read-only)
    private final JTextField eSignatureField = new JTextField(36);

    // (Placeholders removed) — fields accept direct input

    // Formatter for date+time display (yyyy-MM-dd hh:mm AM/PM)
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    // Model for storing sign-in records (admin-protected)
    // Columns: Name, Course, Year Level, Date & Time In, E-Signature
    // The table view is read-only: records can only be removed by an admin (Delete Selected).
    // Persistence is handled via a CSV file in the user's home directory.
    private final DefaultTableModel recordsTableModel = new DefaultTableModel(new String[] {"Name","Course","Year Level","Date & Time In", "E-Signature"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // All fields are read-only in the records table (admin can only Delete rows or Save changes made by other means)
            return false;
        }
    };
    private final JTable recordsTable = new JTable(recordsTableModel);
    // Admin password for viewing/deleting records. Replace with a safer value or configuration for production use.
    private static final String ADMIN_PASSWORD = "admin123"; // change this for better security
    // Use a consistent records path in the user's home folder so files persist across sessions
    private static final java.nio.file.Path RECORDS_PATH = Paths.get(System.getProperty("user.home"), ".attendance_records.csv");

    // Tracks whether the initial delete confirmation has already occurred (first delete will prompt)
    private boolean deleteConfirmedOnce = false;

    public Attendance() { 
        super("Attendance Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Make Time In and E-Signature read-only (program generated)
        timeInField.setEditable(false);
        timeInField.setFocusable(false);
        eSignatureField.setEditable(false);
        eSignatureField.setFocusable(false);
        // Improve visibility for the E-Signature: wider columns, monospaced font, and preferred width
        eSignatureField.setColumns(36);
        eSignatureField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        // Allow the e-signature field to resize with the window rather than forcing a fixed width
        eSignatureField.setPreferredSize(null);

        // --- Build form panel: labels and inputs are arranged in a GridLayout for consistent spacing ---
        // The footer button panel provides Sign In, Clear, View Records (admin), and Exit actions.

        // Build form panel with labels and fields using GridLayout for clarity
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Attendance Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Course:"));
        formPanel.add(courseField);

        formPanel.add(new JLabel("Year Level:"));
        formPanel.add(yearLevelCombo);

        formPanel.add(new JLabel("Date & Time In:"));
        formPanel.add(timeInField);

        formPanel.add(new JLabel("E-Signature:"));
        formPanel.add(eSignatureField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons panel (includes admin-protected View Records button)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton signInBtn = new JButton("Sign In");
        JButton clearBtn = new JButton("Clear");
        JButton viewBtn = new JButton("View Records");
        JButton exitBtn = new JButton("Exit");

        buttons.add(signInBtn);
        buttons.add(clearBtn);
        buttons.add(viewBtn);
        buttons.add(exitBtn);

        add(buttons, BorderLayout.SOUTH);

        // Action: when user clicks Sign In, perform validation and record the sign-in.
        // Steps:
        // 1) Validate required fields (Name and Course).
        // 2) Prevent duplicate sign-ins by Name (case-insensitive) to avoid accidental double entries.
        // 3) Capture current system date/time and generate an E-Signature (UUID).
        // 4) Append to the internal table model and persist to disk.
        signInBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String course = courseField.getText().trim();
                String year = (String) yearLevelCombo.getSelectedItem();
                if (name.isEmpty() || course.isEmpty()) {
                    JOptionPane.showMessageDialog(Attendance.this,
                            "Please enter Name and Course before signing in.",
                            "Missing information", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Prevent duplicate sign-ins by the same name (case-insensitive)
                for (int r = 0; r < recordsTableModel.getRowCount(); r++) {
                    Object existing = recordsTableModel.getValueAt(r, 0);
                    if (existing != null && name.equalsIgnoreCase(existing.toString().trim())) {
                        JOptionPane.showMessageDialog(Attendance.this,
                                "This name has already signed in.", "Already signed in", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                // Use formatted current date and time (yyyy-MM-dd hh:mm AM/PM)
                String timeIn = java.time.LocalDateTime.now().format(dateTimeFormatter);
                timeInField.setText(timeIn);

                // Use UUID for a programmatic E-Signature as requested
                String eSignature = java.util.UUID.randomUUID().toString();
                eSignatureField.setText(eSignature);
                // Show the beginning of the signature so it's visible (no horizontal scroll required)
                eSignatureField.setCaretPosition(0);

                // Add record to table model and persist it
                String[] row = new String[] { name, course, year, timeIn, eSignature };
                recordsTableModel.addRow(row);
                saveRecordsToFile();
            }
        });

        // Clear inputs (resets all fields to blank/default; read-only fields are cleared as well)
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.setText("");
                courseField.setText("");
                yearLevelCombo.setSelectedIndex(0);
                timeInField.setText("");
                eSignatureField.setText("");
            }
        });

        // View records - requires admin password to view the records dialog
        // The admin dialog displays all saved records in a scrollable table and permits deletion
        // Note: Delete prompts once per admin session; confirmation flag resets when dialog closes
        viewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPasswordField pwd = new JPasswordField();
                int option = JOptionPane.showConfirmDialog(Attendance.this, pwd,
                        "Enter admin password", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    String pass = new String(pwd.getPassword());
                    if (ADMIN_PASSWORD.equals(pass)) {
                        if (recordsTableModel.getRowCount() == 0) {
                            JOptionPane.showMessageDialog(Attendance.this, "No records to display.", "Records",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            recordsTable.setFillsViewportHeight(true);

                            // Ensure all columns are visible: turn off auto-resize and set preferred column widths
                            recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                            int[] colWidths = new int[] {180, 140, 80, 200, 320}; // Name, Course, Year, Date/Time, Signature
                            int totalWidth = 0;
                            for (int i = 0; i < colWidths.length && i < recordsTable.getColumnModel().getColumnCount(); i++) {
                                recordsTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
                                totalWidth += colWidths[i];
                            }

                            JScrollPane scroll = new JScrollPane(recordsTable);
                            scroll.setPreferredSize(new Dimension(Math.min(totalWidth + 20, 1000), 300));
                            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                            // Dialog with Delete and Close (Save removed)
                            JPanel panel = new JPanel(new BorderLayout());
                            panel.add(scroll, BorderLayout.CENTER);

                            JButton deleteBtn = new JButton("Delete Selected");
                            JButton closeBtn = new JButton("Close");

                            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                            btnPanel.add(deleteBtn);
                            btnPanel.add(closeBtn);
                            panel.add(btnPanel, BorderLayout.SOUTH);

                            final JDialog dialog = new JDialog(Attendance.this, "Attendance Records (Admin)", true);
                            dialog.getContentPane().add(panel);
                            dialog.pack();
                            dialog.setLocationRelativeTo(Attendance.this);

                            // Reset the delete confirmation flag when the dialog is closed (each admin session gets confirmation)
                            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosing(java.awt.event.WindowEvent e) {
                                    deleteConfirmedOnce = false;
                                }

                                @Override
                                public void windowClosed(java.awt.event.WindowEvent e) {
                                    deleteConfirmedOnce = false;
                                }
                            });

                            // Delete handler: prompts once per admin dialog session, then allows subsequent deletes without prompting
                            deleteBtn.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ev) {
                                    int sel = recordsTable.getSelectedRow();
                                    if (sel >= 0) {
                                        if (!deleteConfirmedOnce) {
                                            int confirm = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete this?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                            if (confirm == JOptionPane.YES_OPTION) {
                                                deleteConfirmedOnce = true;
                                                recordsTableModel.removeRow(sel);
                                                saveRecordsToFile();
                                            }
                                        } else {
                                            recordsTableModel.removeRow(sel);
                                            saveRecordsToFile();
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(dialog, "No row selected.", "Delete", JOptionPane.WARNING_MESSAGE);
                                    }
                                }
                            });

                            closeBtn.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ev) {
                                    // Reset confirmation flag when admin closes the dialog
                                    deleteConfirmedOnce = false;
                                    dialog.dispose();
                                }
                            });

                            dialog.setVisible(true);
                        }
                    } else {
                        JOptionPane.showMessageDialog(Attendance.this, "Incorrect password.", "Access denied",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Exit the application
        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Load saved records from disk (if any)
        loadRecordsFromFile();

        // Table UI settings: fill height and prevent column reordering
        recordsTable.setFillsViewportHeight(true);
        recordsTable.getTableHeader().setReorderingAllowed(false);
        // Ensure Course, Year Level, Date & Time In and E-Signature columns cannot be edited even if an editor exists
        try {
            recordsTable.getColumnModel().getColumn(1).setCellEditor(null);
            recordsTable.getColumnModel().getColumn(2).setCellEditor(null);
            recordsTable.getColumnModel().getColumn(3).setCellEditor(null);
            recordsTable.getColumnModel().getColumn(4).setCellEditor(null);
            // Make E-Signature column wider so full values display
            recordsTable.getColumnModel().getColumn(4).setPreferredWidth(260);
        } catch (Exception ex) {
            // ignore if columns are not yet available
        }

        // Ensure records are saved when the main window closes (protects against accidental data loss)
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveRecordsToFile();
            }
        });

        // Make the main window size adaptive: pack components, allow resizing, and set a reasonable minimum
        pack();
        setMinimumSize(new Dimension(380, 260));
        setLocationRelativeTo(null); // center on screen
        setResizable(true);
    }



    // Load records from CSV file into the table model
    // CSV layout: "Name","Course","Year Level","Date & Time In","E-Signature"
    private void loadRecordsFromFile() {
        java.nio.file.Path path = RECORDS_PATH;
        if (!Files.exists(path)) return;
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> cols = parseCsvLine(line);
                if (cols.size() >= 5) {
                    recordsTableModel.addRow(new String[] {cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4)});
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load records: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Save records from the table model to CSV file
    // Writes to a temporary file and performs an atomic move when supported to avoid partial writes
    private void saveRecordsToFile() {
        java.nio.file.Path path = RECORDS_PATH;
        java.nio.file.Path temp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(temp)) {
            for (int r = 0; r < recordsTableModel.getRowCount(); r++) {
                for (int c = 0; c < recordsTableModel.getColumnCount(); c++) {
                    Object val = recordsTableModel.getValueAt(r, c);
                    String s = val == null ? "" : val.toString();
                    s = s.replace("\"", "\"\""); // escape quotes
                    bw.write('"' + s + '"');
                    if (c < recordsTableModel.getColumnCount() - 1) bw.write(',');
                }
                bw.newLine();
            }
            bw.flush();
            // Atomically move temp to final location (replace existing)
            Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
            try {
                // Fall back to non-atomic move
                Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save records: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save records: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Ensure temp file is removed if something went wrong
            try {
                if (Files.exists(temp)) Files.deleteIfExists(temp);
            } catch (IOException ignored) {}
        }
    }

    // Very small CSV parser to handle quoted fields and escaped quotes
    // Handles CSV lines produced by saveRecordsToFile(): fields are quoted and quotes inside fields are doubled
    private List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    cols.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        cols.add(cur.toString());
        return cols;
    }

    public static void main(String[] args) {
        // Launch the Swing UI on the Event Dispatch Thread (EDT) as required for thread-safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Attendance app = new Attendance();
                app.setVisible(true);
            }
        });
    }
}
