import java.io.*;
import java.util.*;

/*
    MP07 - Sort Records Alphabetically by a Column
    
    This program reads a CSV dataset and sorts all records alphabetically
    based on the values in a user-specified column. The sorted results
    are displayed in a formatted table output.

    Author: Villaflores, Joellen T.
    Course: Programming 2 Laboratory
    School: University of Perpetual Help System DALTA Molino Campus
 */

public class MP07 {

    /**
     * Parses a CSV line properly handling quoted fields that may contain commas
     */
    static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }

        // Add the last field
        fields.add(field.toString().trim());
        return fields;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Step 1: Ask the user for the dataset file path
        System.out.print("Enter the dataset file path: ");
        String filePath = scanner.nextLine().trim();

        // Step 2: Read the CSV file and parse its contents
        List<String[]> records = new ArrayList<>(); // holds all data rows
        String[] headers = null;                    // holds the header row (column names)

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            List<String[]> allRows = new ArrayList<>();

            // First pass: read all non-empty rows using proper CSV parsing
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> fields = parseCSVLine(line);
                allRows.add(fields.toArray(new String[0]));
            }

            if (allRows.isEmpty()) {
                System.out.println("Error: The file appears to be empty.");
                scanner.close();
                return;
            }

            // Find the header row: look for the first row with substantial content
            // (at least 30% of its fields are non-empty)
            int headerRowIndex = 0;

            for (int i = 0; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                int nonEmptyCount = 0;
                for (String field : row) {
                    if (!field.trim().isEmpty()) {
                        nonEmptyCount++;
                    }
                }
                
                // First row with at least 30% filled fields is likely the header
                int filledPercentage = row.length > 0 ? (nonEmptyCount * 100) / row.length : 0;
                
                if (filledPercentage >= 30) {
                    headerRowIndex = i;
                    break; // Take the FIRST matching row, not the last
                }
            }

            headers = allRows.get(headerRowIndex);    // Found header row
            records = new ArrayList<>(allRows.subList(headerRowIndex + 1, allRows.size())); // All rows after header

        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found at path: " + filePath);
            scanner.close();
            return;
        } catch (IOException e) {
            System.out.println("Error: Could not read the file. " + e.getMessage());
            scanner.close();
            return;
        }

        // Step 3: Check if the file has valid content
        if (headers == null || headers.length == 0) {
            System.out.println("Error: The file appears to be empty or has no headers.");
            scanner.close();
            return;
        }

        if (records.isEmpty()) {
            System.out.println("No data records found in the file.");
            scanner.close();
            return;
        }

        // Step 4: Display available columns for sorting selection
        System.out.println("\nAvailable columns:");
        for (int i = 0; i < headers.length; i++) {
            System.out.printf("  [%d] %s%n", i, headers[i].trim());
        }

        // Step 5: Ask the user which column to sort by
        System.out.print("\nEnter column index to sort by: ");
        int columnIndex;

        try {
            columnIndex = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid integer for the column index.");
            scanner.close();
            return;
        }

        // Step 6: Validate the column index
        if (columnIndex < 0 || columnIndex >= headers.length) {
            System.out.println("Error: Column index out of range (0 to " + (headers.length - 1) + ").");
            scanner.close();
            return;
        }

        // Step 7: Sort the records alphabetically using a Comparator
        // Comparator compares two records based on the chosen column value (case-insensitive)
        final int sortColumn = columnIndex; // must be effectively final for lambda use
        records.sort((row1, row2) -> {
            // Get the column value from each row (safely handle short rows)
            String val1 = (sortColumn < row1.length) ? row1[sortColumn].trim() : "";
            String val2 = (sortColumn < row2.length) ? row2[sortColumn].trim() : "";
            // Compare ignoring case for alphabetical ordering
            return val1.compareToIgnoreCase(val2);
        });

        // Step 8: Calculate column widths for formatted table output
        // Start with the width of the header name for each column
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].trim().length();
        }

        // Expand column width if any data value is wider
        for (String[] record : records) {
            for (int i = 0; i < headers.length && i < record.length; i++) {
                colWidths[i] = Math.max(colWidths[i], record[i].trim().length());
            }
        }

        // Step 9: Display sorted records in a formatted table
        System.out.println("\n========================================");
        System.out.println("  Records Sorted by: " + headers[columnIndex].trim() + " (A-Z)");
        System.out.println("========================================\n");

        // Print header row
        StringBuilder headerRow = new StringBuilder("  ");
        for (int i = 0; i < headers.length; i++) {
            headerRow.append(String.format("%-" + (colWidths[i] + 2) + "s", headers[i].trim()));
        }
        System.out.println(headerRow);
        System.out.println("  " + "-".repeat(headerRow.length() - 2));

        // Print each sorted data row
        for (String[] record : records) {
            StringBuilder row = new StringBuilder("  ");
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < record.length) ? record[i].trim() : "";
                row.append(String.format("%-" + (colWidths[i] + 2) + "s", cell));
            }
            System.out.println(row);
        }

        System.out.println("\n----------------------------------------");
        System.out.println("  Total records sorted: " + records.size());
        System.out.println("========================================");

        scanner.close();
    }
}