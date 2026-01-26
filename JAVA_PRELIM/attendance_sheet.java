/*
    display windows using java swing (jframe)
    labeled fields:
        Attendance Name
        Course/Year
        Time In (automatically displayed current time)
        E-Signature (programmatically generated e.g., based on timestamp, random number, or UUID)
    window is properly formatted and readable
    NO PROGRAM ERRORS
*/

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
/*
    Attendance Sheet Application
    This application creates a GUI window using Java Swing to display an attendance sheet
    with labeled fields for Attendance Name, Course/Year, Time In, and E-Signature.
*/

public class attendance_sheet {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Attendance Sheet");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);

        // Top form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel nameLabel = new JLabel("Attendance Name:");
        JTextField nameField = new JTextField();

        JLabel courseLabel = new JLabel("Course/Year:");
        JTextField courseField = new JTextField();

        JLabel timeLabel = new JLabel("Time In:");
        JTextField timeField = new JTextField();
        timeField.setEditable(false);
        timeField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        JLabel signatureLabel = new JLabel("E-Signature:");
        JTextField signatureField = new JTextField();
        signatureField.setEditable(false);
        signatureField.setText(UUID.randomUUID().toString());

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(courseLabel);
        formPanel.add(courseField);
        formPanel.add(timeLabel);
        formPanel.add(timeField);
        formPanel.add(signatureLabel);
        formPanel.add(signatureField);

        // Table for admin view
        String[] cols = new String[]{"Attendance Name", "Course/Year", "Time In", "E-Signature"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton addButton = new JButton("Add Entry");
        JButton removeButton = new JButton("Remove Selected");
        JButton clearButton = new JButton("Clear Fields");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);

        // Wire up actions
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String course = courseField.getText().trim();
                if (name.isEmpty() || course.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both Name and Course/Year.", "Missing Data", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String timeIn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String signature = UUID.randomUUID().toString();

                timeField.setText(timeIn);
                signatureField.setText(signature);

                tableModel.addRow(new Object[]{name, course, timeIn, signature});

                nameField.setText("");
                courseField.setText("");
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = table.getSelectedRow();
                if (selected >= 0) {
                    tableModel.removeRow(selected);
                } else {
                    JOptionPane.showMessageDialog(frame, "No row selected.", "Remove", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.setText("");
                courseField.setText("");
                timeField.setText("");
                signatureField.setText("");
            }
        });

        // Layout
        frame.setLayout(new BorderLayout());
        frame.add(formPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(tableScroll, BorderLayout.SOUTH);

        // Adjust sizes so table expands
        tableScroll.setPreferredSize(new Dimension(680, 260));

        frame.pack();
        frame.setVisible(true);
    }
}
