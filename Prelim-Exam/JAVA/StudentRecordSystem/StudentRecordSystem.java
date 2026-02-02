// Joellen T. Villaflores, 25-0775-853
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class StudentRecordSystem extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField idField, nameField, gradeField;
    
    public StudentRecordSystem() {
        super("Records - Joellen T. Villaflores 25-0775-853");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Grade"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        idField = new JTextField();
        nameField = new JTextField();
        gradeField = new JTextField();
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        
        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Grade:"));
        inputPanel.add(gradeField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Load data from CSV
        loadCSV();
        
        addButton.addActionListener(e -> addRow());
        deleteButton.addActionListener(e -> deleteRow());
    }

    private void loadCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader("../MOCK_DATA.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    tableModel.addRow(parts);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRow() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String grade = gradeField.getText().trim();
        if (!id.isEmpty() && !name.isEmpty() && !grade.isEmpty()) {
            tableModel.addRow(new String[]{id, name, grade});
            idField.setText("");
            nameField.setText("");
            gradeField.setText("");
        }
    }

    private void deleteRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRecordSystem().setVisible(true));
    }
}
