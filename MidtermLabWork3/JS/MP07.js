/*
    MP07 - Sort Records Alphabetically by a Column
    
    This program reads a CSV dataset and sorts all records alphabetically
    based on the values in a user-specified column. The sorted results
    are displayed in a formatted table output.
    
    Author: Villaflores, Joellen T.
    Course: Programming 2 Laboratory
    School: University of Perpetual Help System DALTA Molino Campus
    
    Run with: node MP07.js
 */

// Import required Node.js modules
const fs = require("fs");           // File System module for reading files
const readline = require("readline"); // Module for reading user input from terminal

// Create readline interface for receiving input from the user
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
});

/**
 * parseCSV - Parses raw CSV text content into a 2D array of strings,
 * properly handling quoted fields that may contain commas.
 *
 * @param {string} content - The raw text content from the CSV file
 * @returns {string[][]} - Array of rows; each row is an array of trimmed field values
 */
function parseCSV(content) {
    return content
        .split("\n")
        .filter(line => line.trim() !== "") // skip empty lines
        .map(line => parseCSVLine(line));
}

/**
 * parseCSVLine - Parses a single CSV line, respecting quoted fields.
 *
 * @param {string} line - A single CSV line
 * @returns {string[]} - Array of trimmed field values
 */
function parseCSVLine(line) {
    const fields = [];
    let field = "";
    let inQuotes = false;

    for (let i = 0; i < line.length; i++) {
        const char = line[i];

        if (char === '"') {
            inQuotes = !inQuotes;
        } else if (char === "," && !inQuotes) {
            fields.push(field.trim());
            field = "";
        } else {
            field += char;
        }
    }

    // Add the last field
    fields.push(field.trim());
    return fields;
}

/**
 * sortRecords - Sorts records alphabetically based on a specified column.
 *
 * @param {string[][]} records - The data rows to sort
 * @param {number} colIndex - The column index to sort by
 * @returns {string[][]} - Sorted array of records
 */
function sortRecords(records, colIndex) {
    // Create a shallow copy to avoid modifying the original
    const sorted = [...records];
    
    sorted.sort((a, b) => {
        const aValue = colIndex < a.length ? a[colIndex].toLowerCase() : "";
        const bValue = colIndex < b.length ? b[colIndex].toLowerCase() : "";
        return aValue.localeCompare(bValue);
    });

    return sorted;
}

/**
 * printTable - Displays records in a fixed-width aligned table format.
 *
 * @param {string[]} headers - The column header names
 * @param {string[][]} rows - The data rows to display
 */
function printTable(headers, rows) {
    // Determine the widest value in each column (header or data)
    const colWidths = headers.map((h, i) => {
        const maxDataWidth = rows.reduce((max, row) => {
            const len = (i < row.length ? row[i] : "").length;
            return Math.max(max, len);
        }, 0);
        return Math.max(h.length, maxDataWidth);
    });

    // Pad a string to fixed width with trailing spaces
    const padCell = (text, width) => text.padEnd(width + 2);

    // Print header row
    const headerLine = "  " + headers.map((h, i) => padCell(h, colWidths[i])).join("");
    console.log(headerLine);
    console.log("  " + "-".repeat(headerLine.length - 2));

    // Print each data row
    for (const row of rows) {
        const rowLine = "  " + headers.map((_, i) => {
            const cell = i < row.length ? row[i] : "";
            return padCell(cell, colWidths[i]);
        }).join("");
        console.log(rowLine);
    }
}

// Step 1: Ask user for the CSV file path
rl.question("Enter the dataset file path: ", (filePath) => {

    // Step 2: Read the CSV file from disk
    let content;
    try {
        content = fs.readFileSync(filePath.trim(), "utf8");
    } catch (err) {
        console.log("Error: Could not read file. " + err.message);
        rl.close();
        return;
    }

    // Step 3: Parse CSV into rows
    const allRows = parseCSV(content);

    if (allRows.length === 0) {
        console.log("Error: The file appears to be empty.");
        rl.close();
        return;
    }

    // Step 4: Find the header row intelligently
    // Look for the first row with substantial content (at least 30% non-empty fields)
    let headerRowIndex = 0;

    for (let i = 0; i < allRows.length; i++) {
        const row = allRows[i];
        const nonEmptyCount = row.filter(field => field.trim() !== "").length;
        const filledPercentage = row.length > 0 ? (nonEmptyCount * 100) / row.length : 0;

        // First row with at least 30% filled fields is likely the header
        if (filledPercentage >= 30) {
            headerRowIndex = i;
            break;
        }
    }

    const headers = allRows[headerRowIndex];         // found header row
    const records = allRows.slice(headerRowIndex + 1);   // all rows after header

    if (records.length === 0) {
        console.log("No data records found in the file.");
        rl.close();
        return;
    }

    // Step 5: Display available columns for selection
    console.log("\nAvailable columns:");
    headers.forEach((header, index) => {
        console.log(`  [${index}] ${header}`);
    });

    // Step 6: Ask which column to sort by
    rl.question("\nEnter column index to sort by: ", (colInput) => {

        // Step 7: Validate the column index
        const columnIndex = parseInt(colInput.trim(), 10);

        if (isNaN(columnIndex) || columnIndex < 0 || columnIndex >= headers.length) {
            console.log(`Error: Invalid column index. Must be between 0 and ${headers.length - 1}.`);
            rl.close();
            return;
        }

        // Step 8: Sort records by the chosen column
        const sortedRecords = sortRecords(records, columnIndex);

        // Step 9: Display sorted results
        console.log("\n========================================");
        console.log(`  Sorted by Column: ${headers[columnIndex]}`);
        console.log("========================================");
        console.log();
        printTable(headers, sortedRecords);

        console.log("\n----------------------------------------");
        console.log(`  Total records: ${sortedRecords.length}`);
        console.log("========================================");

        rl.close();
    });
});