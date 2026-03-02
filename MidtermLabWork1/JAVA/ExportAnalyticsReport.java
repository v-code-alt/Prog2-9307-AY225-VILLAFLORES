// ================================================================
//  FILE:    ExportAnalyticsReport.java
//  AUTHOR:  Villaflores
//  PURPOSE: Reads a VGChartz CSV dataset, performs analytics on
//           the video game sales data, displays the results in a
//           Swing window, and exports a summary_report.csv file.
//
//  HOW TO COMPILE:
//    javac ExportAnalyticsReport.java
//
//  HOW TO RUN:
//    java ExportAnalyticsReport
//
//  REQUIREMENTS MET:
//    - Scanner equivalent (JTextField / JOptionPane) for file input
//    - File class used for path validation
//    - Loops until a valid file path is entered
//    - Loads dataset into memory (ArrayList)
//    - Performs analytics and displays results
//    - Exports summary_report.csv using FileWriter
//
//  MATH FUNCTIONS USED (all inside the Analytics inner class):
//    Math.max()   — finds the highest single-game sales figure
//    Math.min()   — finds the lowest non-zero single-game sales
//    Math.round() — rounds the average sales to 2 decimal places
//    Math.pow()   — squares differences from mean (std deviation)
//    Math.sqrt()  — converts variance into standard deviation
// ================================================================

// ── Swing imports for building the window/GUI ─────────────────────
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

// ── AWT imports for layout, colors, and event handling ───────────
import java.awt.*;
import java.awt.event.*;

// ── Java I/O imports for reading and writing files ────────────────
import java.io.*;

// ── Utility imports for formatting numbers and collections ────────
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

// ── Main class — extends JFrame to create a window application ────
public class ExportAnalyticsReport extends JFrame {

    // ── GUI COMPONENT FIELDS ──────────────────────────────────────
    // These are declared here so they can be accessed from multiple methods.

    private JTextField filePathField;  // Text box where the user types or sees the file path
    private JButton browseButton;      // Opens a file picker dialog
    private JButton loadButton;        // Triggers loading and analysis of the CSV
    private JButton exportButton;      // Saves the summary report as a CSV file
    private JTextArea resultArea;      // Large text box that shows the analytics results
    private JLabel statusLabel;        // Bottom bar that shows current program status

    // ── DATA FIELDS ───────────────────────────────────────────────
    // 'records' holds every row from the CSV as a String array.
    // 'analytics' is our custom inner class that performs all the math.

    private List<String[]> records = new ArrayList<>();  // Each element = one game row
    private Analytics analytics;                          // Computed results from the data

    // ── CSV COLUMN INDEX CONSTANTS ────────────────────────────────
    // The CSV has many columns. These constants map each column name
    // to its 0-based index position so we don't use "magic numbers".
    // Example: row[COL_TITLE] gives us the game title for that row.

    static final int COL_TITLE        = 1;   // Game title
    static final int COL_CONSOLE      = 2;   // Console/platform (PS4, X360, etc.)
    static final int COL_GENRE        = 3;   // Genre (Action, Sports, etc.)
    static final int COL_PUBLISHER    = 4;   // Publisher name
    static final int COL_CRITIC       = 6;   // Critic score (0–10)
    static final int COL_TOTAL_SALES  = 7;   // Total global sales in millions
    static final int COL_NA_SALES     = 8;   // North America sales in millions
    static final int COL_JP_SALES     = 9;   // Japan sales in millions
    static final int COL_PAL_SALES    = 10;  // PAL region (Europe/Australia) in millions
    static final int COL_OTHER_SALES  = 11;  // Other regions sales in millions

    // ── NUMBER FORMATTERS ─────────────────────────────────────────
    // df2 formats numbers with 2 decimal places and comma separators.
    // df0 formats numbers as whole integers with comma separators.
    // Example: df2.format(1234.5) → "1,234.50"
    //          df0.format(64016)  → "64,016"

    private final DecimalFormat df2 = new DecimalFormat("#,##0.00");
    private final DecimalFormat df0 = new DecimalFormat("#,##0");

    // ── COLOR CONSTANTS ───────────────────────────────────────────
    // Defined once here so the same colors are reused across the UI.

    private static final Color BLUE   = new Color(30,  120, 200);  // Used for header and browse button
    private static final Color GREEN  = new Color(34,  153,  84);  // Used for the Load button
    private static final Color ORANGE = new Color(214, 122,   0);  // Used for the Export button
    private static final Color BG     = new Color(245, 248, 252);  // Light blue-grey background
    private static final Color PANEL  = new Color(235, 242, 252);  // Slightly darker panel background


    // ════════════════════════════════════════════════════════════════
    //  INNER CLASS: Analytics
    //  ─────────────────────
    //  This class is responsible for ALL mathematical calculations.
    //  It takes the raw list of CSV records and computes:
    //    - Totals (global sales, regional sales, critic scores)
    //    - Max/Min single-game sales using Math.max() / Math.min()
    //    - Average sales per game using Math.round()
    //    - Standard deviation using Math.pow() and Math.sqrt()
    //    - Grouped sales by genre, console, and publisher
    //    - A sorted top 10 best-selling games list
    //
    //  All results are stored as final (read-only) fields so they
    //  can be safely read by the display and export methods later.
    // ════════════════════════════════════════════════════════════════
    static class Analytics {

