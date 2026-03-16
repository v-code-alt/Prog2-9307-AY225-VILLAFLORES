import java.io.*;
import java.util.*;

/*
    MP06 - Display Unique Values in a Column
    This program reads a CSV dataset and displays all unique (non-duplicate)
    values found in a user-specified column. It helps identify distinct
    categories or entries within a particular field of the dataset.
    
    Author: Villaflores, Joellen T.
    Course: Programming 2 Laboratory
    School: University of Perpetual Help System DALTA Molino Campus
 */

public class MP06 {

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
        String[] headers = null;                    // stores column names from header row

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

        // Step 3: Validate the file had content
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

        // Step 4: Display available columns for user selection
        System.out.println("\nAvailable columns:");
        for (int i = 0; i < headers.length; i++) {
            System.out.printf("  [%d] %s%n", i, headers[i].trim());
        }

        // Step 5: Prompt the user to choose a column by index
        System.out.print("\nEnter column index to display unique values: ");
        int columnIndex;

        try {
            columnIndex = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid integer for the column index.");
            scanner.close();
            return;
        }

        // Step 6: Validate column index is within range
        if (columnIndex < 0 || columnIndex >= headers.length) {
            System.out.println("Error: Column index out of range (0 to " + (headers.length - 1) + ").");
            scanner.close();
            return;
        }

        // Step 7: Collect unique values using LinkedHashSet (preserves insertion order)
        Set<String> uniqueValues = new LinkedHashSet<>();

        for (String[] record : records) {
            if (columnIndex < record.length) {
                String value = record[columnIndex].trim();
                if (!value.isEmpty()) {
                    uniqueValues.add(value); // duplicates are automatically ignored by Set
                }
            }
        }

        // Step 8: Display all unique values
        System.out.println("\n========================================");
        System.out.println("  Unique Values in Column: " + headers[columnIndex].trim());
        System.out.println("========================================");

        if (uniqueValues.isEmpty()) {
            System.out.println("  (No values found in this column)");
        } else {
            int count = 1;
            for (String value : uniqueValues) {
                System.out.printf("  %d. %s%n", count++, value);
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("  Total unique values: " + uniqueValues.size());
        System.out.println("========================================");

        scanner.close();
    }
}