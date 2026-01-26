import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PrelimGradeCalculator extends JFrame {
    private JTextField absencesField, lab1Field, lab2Field, lab3Field;
    private JPanel formPanel, resultsPanel;
    
    // Result labels
    private JLabel displayAttendance, displayAbsences, displayLab1, displayLab2, displayLab3;
    private JLabel displayLabAvg, displayLabAvgFormula, displayClassStanding, displayClassStandingFormula;
    private JLabel passScore, excellentScore;
    private JLabel passRemark, excellentRemark;
    
    public PrelimGradeCalculator() {
        setTitle("Prelim Grade Calculator");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(139, 0, 0));
        
        createFormPanel();
        createResultsPanel();
        
        // Show form panel initially
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    private void createFormPanel() {
        formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(139, 0, 0));
        
        // Wrapper panel for shadow effect
        JPanel shadowWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int shadowSize = 30;
                int x = shadowSize;
                int y = shadowSize;
                int width = getWidth() - (shadowSize * 2);
                int height = getHeight() - (shadowSize * 2);
                
                // Draw multiple shadow layers for depth
                for (int i = shadowSize; i > 0; i--) {
                    float alpha = (float)(shadowSize - i) / (float)shadowSize * 0.05f;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(x - i, y - i, width + (i * 2), height + (i * 2), 20, 20);
                }
                
                g2d.dispose();
            }
        };
        shadowWrapper.setOpaque(false);
        shadowWrapper.setLayout(new BorderLayout());
        shadowWrapper.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Create white container with max width
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        
        // Create glowing border effect with multiple borders
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
                BorderFactory.createLineBorder(new Color(218, 165, 32), 4)
            ),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        container.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        container.setPreferredSize(new Dimension(800, 700));
        
        // Title
        JLabel title = new JLabel("Prelim Grade Calculator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(139, 0, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Calculate your required Prelim Exam score to pass or achieve excellence.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(102, 102, 102));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        container.add(title);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
        container.add(subtitle);
        container.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Input fields - Changed to absences instead of attendances
        absencesField = createInputField(container, "Number of Absences");
        absencesField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextField) input).getText();
                try {
                    int value = Integer.parseInt(text);
                    return value >= 0 && value <= 4;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
        lab1Field = createInputField(container, "Lab Work 1 Grade");
        lab2Field = createInputField(container, "Lab Work 2 Grade");
        lab3Field = createInputField(container, "Lab Work 3 Grade");
        
        container.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Calculate button
        JButton calcButton = new JButton("Calculate");
        calcButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        calcButton.setBackground(new Color(139, 0, 0));
        calcButton.setForeground(new Color(255, 215, 0));
        calcButton.setFocusPainted(false);
        calcButton.setBorderPainted(false);
        calcButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        calcButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calcButton.addActionListener(e -> calculateGrade());
        
        container.add(calcButton);
        
        shadowWrapper.add(container, BorderLayout.CENTER);
        
        // Add shadow wrapper to form panel with GridBagConstraints to center it
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(shadowWrapper, gbc);
    }
    
    private JTextField createInputField(JPanel parent, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(51, 51, 51));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(label);
        
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
        
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(500, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        field.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    calculateGrade();
                }
            }
        });
        
        parent.add(field);
        parent.add(Box.createRigidArea(new Dimension(0, 20)));
        
        return field;
    }
    
    private void createResultsPanel() {
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new GridBagLayout());
        resultsPanel.setBackground(new Color(139, 0, 0));
        
        // Wrapper panel for shadow effect
        JPanel shadowWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int shadowSize = 30;
                int x = shadowSize;
                int y = shadowSize;
                int width = getWidth() - (shadowSize * 2);
                int height = getHeight() - (shadowSize * 2);
                
                // Draw multiple shadow layers for depth
                for (int i = shadowSize; i > 0; i--) {
                    float alpha = (float)(shadowSize - i) / (float)shadowSize * 0.05f;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(x - i, y - i, width + (i * 2), height + (i * 2), 20, 20);
                }
                
                g2d.dispose();
            }
        };
        shadowWrapper.setOpaque(false);
        shadowWrapper.setLayout(new BorderLayout());
        shadowWrapper.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Create white container with max width
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout(20, 20));
        container.setBackground(Color.WHITE);
        
        // Create glowing border effect
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
                BorderFactory.createLineBorder(new Color(218, 165, 32), 4)
            ),
            BorderFactory.createEmptyBorder(40, 30, 30, 30)
        ));
        container.setMaximumSize(new Dimension(1200, Integer.MAX_VALUE));
        container.setPreferredSize(new Dimension(1200, 650));
        
        // Two column layout
        JPanel twoColumnPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        twoColumnPanel.setBackground(Color.WHITE);
        
        // Left panel - Current Standing
        JPanel leftPanel = createCurrentStandingPanel();
        twoColumnPanel.add(leftPanel);
        
        // Right panel - Required Scores
        JPanel rightPanel = createRequiredScoresPanel();
        twoColumnPanel.add(rightPanel);
        
        container.add(twoColumnPanel, BorderLayout.CENTER);
        
        // Return button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton returnButton = new JButton("Return");
        returnButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        returnButton.setBackground(new Color(139, 0, 0));
        returnButton.setForeground(new Color(255, 215, 0));
        returnButton.setFocusPainted(false);
        returnButton.setBorderPainted(false);
        returnButton.setPreferredSize(new Dimension(200, 45));
        returnButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnButton.addActionListener(e -> returnToForm());
        
        buttonPanel.add(returnButton);
        container.add(buttonPanel, BorderLayout.SOUTH);
        
        shadowWrapper.add(container, BorderLayout.CENTER);
        
        // Add shadow wrapper to results panel with GridBagConstraints to center it
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        resultsPanel.add(shadowWrapper, gbc);
    }
    
    private JPanel createCurrentStandingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel header = new JLabel("Your Current Standing");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(new Color(139, 0, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Attendance with absences info
        JPanel attendancePanel = new JPanel();
        attendancePanel.setLayout(new BoxLayout(attendancePanel, BoxLayout.Y_AXIS));
        attendancePanel.setBackground(new Color(248, 249, 250));
        attendancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        attendancePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel attendanceRow = new JPanel(new BorderLayout());
        attendanceRow.setBackground(new Color(248, 249, 250));
        JLabel attendanceLabel = new JLabel("Attendance Score");
        attendanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        attendanceLabel.setForeground(new Color(102, 102, 102));
        displayAttendance = new JLabel("-");
        displayAttendance.setFont(new Font("Segoe UI", Font.BOLD, 14));
        displayAttendance.setForeground(new Color(51, 51, 51));
        attendanceRow.add(attendanceLabel, BorderLayout.WEST);
        attendanceRow.add(displayAttendance, BorderLayout.EAST);
        
        displayAbsences = new JLabel("");
        displayAbsences.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        displayAbsences.setForeground(new Color(102, 102, 102));
        displayAbsences.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        attendancePanel.add(attendanceRow);
        attendancePanel.add(Box.createRigidArea(new Dimension(0, 3)));
        attendancePanel.add(displayAbsences);
        
        panel.add(attendancePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        displayLab1 = addResultRow(panel, "Lab Work 1");
        displayLab2 = addResultRow(panel, "Lab Work 2");
        displayLab3 = addResultRow(panel, "Lab Work 3");
        
        // Lab Work Average with formula
        JPanel labAvgPanel = new JPanel();
        labAvgPanel.setLayout(new BoxLayout(labAvgPanel, BoxLayout.Y_AXIS));
        labAvgPanel.setBackground(new Color(248, 249, 250));
        labAvgPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        labAvgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel labAvgRow = new JPanel(new BorderLayout());
        labAvgRow.setBackground(new Color(248, 249, 250));
        JLabel labAvgLabel = new JLabel("Lab Work Average");
        labAvgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labAvgLabel.setForeground(new Color(102, 102, 102));
        displayLabAvg = new JLabel("-");
        displayLabAvg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        displayLabAvg.setForeground(new Color(51, 51, 51));
        labAvgRow.add(labAvgLabel, BorderLayout.WEST);
        labAvgRow.add(displayLabAvg, BorderLayout.EAST);
        
        displayLabAvgFormula = new JLabel("");
        displayLabAvgFormula.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        displayLabAvgFormula.setForeground(new Color(102, 102, 102));
        displayLabAvgFormula.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        labAvgPanel.add(labAvgRow);
        labAvgPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        labAvgPanel.add(displayLabAvgFormula);
        
        panel.add(labAvgPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Class Standing with formula
        JPanel csPanel = new JPanel();
        csPanel.setLayout(new BoxLayout(csPanel, BoxLayout.Y_AXIS));
        csPanel.setBackground(new Color(248, 249, 250));
        csPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        csPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel csRow = new JPanel(new BorderLayout());
        csRow.setBackground(new Color(248, 249, 250));
        JLabel csLabel = new JLabel("Class Standing");
        csLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        csLabel.setForeground(new Color(102, 102, 102));
        displayClassStanding = new JLabel("-");
        displayClassStanding.setFont(new Font("Segoe UI", Font.BOLD, 14));
        displayClassStanding.setForeground(new Color(51, 51, 51));
        csRow.add(csLabel, BorderLayout.WEST);
        csRow.add(displayClassStanding, BorderLayout.EAST);
        
        displayClassStandingFormula = new JLabel("");
        displayClassStandingFormula.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        displayClassStandingFormula.setForeground(new Color(102, 102, 102));
        displayClassStandingFormula.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        csPanel.add(csRow);
        csPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        csPanel.add(displayClassStandingFormula);
        
        panel.add(csPanel);
        
        return panel;
    }
    
    private JLabel addResultRow(JPanel parent, String labelText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(248, 249, 250));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(102, 102, 102));
        
        JLabel value = new JLabel("-");
        value.setFont(new Font("Segoe UI", Font.BOLD, 14));
        value.setForeground(new Color(51, 51, 51));
        
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        
        parent.add(row);
        parent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        return value;
    }
    
    private JPanel createRequiredScoresPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(139, 0, 0, 21));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel header = new JLabel("Required Prelim Exam Scores");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(new Color(139, 0, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Pass panel
        JPanel passPanel = createTargetCard("To Pass (75)");
        passScore = (JLabel) passPanel.getComponent(2);
        passRemark = (JLabel) passPanel.getComponent(4);
        panel.add(passPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Excellent panel
        JPanel excellentPanel = createTargetCard("For Excellent Standing (100)");
        excellentScore = (JLabel) excellentPanel.getComponent(2);
        excellentRemark = (JLabel) excellentPanel.getComponent(4);
        panel.add(excellentPanel);
        
        return panel;
    }
    
    private JPanel createTargetCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel score = new JLabel("-");
        score.setFont(new Font("Segoe UI", Font.BOLD, 24));
        score.setForeground(new Color(51, 51, 51));
        score.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(score);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel remark = new JLabel("");
        remark.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remark.setOpaque(true);
        remark.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        remark.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(remark);
        
        return card;
    }
    
    private void calculateGrade() {
        try {
            // Get absences and calculate attendance score
            double absences = validateInput(absencesField.getText(), "Absences");
            double attendanceScore = 100 - absences;
            
            double lab1 = validateInput(lab1Field.getText(), "Lab Work 1");
            double lab2 = validateInput(lab2Field.getText(), "Lab Work 2");
            double lab3 = validateInput(lab3Field.getText(), "Lab Work 3");
            
            // Calculate Lab Work Average
            double labWorkAverage = (lab1 + lab2 + lab3) / 3.0;
            
            // Calculate Class Standing
            double classStanding = (attendanceScore * 0.40) + (labWorkAverage * 0.60);
            
            // Calculate required Prelim Exam scores
            double requiredForPass = (75 - (classStanding * 0.30)) / 0.70;
            double requiredForExcellent = (100 - (classStanding * 0.30)) / 0.70;
            
            // Display results
            displayAttendance.setText(Math.round(attendanceScore) + "%");
            displayAbsences.setText((int)absences + " absences");
            displayLab1.setText(String.format("%.2f", lab1));
            displayLab2.setText(String.format("%.2f", lab2));
            displayLab3.setText(String.format("%.2f", lab3));
            displayLabAvg.setText(String.valueOf(Math.round(labWorkAverage)));
            displayLabAvgFormula.setText(String.format("(%d + %d + %d) / 3 = %d", 
                Math.round(lab1), Math.round(lab2), Math.round(lab3), Math.round(labWorkAverage)));
            displayClassStanding.setText(String.valueOf(Math.round(classStanding)));
            displayClassStandingFormula.setText("40% Attendance + 60% Lab Avg");
            
            // Display required scores - show empty if > 100
            if (requiredForPass > 100) {
                passScore.setText("");
            } else {
                passScore.setText(String.valueOf(Math.round(requiredForPass)));
            }
            
            if (requiredForExcellent > 100) {
                excellentScore.setText("");
            } else {
                excellentScore.setText(String.valueOf(Math.round(requiredForExcellent)));
            }
            
            // Pass remarks
            if (requiredForPass <= 0) {
                passRemark.setText("<html><center>You already passed!<br>Your Class Standing is excellent!</center></html>");
                passRemark.setBackground(new Color(212, 237, 218));
                passRemark.setForeground(new Color(21, 87, 36));
            } else if (requiredForPass <= 100) {
                passRemark.setText(String.format("<html><center>You need to score %d on the Prelim Exam to pass.</center></html>", 
                    Math.round(requiredForPass)));
                passRemark.setBackground(new Color(255, 243, 205));
                passRemark.setForeground(new Color(133, 100, 4));
            } else {
                passRemark.setText("<html><center>Unfortunately, it is not possible to pass<br>even with a perfect Prelim Exam score.</center></html>");
                passRemark.setBackground(new Color(248, 215, 218));
                passRemark.setForeground(new Color(114, 28, 36));
            }
            
            // Excellent remarks
            if (requiredForExcellent <= 0) {
                excellentRemark.setText("<html><center>Perfect! You already have<br>an excellent standing!</center></html>");
                excellentRemark.setBackground(new Color(212, 237, 218));
                excellentRemark.setForeground(new Color(21, 87, 36));
            } else if (requiredForExcellent <= 100) {
                excellentRemark.setText(String.format("<html><center>You need to score %d on the Prelim Exam for excellence.</center></html>", 
                    Math.round(requiredForExcellent)));
                excellentRemark.setBackground(new Color(255, 243, 205));
                excellentRemark.setForeground(new Color(133, 100, 4));
            } else {
                excellentRemark.setText("<html><center>It is not possible to achieve 100%<br>even with a perfect Prelim Exam score.</center></html>");
                excellentRemark.setBackground(new Color(248, 215, 218));
                excellentRemark.setForeground(new Color(114, 28, 36));
            }
            
            // Switch to results panel
            getContentPane().removeAll();
            add(resultsPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private double validateInput(String value, String fieldName) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Please enter a value for " + fieldName);
        }
        
        try {
            double num = Double.parseDouble(value.trim());
            if (fieldName.toLowerCase().contains("absence")) {
                if (num < 0 || num > 4) {
                    throw new NumberFormatException(fieldName + " must be between 0 and 4");
                }
            } else {
                if (num < 0 || num > 100) {
                    throw new NumberFormatException(fieldName + " must be between 0 and 100");
                }
            }
            return num;
        } catch (NumberFormatException e) {
            if (fieldName.toLowerCase().contains("absence")) {
                throw new NumberFormatException("Please enter a valid number for " + fieldName + " (0-4)");
            } else {
                throw new NumberFormatException("Please enter a valid number for " + fieldName + " (0-100)");
            }
        }
    }
    
    private void returnToForm() {
        absencesField.setText("");
        lab1Field.setText("");
        lab2Field.setText("");
        lab3Field.setText("");
        
        getContentPane().removeAll();
        add(formPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new PrelimGradeCalculator();
            }
        });
    }
}