        // ── RESULT FIELDS (populated in the constructor) ──────────
        final int    totalGames;          // Total number of game entries in the dataset
        final double totalSales;          // Sum of all global sales (in millions)
        final double naSales;             // Sum of North America sales
        final double jpSales;             // Sum of Japan sales
        final double palSales;            // Sum of PAL region (Europe/AUS) sales
        final double otherSales;          // Sum of other regions' sales
        final double avgCriticScore;      // Average critic score across all rated games
        final int    criticCount;         // How many games have a critic score

        // Results from Math functions:
        final double highestSale;         // Math.max() — highest single-game total sales
        final double lowestSale;          // Math.min() — lowest non-zero single-game sales
        final double avgSalesPerGame;     // Math.round() — mean sales per game, rounded
        final double salesStdDev;         // Math.pow() + Math.sqrt() — standard deviation

        // Title of the games with the highest and lowest sales
        final String highestGame;
        final String lowestGame;

        // Maps grouping total sales by category (used for Top 5 tables)
        final Map<String, Double> salesByGenre;
        final Map<String, Double> salesByConsole;
        final Map<String, Double> salesByPublisher;

        // Sorted list of the top 10 best-selling individual game entries
        final List<String[]> top10Games;


        // ── CONSTRUCTOR ───────────────────────────────────────────
        // This is where all the math happens.
        // It loops through every record TWICE:
        //   Pass 1 — accumulate totals, find max, find min, group by category
        //   Pass 2 — calculate standard deviation (requires the mean from Pass 1)

        Analytics(List<String[]> records) {

            // ── Initialize running totals ─────────────────────────
            int    games        = records.size(); // Total number of rows/games
            double ts           = 0;              // Total global sales accumulator
            double na           = 0;              // North America sales accumulator
            double jp           = 0;              // Japan sales accumulator
            double pal          = 0;              // PAL sales accumulator
            double ot           = 0;              // Other region sales accumulator
            double totalCritic  = 0;              // Sum of critic scores (for average)
            int    cCount       = 0;              // Count of games that have a critic score

            // ── Initialize Math.max / Math.min trackers ───────────
            // We start maxSale at the smallest possible double so the
            // first real value will always be larger (i.e., becomes max).
            // We start minSale at the largest possible double so the
            // first real non-zero value will always be smaller (i.e., becomes min).
            double maxSale = Double.MIN_VALUE;
            double minSale = Double.MAX_VALUE;
            String maxGame = "";  // Will hold the title of the highest-selling game
            String minGame = "";  // Will hold the title of the lowest-selling game

            // ── Initialize grouping maps (TreeMap keeps keys sorted A-Z) ──
            Map<String, Double> byGenre     = new TreeMap<>();
            Map<String, Double> byConsole   = new TreeMap<>();
            Map<String, Double> byPublisher = new TreeMap<>();


            // ════════════════════════════════════════════════════
            //  PASS 1 — Loop through every record once
            //  Accumulate totals, track max/min, group by category
            // ════════════════════════════════════════════════════
            for (String[] r : records) {

                // Read the numeric values from each column safely
                // (safeDouble returns 0 if the cell is blank or not a number)
                double sale = safeDouble(r, COL_TOTAL_SALES);
                double naS  = safeDouble(r, COL_NA_SALES);
                double jpS  = safeDouble(r, COL_JP_SALES);
                double palS = safeDouble(r, COL_PAL_SALES);
                double otS  = safeDouble(r, COL_OTHER_SALES);
                double cs   = safeDouble(r, COL_CRITIC);

                // Add this game's values to the running totals
                ts  += sale;
                na  += naS;
                jp  += jpS;
                pal += palS;
                ot  += otS;

                // Only count critic scores that actually exist (greater than 0)
                if (cs > 0) {
                    totalCritic += cs;
                    cCount++;
                }

                // ── Math.max() ────────────────────────────────────
                // Compare this game's sales against the current maximum.
                // If this game has more sales, it becomes the new max.
                maxSale = Math.max(maxSale, sale);
                if (sale == maxSale) {
                    // Record which game holds the highest sales title
                    maxGame = r[COL_TITLE] + " (" + r[COL_CONSOLE] + ")";
                }

                // ── Math.min() ────────────────────────────────────
                // We skip games with 0 sales to avoid counting missing data.
                // Among non-zero games, track the one with the fewest sales.
                if (sale > 0) {
                    minSale = Math.min(minSale, sale);
                    if (sale == minSale) {
                        // Record which game holds the lowest sales title
                        minGame = r[COL_TITLE] + " (" + r[COL_CONSOLE] + ")";
                    }
                }

                // ── Group sales by category ───────────────────────
                // safeCol returns the cell value, or "Unknown" if it's blank.
                // merge() adds the sale amount to the existing total for that key.
                // If the key doesn't exist yet, it starts at 0 and adds sale.
                String genre   = safeCol(r, COL_GENRE,     "Unknown");
                String console = safeCol(r, COL_CONSOLE,   "Unknown");
                String pub     = safeCol(r, COL_PUBLISHER, "Unknown");

                byGenre.merge(genre,   sale, Double::sum);
                byConsole.merge(console, sale, Double::sum);
                byPublisher.merge(pub, sale, Double::sum);

            } // ── End of Pass 1 ────────────────────────────────────


            // ── Calculate the mean (average) sales per game ───────
            // This is needed BEFORE Pass 2 because standard deviation
            // requires the mean to compute each game's deviation from it.
            double mean = (games > 0) ? ts / games : 0;


            // ════════════════════════════════════════════════════
            //  PASS 2 — Standard Deviation calculation
            //  Formula: stdDev = sqrt( sum((x - mean)^2) / N )
            //  where x = each game's sales, N = total number of games
            // ════════════════════════════════════════════════════
            double sumSquaredDiffs = 0;

            for (String[] r : records) {
                double sale = safeDouble(r, COL_TOTAL_SALES);

                // ── Math.pow(x, 2) ────────────────────────────────
                // Compute (sale - mean)^2 — the squared difference from the mean.
                // Squaring ensures negative differences don't cancel out positives.
                sumSquaredDiffs += Math.pow(sale - mean, 2);
            }

            // ── Math.sqrt() ───────────────────────────────────────
            // Divide total squared differences by N to get the variance,
            // then take the square root to get the standard deviation.
            // A higher stdDev means sales figures are more spread out.
            double stdDev = (games > 0) ? Math.sqrt(sumSquaredDiffs / games) : 0;


            // ── Math.round() ──────────────────────────────────────
            // Round the mean to exactly 2 decimal places.
            // Multiplying by 100, rounding, then dividing by 100 is the
            // standard Java trick since Math.round() only rounds to integers.
            double avgRounded = Math.round(mean * 100.0) / 100.0;


            // ── Sort records by total sales (descending) for Top 10 ──
            // We make a copy first so the original 'records' list is unchanged.
            List<String[]> sorted = new ArrayList<>(records);
            sorted.sort((a, b) -> Double.compare(
                safeDouble(b, COL_TOTAL_SALES),   // b first = descending order
                safeDouble(a, COL_TOTAL_SALES)
            ));


            // ── Assign all computed values to the final fields ────
            // These fields are 'final', meaning they cannot be changed
            // once set here. This makes the Analytics object safe to read
            // from multiple places without risk of accidental modification.
            this.totalGames       = games;
            this.totalSales       = ts;
            this.naSales          = na;
            this.jpSales          = jp;
            this.palSales         = pal;
            this.otherSales       = ot;
            this.avgCriticScore   = (cCount > 0) ? totalCritic / cCount : 0;
            this.criticCount      = cCount;

            // Guard against no data: if no game had sales > 0, return 0
            this.highestSale      = (maxSale == Double.MIN_VALUE) ? 0 : maxSale;
            this.lowestSale       = (minSale == Double.MAX_VALUE) ? 0 : minSale;
            this.highestGame      = maxGame;
            this.lowestGame       = minGame;

            this.avgSalesPerGame  = avgRounded;
            this.salesStdDev      = stdDev;
            this.salesByGenre     = byGenre;
            this.salesByConsole   = byConsole;
            this.salesByPublisher = byPublisher;

            // Math.min(10, sorted.size()) ensures we don't crash if
            // the dataset has fewer than 10 games
            this.top10Games = sorted.subList(0, Math.min(10, sorted.size()));
        }


