import java.io.*;
import java.util.*;

/*
    MP08 - Filter Records Using a Keyword
    
    This program reads a CSV dataset and filters rows that contain a
    user-specified keyword in a selected column. Only matching records
    are displayed, allowing targeted data retrieval from the dataset.
    
    Author: Villaflores, Joellen T.
    Course: Programming 2 Laboratory
    School: University of Perpetual Help System DALTA Molino Campus
 */
public class MP08 {

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

        // Step 1: Ask the user for the CSV file path
        System.out.print("Enter the dataset file path: ");
        String filePath = scanner.nextLine().trim();

        // Step 2: Read and parse the CSV file
        List<String[]> records = new ArrayList<>(); // stores all data rows
        String[] headers = null;                    // stores the header row (column names)

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

        // Step 3: Validate file content
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

        // Step 4: Show available columns for the user to choose from
        System.out.println("\nAvailable columns:");
        for (int i = 0; i < headers.length; i++) {
            System.out.printf("  [%d] %s%n", i, headers[i].trim());
        }

        // Step 5: Ask which column to apply the filter on
        System.out.print("\nEnter column index to filter by: ");
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

        // Step 7: Ask the user for the keyword to filter with
        System.out.print("Enter the keyword to filter by: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Error: Keyword cannot be empty.");
            scanner.close();
            return;
        }

        // Step 8: Filter records - collect rows where the selected column contains the keyword
        // Using case-insensitive matching so "apple", "Apple", "APPLE" all match
        List<String[]> filteredRecords = new ArrayList<>();

        for (String[] record : records) {
            if (columnIndex < record.length) {
                String cellValue = record[columnIndex].trim();
                // Check if this cell value contains the keyword (case-insensitive)
                if (cellValue.toLowerCase().contains(keyword.toLowerCase())) {
                    filteredRecords.add(record);
                }
            }
        }

        // Step 9: Display the filtered results in a formatted table
        System.out.println("\n========================================");
        System.out.println("  Filter: \"" + keyword + "\" in column: " + headers[columnIndex].trim());
        System.out.println("========================================");

        if (filteredRecords.isEmpty()) {
            System.out.println("  No records matched the keyword: \"" + keyword + "\"");
        } else {

            // Calculate column widths for aligned output
            int[] colWidths = new int[headers.length];
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = headers[i].trim().length();
            }
            for (String[] record : filteredRecords) {
                for (int i = 0; i < headers.length && i < record.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], record[i].trim().length());
                }
            }

            // Print header row
            System.out.println();
            StringBuilder headerRow = new StringBuilder("  ");
            for (int i = 0; i < headers.length; i++) {
                headerRow.append(String.format("%-" + (colWidths[i] + 2) + "s", headers[i].trim()));
            }
            System.out.println(headerRow);
            System.out.println("  " + "-".repeat(headerRow.length() - 2));

            // Print each matching record
            for (String[] record : filteredRecords) {
                StringBuilder row = new StringBuilder("  ");
                for (int i = 0; i < headers.length; i++) {
                    String cell = (i < record.length) ? record[i].trim() : "";
                    row.append(String.format("%-" + (colWidths[i] + 2) + "s", cell));
                }
                System.out.println(row);
            }
        }

        System.out.println("\n----------------------------------------");
        System.out.println("  Total matching records: " + filteredRecords.size() + " / " + records.size());
        System.out.println("========================================");

        scanner.close();
    }
}