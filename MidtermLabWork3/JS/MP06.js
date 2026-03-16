/*
    MP06 - Display Unique Values in a Column
    
    This program reads a CSV dataset and displays all unique (non-duplicate)
    values found in a user-specified column. It helps identify distinct
    categories or entries within a particular field of the dataset.
    
    Author: Villaflores, Joellen T.
    Course: Programming 2 Laboratory
    School: University of Perpetual Help System DALTA Molino Campus
   
    Run with: node MP06.js
 */

// Import required Node.js modules
const fs = require("fs");          // File System module for reading files
const readline = require("readline"); // Module for reading user input from terminal

// Create an interface for reading input from the command line
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
});

/**
 * parseCSV - Parses raw CSV text into an array of string arrays,
 * properly handling quoted fields that may contain commas.
 *
 * @param {string} content - The raw text content of the CSV file
 * @returns {string[][]} - Array of rows, each row is an array of field strings
 */
function parseCSV(content) {
    return content
        .split("\n")
        .filter(line => line.trim() !== "")
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

    fields.push(field.trim());
    return fields;
}

/**
 * getUniqueValues - Extracts unique values from a specific column in the dataset.
 *
 * @param {string[][]} records - Array of data rows (excluding header)
 * @param {number} colIndex - The index of the column to extract values from
 * @returns {string[]} - Array of unique non-empty values
 */
function getUniqueValues(records, colIndex) {
    // Use a Set to automatically eliminate duplicates
    const uniqueSet = new Set();

    for (const record of records) {
        // Only add the value if the column exists in this row and is not empty
        if (colIndex < record.length && record[colIndex] !== "") {
            uniqueSet.add(record[colIndex]);
        }
    }

    // Convert Set back to Array for easy display
    return Array.from(uniqueSet);
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

    // Step 3: Parse CSV content into rows
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

        if (filledPercentage >= 30) {
            headerRowIndex = i;
            break;
        }
    }

    const headers = allRows[headerRowIndex];
    const records = allRows.slice(headerRowIndex + 1);

    if (records.length === 0) {
        console.log("No data records found in the file.");
        rl.close();
        return;
    }

    // Step 5: Display available columns for the user to choose from
    console.log("\nAvailable columns:");
    headers.forEach((header, index) => {
        console.log(`  [${index}] ${header}`);
    });

    // Step 6: Ask user to enter a column index
    rl.question("\nEnter column index to display unique values: ", (input) => {

        // Step 7: Parse and validate the column index
        const columnIndex = parseInt(input.trim(), 10);

        if (isNaN(columnIndex) || columnIndex < 0 || columnIndex >= headers.length) {
            console.log(`Error: Invalid column index. Must be between 0 and ${headers.length - 1}.`);
            rl.close();
            return;
        }

        // Step 8: Get all unique values from the chosen column
        const uniqueValues = getUniqueValues(records, columnIndex);

        // Step 9: Display results
        console.log("\n========================================");
        console.log(`  Unique Values in Column: ${headers[columnIndex]}`);
        console.log("========================================");

        if (uniqueValues.length === 0) {
            console.log("  (No values found in this column)");
        } else {
            uniqueValues.forEach((value, index) => {
                console.log(`  ${index + 1}. ${value}`);
            });
        }

        console.log("----------------------------------------");
        console.log(`  Total unique values: ${uniqueValues.length}`);
        console.log("========================================");

        rl.close();
    });
});