        // ── HELPER: Safe numeric parser ───────────────────────────
        // Attempts to parse a cell value as a double.
        // Returns 0 if the column index is out of range, the cell is
        // empty, or the value isn't a valid number.
        // This prevents crashes from missing or malformed data.
        static double safeDouble(String[] row, int col) {
            try {
                return (col < row.length && !row[col].isEmpty())
                    ? Double.parseDouble(row[col])
                    : 0;
            } catch (NumberFormatException e) {
                return 0; // Not a number — treat as 0
            }
        }

        // ── HELPER: Safe string column reader ─────────────────────
        // Returns the cell value at 'col', trimmed of whitespace.
        // If the column is out of range or the cell is blank,
        // returns the provided 'fallback' string instead (e.g. "Unknown").
        static String safeCol(String[] row, int col, String fallback) {
            if (col >= row.length) return fallback;  // Column doesn't exist
            String v = row[col].trim();
            return v.isEmpty() ? fallback : v;        // Empty cell → use fallback
        }

    } // ── End of Analytics inner class ─────────────────────────────


    // ════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR — ExportAnalyticsReport()
    //  Called when the program starts. Sets up the window properties
    //  and makes it visible on screen.
    // ════════════════════════════════════════════════════════════════
    public ExportAnalyticsReport() {
        // Set the title bar text of the window
        super("VGChartz Analytics Report  —  Villaflores");

        // Build all the visual components
        initUI();

        // Close the program entirely when the window's X button is clicked
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set initial window size (width x height in pixels)
        setSize(900, 760);

        // Prevent the window from being resized smaller than this
        setMinimumSize(new Dimension(750, 580));

        // Center the window on the user's screen
        setLocationRelativeTo(null);

        // Make the window visible (it's hidden until this is called)
        setVisible(true);
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: initUI()
    //  Assembles the three main sections of the window:
    //    NORTH  — blue header banner with title and subtitle
    //    CENTER — step-by-step controls + results text area
    //    SOUTH  — status bar showing the current program state
    // ════════════════════════════════════════════════════════════════
    private void initUI() {
        // BorderLayout divides the window into NORTH, CENTER, SOUTH (and EAST/WEST)
        setLayout(new BorderLayout(0, 0));

        // Set the window background color
        getContentPane().setBackground(BG);

        // Add each section to its region
        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: buildHeaderPanel()
    //  Creates the blue banner at the top of the window containing
    //  the app title and a brief instruction subtitle.
    // ════════════════════════════════════════════════════════════════
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLUE);
        header.setBorder(new EmptyBorder(14, 18, 10, 18)); // padding: top, left, bottom, right

        // Main title label
        JLabel title = new JLabel("  VGChartz Analytics Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        // Subtitle with user instructions
        JLabel subtitle = new JLabel("  Follow the 3 steps below to load your data and export the summary report.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(200, 225, 255)); // Light blue tint

        // Inner panel to stack title above subtitle
        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false); // Transparent so the blue background shows through
        text.add(title,    BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.SOUTH);
        header.add(text, BorderLayout.CENTER);

        return header;
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: buildCenterPanel()
    //  Creates the main body of the window:
    //    - Top: the step-by-step controls (file input + buttons)
    //    - Center: the scrollable results text area
    // ════════════════════════════════════════════════════════════════
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(12, 14, 6, 14));

        // Add the step controls at the top
        center.add(buildStepsPanel(), BorderLayout.NORTH);

        // ── Results Text Area ─────────────────────────────────────
        // This is where the analytics output is displayed after loading.
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13)); // Monospace font for aligned columns
        resultArea.setEditable(false);                             // User cannot type into this area
        resultArea.setBackground(new Color(250, 252, 255));
        resultArea.setForeground(new Color(30, 30, 50));
        resultArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Default welcome message shown before any file is loaded
        resultArea.setText(
            "  Welcome!\n\n" +
            "  This tool reads a video game sales CSV file and generates an analytics summary.\n\n" +
            "  HOW TO USE:\n" +
            "  ─────────────────────────────────────────────────────────────\n" +
            "  Step 1  Click \"Browse\" to find your CSV file on your computer.\n\n" +
            "  Step 2  Click \"Load & Analyze\" to process the data.\n\n" +
            "  Step 3  Click \"Export to CSV\" to save the report.\n" +
            "  ─────────────────────────────────────────────────────────────\n\n" +
            "  Your results will appear here once the file is loaded."
        );

        // Wrap resultArea in a scroll pane so users can scroll through long output
        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(new LineBorder(new Color(190, 210, 235), 1));

        // Label above the results area
        JLabel resultsLabel = new JLabel("  Analysis Results:");
        resultsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultsLabel.setBorder(new EmptyBorder(4, 2, 4, 0));

        // Wrapper panel for the label + scroll pane
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setBackground(BG);
        wrapper.add(resultsLabel, BorderLayout.NORTH);
        wrapper.add(scroll,       BorderLayout.CENTER);

        center.add(wrapper, BorderLayout.CENTER);
        return center;
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: buildStepsPanel()
    //  Creates the step-by-step control area with two columns:
    //    LEFT  — Step 1: file path input + Browse button
    //    RIGHT — Step 2 (Load) and Step 3 (Export) buttons
    //  Also registers all button click listeners here.
    // ════════════════════════════════════════════════════════════════
    private JPanel buildStepsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(190, 210, 235), 1),
            new EmptyBorder(14, 16, 14, 16)
        ));

        // ── LEFT COLUMN: Step 1 — File Selection ─────────────────
        JLabel step1Title = new JLabel("Step 1 — Select your CSV file:");
        step1Title.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel step1Hint = new JLabel("  Tip: Use the Browse button — no typing needed!");
        step1Hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        step1Hint.setForeground(new Color(80, 100, 150));

        // The file path text field — shows the selected path after browsing
        filePathField = new JTextField();
        filePathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filePathField.setBackground(Color.WHITE);
        filePathField.setBorder(new CompoundBorder(
            new LineBorder(new Color(180, 200, 230)),
            new EmptyBorder(5, 8, 5, 8)
        ));

        // Browse button — opens the OS file picker dialog
        browseButton = styledButton("Browse...", BLUE);

        // Arrange the text field and browse button side by side
        JPanel fileRow = new JPanel(new BorderLayout(6, 0));
        fileRow.setOpaque(false);
        fileRow.add(filePathField, BorderLayout.CENTER); // Stretches to fill space
        fileRow.add(browseButton,  BorderLayout.EAST);   // Fixed width on the right

        // Stack Step 1 label, file row, and hint vertically
        JPanel step1 = new JPanel(new BorderLayout(0, 5));
        step1.setOpaque(false);
        step1.add(step1Title, BorderLayout.NORTH);
        step1.add(fileRow,    BorderLayout.CENTER);
        step1.add(step1Hint,  BorderLayout.SOUTH);

        // ── RIGHT COLUMN: Steps 2 & 3 — Action Buttons ───────────
        JLabel step23Title = new JLabel("Step 2 — Analyze  |  Step 3 — Export:");
        step23Title.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Load & Analyze button — reads the CSV and runs analytics
        loadButton = styledButton("Load & Analyze", GREEN);

        // Export button — disabled until data is loaded successfully
        exportButton = styledButton("Export to CSV", ORANGE);
        exportButton.setEnabled(false); // Greyed out until load completes

        JLabel step23Hint = new JLabel("  Tip: Export button unlocks after loading.");
        step23Hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        step23Hint.setForeground(new Color(80, 100, 150));

        // Place both buttons side by side
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(loadButton);
        btnRow.add(exportButton);

        JPanel step23 = new JPanel(new BorderLayout(0, 5));
        step23.setOpaque(false);
        step23.add(step23Title, BorderLayout.NORTH);
        step23.add(btnRow,      BorderLayout.CENTER);
        step23.add(step23Hint,  BorderLayout.SOUTH);

        // GridLayout(1, 2) = one row, two equal columns
        JPanel columns = new JPanel(new GridLayout(1, 2, 20, 0));
        columns.setOpaque(false);
        columns.add(step1);
        columns.add(step23);
        panel.add(columns, BorderLayout.CENTER);

        // ── Register Button Listeners ─────────────────────────────
        // Lambda expressions: each button click calls the corresponding method.
        browseButton.addActionListener(e -> browseFile());
        loadButton.addActionListener(e   -> loadAndAnalyze());
        exportButton.addActionListener(e -> exportCSV());

        // Allow pressing Enter in the file path field to trigger load
        filePathField.addActionListener(e -> loadAndAnalyze());

        return panel;
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: buildStatusBar()
    //  Creates the thin bar at the bottom of the window.
    //  It shows the current status (e.g. "Ready", "Loading...", "Done!").
    //  Updated dynamically by setStatus().
    // ════════════════════════════════════════════════════════════════
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(220, 232, 248));
        bar.setBorder(new EmptyBorder(5, 14, 5, 14));

        statusLabel = new JLabel("Ready — please select a CSV file to get started.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(60, 80, 120));
        bar.add(statusLabel, BorderLayout.WEST);

        return bar;
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: styledButton(text, bg)
    //  A reusable helper that creates a consistently styled button.
    //  Also adds a hover effect: the button darkens when the mouse
    //  enters and restores its original color when the mouse leaves.
    // ════════════════════════════════════════════════════════════════
    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);   // White text on colored background
        btn.setFocusPainted(false);       // Remove the dotted focus rectangle
        btn.setOpaque(true);              // Needed for background color to show on all platforms
        btn.setBorderPainted(false);      // Remove the default button border
        btn.setBorder(new EmptyBorder(8, 18, 8, 18)); // Padding inside the button
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor on hover

        // Precompute the darker hover color
        Color hover = bg.darker();

        // MouseAdapter lets us override only the events we care about
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Only apply hover if the button is currently enabled
                if (btn.isEnabled()) btn.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // Restore original color when the mouse leaves
                btn.setBackground(bg);
            }
        });

        return btn;
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: browseFile()
    //  Opens the operating system's native file chooser dialog.
    //  Filters to only show CSV files so users aren't overwhelmed.
    //  When a file is selected, its path is placed in filePathField.
    // ════════════════════════════════════════════════════════════════
    private void browseFile() {
        JFileChooser chooser = new JFileChooser();

        // Only show CSV files in the picker
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        chooser.setDialogTitle("Find and Select Your CSV Dataset File");
        chooser.setApproveButtonText("Use This File"); // Custom label for the confirm button

        // showOpenDialog returns APPROVE_OPTION if the user clicked "Use This File"
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Put the selected file's full path into the text field
            filePathField.setText(chooser.getSelectedFile().getAbsolutePath());

            // Update the status bar to guide the user to the next step
            setStatus(
                "File selected: " + chooser.getSelectedFile().getName() +
                "  —  Now click \"Load & Analyze\".",
                new Color(30, 100, 50)
            );
        }
        // If the user cancelled the dialog, nothing happens
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: loadAndAnalyze()
    //  Triggered when the user clicks "Load & Analyze".
    //  Validates the file path first (loops until valid),
    //  then loads the CSV in a background thread to keep the UI
    //  responsive, then calls displayResults() when done.
    // ════════════════════════════════════════════════════════════════
    private void loadAndAnalyze() {
        String path = filePathField.getText().trim();

        // ── Validation Loop 1: Empty path ─────────────────────────
        // Keep asking until the user provides a non-empty string.
        while (path.isEmpty()) {
            setStatus("No file selected yet.", new Color(160, 70, 0));
            path = JOptionPane.showInputDialog(
                this,
                "No file has been selected yet.\n" +
                "Please type or paste the full path to your CSV file:",
                "No File Selected",
                JOptionPane.WARNING_MESSAGE
            );
            if (path == null) return; // User clicked Cancel — exit the method
            path = path.trim();
            filePathField.setText(path); // Update the text field
        }

        // ── Validation Loop 2: File must actually exist ───────────
        // new File(path) does NOT open or read the file — it just creates
        // a reference. We then call .exists() and .isFile() to verify it.
        File file = new File(path);
        while (!file.exists() || !file.isFile()) {
            setStatus("File not found. Please check the path.", Color.RED.darker());
            JOptionPane.showMessageDialog(
                this,
                "We could not find a file at:\n  " + path +
                "\n\nPlease check the file path and try again.",
                "File Not Found",
                JOptionPane.ERROR_MESSAGE
            );
            // Ask again
            path = JOptionPane.showInputDialog(
                this,
                "Please enter the correct full file path:",
                "Try Again",
                JOptionPane.WARNING_MESSAGE
            );
            if (path == null) return;  // User clicked Cancel
            path = path.trim();
            filePathField.setText(path);
            file = new File(path);     // Re-create the File reference with the new path
        }

        // ── File is valid — begin loading ─────────────────────────
        setStatus("Loading your file, please wait...", BLUE);
        resultArea.setText("  Loading data from: " + file.getName() + "\n  This may take a moment...");

        // Keep a final reference for use inside the anonymous inner class
        final File finalFile = file;

        // ── SwingWorker — run CSV loading in a background thread ──
        // Swing is single-threaded. If we read the file on the main thread,
        // the window will freeze until loading finishes.
        // SwingWorker moves heavy work to a background thread while keeping
        // the UI responsive. doInBackground() runs off the main thread.
        // done() runs back on the main (Event Dispatch) thread when finished.
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loadCSV(finalFile); // Read file in background
                return null;
            }
            @Override
            protected void done() {
                // Back on the main thread — safe to update the UI now
                analytics = new Analytics(records); // Run all math calculations
                displayResults();                   // Render results to the text area
                exportButton.setEnabled(true);      // Unlock the Export button
                setStatus(
                    "Done! Loaded " + df0.format(records.size()) +
                    " records  —  Click \"Export to CSV\" to save the report.",
                    new Color(30, 120, 60)
                );
            }
        };
        worker.execute(); // Start the background task
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: loadCSV(file)
    //  Reads the CSV file line by line into the 'records' list.
    //  Each line is parsed into a String array where each element
    //  is one column value. The first line (headers) is skipped.
    //  Only rows with at least 12 columns are included to avoid
    //  incomplete or malformed rows from breaking the analytics.
    // ════════════════════════════════════════════════════════════════
    private void loadCSV(File file) {
        records.clear(); // Remove any data from a previously loaded file

        // BufferedReader reads the file line by line efficiently
        // The try-with-resources block automatically closes the reader when done
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true; // Flag to skip the header row

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;  // Skip the very first line (column headers)
                    continue;
                }
                // Parse the CSV line into an array of column values
                String[] cols = parseCsvLine(line);

                // Only add rows that have at least 12 columns (all required fields)
                if (cols.length >= 12) {
                    records.add(cols);
                }
            }

        } catch (IOException ex) {
            // If reading fails, show an error dialog on the main thread
            // invokeLater is needed because loadCSV runs on a background thread
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                    this,
                    "Error reading file:\n" + ex.getMessage(),
                    "File Read Error",
                    JOptionPane.ERROR_MESSAGE
                )
            );
        }
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: parseCsvLine(line)
    //  Splits a single CSV line into an array of field values.
    //  Handles the tricky case where a field value contains a comma
    //  (which is valid in CSV if the field is wrapped in quotes).
    //
    //  Example: `"Grand Theft Auto, V",PS4,Action`
    //  Should parse as: ["Grand Theft Auto, V", "PS4", "Action"]
    //  NOT:             ["Grand Theft Auto", " V\"", "PS4", "Action"]
    // ════════════════════════════════════════════════════════════════
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder(); // Builds the current field character by character
        boolean inQ = false;                    // Are we currently inside a quoted field?

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Toggle whether we are inside quotes
                // A quote character signals the start or end of a quoted field
                inQ = !inQ;
            } else if (c == ',' && !inQ) {
                // A comma outside of quotes = end of this field, start of next
                fields.add(sb.toString().trim());
                sb.setLength(0); // Reset the builder for the next field
            } else {
                // Any other character — add it to the current field
                sb.append(c);
            }
        }
        // Add the last field (there's no trailing comma after the final field)
        fields.add(sb.toString().trim());

        return fields.toArray(new String[0]);
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: displayResults()
    //  Builds a formatted text report from the Analytics object
    //  and puts it into the resultArea text box for the user to see.
    //  Uses String.format() to align values in neat columns.
    // ════════════════════════════════════════════════════════════════
    private void displayResults() {
        Analytics a = analytics; // Shorthand reference

        // Separator lines for visual structure in the text output
        String SEP  = "=".repeat(66); // Thick separator for major sections
        String sep2 = "-".repeat(66); // Thin separator for sub-sections

        StringBuilder sb = new StringBuilder();

        // ── Title Block ───────────────────────────────────────────
        sb.append(SEP).append("\n");
        sb.append("   VGChartz 2024  -  CSV Export Analytics Report\n");
        sb.append("   Prepared by: Villaflores\n");
        sb.append(SEP).append("\n\n");

        // ── Dataset Overview Section ──────────────────────────────
        sb.append("  DATASET OVERVIEW\n").append(sep2).append("\n");
        sb.append(String.format("  %-34s  %s games\n",       "Total Titles in Dataset:",  df0.format(a.totalGames)));
        sb.append(String.format("  %-34s  %s M units\n",     "Total Global Sales:",        df2.format(a.totalSales)));
        sb.append(String.format("  %-34s  %.2f out of 10\n", "Average Critic Score:",       a.avgCriticScore));
        sb.append(String.format("  %-34s  %s titles\n\n",    "Titles with a Score:",        df0.format(a.criticCount)));

        // ── Sales Statistics Section (Math functions output) ──────
        sb.append("  SALES STATISTICS  (Math functions)\n").append(sep2).append("\n");
        sb.append(String.format("  %-34s  %s M  [Math.max()]\n",              "Highest Single-Game Sales:", df2.format(a.highestSale)));
        sb.append(String.format("  %-34s  %s\n",                              "  -> Game:",                  shorten(a.highestGame, 45)));
        sb.append(String.format("  %-34s  %s M  [Math.min()]\n",              "Lowest Non-Zero Sale:",      df2.format(a.lowestSale)));
        sb.append(String.format("  %-34s  %s\n",                              "  -> Game:",                  shorten(a.lowestGame, 45)));
        sb.append(String.format("  %-34s  %s M  [Math.round()]\n",           "Average Sales per Game:",    df2.format(a.avgSalesPerGame)));
        sb.append(String.format("  %-34s  %s M  [Math.pow() + Math.sqrt()]\n\n", "Sales Std. Deviation:", df2.format(a.salesStdDev)));

        // ── Regional Sales Section ────────────────────────────────
        sb.append("  REGIONAL SALES BREAKDOWN\n").append(sep2).append("\n");
        sb.append(String.format("  %-34s  %s M\n",   "North America (NA):",  df2.format(a.naSales)));
        sb.append(String.format("  %-34s  %s M\n",   "Japan (JP):",          df2.format(a.jpSales)));
        sb.append(String.format("  %-34s  %s M\n",   "PAL - Europe / AUS:", df2.format(a.palSales)));
        sb.append(String.format("  %-34s  %s M\n\n", "Other Regions:",       df2.format(a.otherSales)));

        // ── Top 5 Rankings ────────────────────────────────────────
        sb.append("  TOP 5 GENRES BY TOTAL SALES\n").append(sep2).append("\n");
        sb.append(buildTopN(a.salesByGenre, 5));

        sb.append("  TOP 5 CONSOLES BY TOTAL SALES\n").append(sep2).append("\n");
        sb.append(buildTopN(a.salesByConsole, 5));

        sb.append("  TOP 5 PUBLISHERS BY TOTAL SALES\n").append(sep2).append("\n");
        sb.append(buildTopN(a.salesByPublisher, 5));

        // ── Top 10 Games ──────────────────────────────────────────
        sb.append("  TOP 10 BEST-SELLING GAMES\n").append(sep2).append("\n");
        for (int i = 0; i < a.top10Games.size(); i++) {
            String[] r = a.top10Games.get(i);
            sb.append(String.format("  %2d. %-44s  %-6s  %s M\n",
                i + 1,
                shorten(r[COL_TITLE], 44),     // Truncate long titles
                r[COL_CONSOLE],
                df2.format(Analytics.safeDouble(r, COL_TOTAL_SALES))
            ));
        }
        sb.append(SEP).append("\n");

        // Set the text and scroll to the top
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: buildTopN(map, n)
    //  Takes a map of { category → total sales } and returns a
    //  formatted string showing the top N entries ranked by sales.
    //  Uses a stream to sort entries descending and limit to N.
    // ════════════════════════════════════════════════════════════════
    private String buildTopN(Map<String, Double> map, int n) {
        StringBuilder sb = new StringBuilder();
        int[] rank = {1}; // Array trick to allow mutation inside a lambda

        map.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Sort descending by sales
            .limit(n)                                                       // Take only top N
            .forEach(e -> sb.append(String.format("  %2d. %-32s  %s M\n",
                rank[0]++,                       // Increment rank after each entry
                shorten(e.getKey(), 32),          // Truncate long category names
                df2.format(e.getValue())          // Format sales value
            )));

        sb.append("\n"); // Blank line after the list
        return sb.toString();
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: exportCSV()
    //  Triggered when the user clicks "Export to CSV".
    //  Opens a save dialog so the user can choose where to save.
    //  Writes the full analytics summary to a CSV file using FileWriter.
    //  The first line written is the header row: Section,Metric,Value
    // ════════════════════════════════════════════════════════════════
    private void exportCSV() {
        // Open a "Save As" dialog with "summary_report.csv" as the default filename
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("summary_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        chooser.setDialogTitle("Choose where to save your Summary Report");
        chooser.setApproveButtonText("Save Here");

        // If the user cancelled the dialog, exit the method
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File outFile = chooser.getSelectedFile();

        // Ensure the file has a .csv extension even if the user didn't type it
        if (!outFile.getName().endsWith(".csv")) {
            outFile = new File(outFile.getAbsolutePath() + ".csv");
        }

        Analytics a = analytics; // Shorthand reference to computed results

        // FileWriter writes text to a file character by character.
        // The try-with-resources block ensures it's always closed, even if an error occurs.
        try (FileWriter fw = new FileWriter(outFile)) {

            // ── Write the required CSV header row ─────────────────
            fw.write("Section,Metric,Value\n");

            // ── Dataset Overview rows ─────────────────────────────
            fw.write("Dataset Overview,Total Game Titles,"               + a.totalGames + "\n");
            fw.write("Dataset Overview,Total Global Sales (M),"          + String.format("%.2f", a.totalSales) + "\n");
            fw.write("Dataset Overview,Average Critic Score (out of 10),"+ String.format("%.2f", a.avgCriticScore) + "\n");
            fw.write("Dataset Overview,Titles With a Critic Score,"      + a.criticCount + "\n");

            // ── Sales Statistics rows (Math function results) ──────
            fw.write("Sales Statistics,Highest Single-Game Sales (M) [Math.max()],"    + String.format("%.2f", a.highestSale) + "\n");
            fw.write("Sales Statistics,Game with Highest Sales,"                        + csvEscape(a.highestGame) + "\n");
            fw.write("Sales Statistics,Lowest Non-Zero Sale (M) [Math.min()],"         + String.format("%.2f", a.lowestSale) + "\n");
            fw.write("Sales Statistics,Game with Lowest Sales,"                         + csvEscape(a.lowestGame) + "\n");
            fw.write("Sales Statistics,Average Sales per Game (M) [Math.round()],"     + String.format("%.2f", a.avgSalesPerGame) + "\n");
            fw.write("Sales Statistics,Sales Std Deviation (M) [Math.pow+Math.sqrt],"  + String.format("%.2f", a.salesStdDev) + "\n");

            // ── Regional Sales rows ───────────────────────────────
            fw.write("Regional Sales,North America (M),"     + String.format("%.2f", a.naSales)    + "\n");
            fw.write("Regional Sales,Japan (M),"             + String.format("%.2f", a.jpSales)    + "\n");
            fw.write("Regional Sales,PAL - Europe/AUS (M)," + String.format("%.2f", a.palSales)   + "\n");
            fw.write("Regional Sales,Other Regions (M),"     + String.format("%.2f", a.otherSales) + "\n");

            // ── Top 5 Rankings ────────────────────────────────────
            writeTopNCSV(fw, a.salesByGenre,     "Top 5 Genres",     5);
            writeTopNCSV(fw, a.salesByConsole,   "Top 5 Consoles",   5);
            writeTopNCSV(fw, a.salesByPublisher, "Top 5 Publishers", 5);

            // ── Top 10 Games rows ─────────────────────────────────
            for (int i = 0; i < a.top10Games.size(); i++) {
                String[] r = a.top10Games.get(i);
                fw.write(String.format("Top 10 Best-Selling Games,#%d - %s (%s),%.2f M\n",
                    i + 1,
                    csvEscape(r[COL_TITLE]),  // Escape commas in game titles
                    r[COL_CONSOLE],
                    Analytics.safeDouble(r, COL_TOTAL_SALES)
                ));
            }

            // ── Success message ───────────────────────────────────
            JOptionPane.showMessageDialog(
                this,
                "Your report was saved successfully!\n\nSaved to:\n  " + outFile.getAbsolutePath() +
                "\n\nYou can open this file in Microsoft Excel or Google Sheets.",
                "Report Exported!",
                JOptionPane.INFORMATION_MESSAGE
            );
            setStatus("Report saved to: " + outFile.getName(), new Color(30, 120, 60));

        } catch (IOException ex) {
            // Show error if the file couldn't be written (e.g., no write permission)
            JOptionPane.showMessageDialog(
                this,
                "Error saving file:\n" + ex.getMessage(),
                "Save Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: writeTopNCSV(fw, map, section, n)
    //  Helper for exportCSV(). Writes the top N entries from a map
    //  to the FileWriter as CSV rows in "Section,#N - Name,Value M" format.
    // ════════════════════════════════════════════════════════════════
    private void writeTopNCSV(FileWriter fw, Map<String, Double> map, String section, int n) throws IOException {
        int rank = 1;

        // Sort descending by value, take top N, write each as a CSV row
        for (Map.Entry<String, Double> e : map.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .toList()) {
            fw.write(String.format("%s,#%d - %s,%.2f M\n",
                section,
                rank++,
                csvEscape(e.getKey()), // Escape in case the name contains a comma
                e.getValue()
            ));
        }
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: shorten(s, max)
    //  Truncates a string to 'max' characters if it's too long,
    //  appending "..." to signal it was cut off.
    //  Used to keep long game/publisher names from breaking layouts.
    // ════════════════════════════════════════════════════════════════
    private String shorten(String s, int max) {
        return (s.length() <= max) ? s : s.substring(0, max - 1) + "...";
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: csvEscape(s)
    //  Wraps a string in double quotes if it contains a comma, quote,
    //  or newline — characters that would break CSV formatting.
    //  Also doubles any existing quotes (CSV standard for escaping).
    //  Example: Grand Theft Auto, V  →  "Grand Theft Auto, V"
    // ════════════════════════════════════════════════════════════════
    private String csvEscape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s; // No special characters — return as-is
    }


    // ════════════════════════════════════════════════════════════════
    //  METHOD: setStatus(msg, color)
    //  Updates the text and color of the status bar label.
    //  Called throughout the program to guide the user through steps.
    // ════════════════════════════════════════════════════════════════
    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }


    // ════════════════════════════════════════════════════════════════
    //  MAIN METHOD — Program Entry Point
    //  Java starts executing here when you run the program.
    //  Sets the Look and Feel to match the operating system's style,
    //  then creates the main window on the Event Dispatch Thread (EDT).
    //  Swing requires all UI creation to happen on the EDT.
    // ════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            // Makes the app look like a native Windows/Mac/Linux app
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // If it fails, Java's default look and feel is used instead
        }

        // invokeLater schedules window creation on the EDT (Event Dispatch Thread)
        // This is the correct and safe way to start a Swing application
        SwingUtilities.invokeLater(ExportAnalyticsReport::new);
    }

} // ── End of ExportAnalyticsReport class ───────────────────────────