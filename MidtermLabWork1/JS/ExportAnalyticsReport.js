// ================================================================
//  FILE:    villaflores_analytics.js
//  AUTHOR:  Villaflores
//  RUNTIME: Node.js  (install from https://nodejs.org)
//
//  PURPOSE:
//    Reads a VGChartz CSV dataset from a user-provided file path,
//    performs sales analytics, prints results to the terminal,
//    and exports a summary_report.csv file.
//
//  HOW TO RUN:
//    node villaflores_analytics.js
//
//  REQUIREMENTS MET:
//    - readline used for user input (as specified)
//    - fs.existsSync() used for file validation (as specified)
//    - Loops until a valid file path is entered
//    - Loads dataset into memory (JavaScript array)
//    - Performs analytics and displays formatted results
//    - Exports summary_report.csv using fs.writeFileSync()
//
//  MATH FUNCTIONS USED (all inside analyzeData()):
//    Math.max()   — finds the highest single-game sales figure
//    Math.min()   — finds the lowest non-zero single-game sales
//    Math.round() — rounds the average sales to 2 decimal places
//    Math.pow()   — squares each difference from the mean
//    Math.sqrt()  — converts variance into standard deviation
// ================================================================

// ── STRICT MODE ───────────────────────────────────────────────────
// 'use strict' enables stricter JavaScript parsing and error handling.
// It prevents the use of undeclared variables and other bad practices.
'use strict';

// ── BUILT-IN NODE.JS MODULE IMPORTS ──────────────────────────────
// These modules come with Node.js — no installation needed.
// 'require()' is how Node.js loads modules (similar to 'import' in Java).

const fs       = require('fs');       // File System: read/write files, check if files exist
const path     = require('path');     // Path utilities: resolve absolute paths, join paths
const readline = require('readline'); // Readline: read input from the terminal line by line


// ── CSV COLUMN INDEX CONSTANTS ────────────────────────────────────
// The CSV has many columns. These constants map each column name
// to its 0-based index so we don't use "magic numbers" in the code.
// Example: row[COL_TITLE] gives the game title for that row.

const COL_TITLE       = 1;   // Game title
const COL_CONSOLE     = 2;   // Console/platform (PS4, X360, etc.)
const COL_GENRE       = 3;   // Genre (Action, Sports, etc.)
const COL_PUBLISHER   = 4;   // Publisher name
const COL_CRITIC      = 6;   // Critic score (0–10)
const COL_TOTAL_SALES = 7;   // Total global sales in millions
const COL_NA_SALES    = 8;   // North America sales in millions
const COL_JP_SALES    = 9;   // Japan sales in millions
const COL_PAL_SALES   = 10;  // PAL region (Europe/Australia) in millions
const COL_OTHER_SALES = 11;  // Other regions' sales in millions


// ── READLINE INTERFACE SETUP ──────────────────────────────────────
// readline.createInterface() connects to the terminal's input and output.
// - 'process.stdin'  = the terminal's keyboard input stream
// - 'process.stdout' = the terminal's text output stream
// This 'rl' object is used to ask questions and read the user's answers.

const rl = readline.createInterface({
    input : process.stdin,
    output: process.stdout
});

// ── HELPER: ask(question) ─────────────────────────────────────────
// Wraps rl.question() in a Promise so we can use 'await' with it.
// Without this, we'd have to nest callbacks, which makes loops hard to write.
//
// How it works:
//   - Returns a Promise that "resolves" (completes) when the user presses Enter
//   - The resolved value is the string the user typed
//   - 'await ask(...)' pauses execution until the user replies

function ask(question) {
    return new Promise(resolve => rl.question(question, resolve));
}


// ════════════════════════════════════════════════════════════════
//  STEP 1 — getValidFilePath()
//  Asks the user to enter the path to the CSV file.
//  Keeps asking in a loop until a valid file path is provided.
//  This fulfills the requirement: "loop until valid path".
// ════════════════════════════════════════════════════════════════
async function getValidFilePath() {
    console.log('');
    console.log('  STEP 1 — Tell us where your CSV file is located');
    console.log('  ------------------------------------------------');
    console.log('  Tip: Right-click the file and choose "Copy as path" (Windows),');
    console.log('       then paste it here.');
    console.log('');

    // 'while (true)' creates an infinite loop.
    // It only exits when we hit a 'return' statement (valid file found).
    while (true) {
        // Ask the user for the file path and wait for their answer
        // .trim() removes any leading/trailing whitespace
        // .replace() removes surrounding quotes that Windows adds when copying a path
        let inputPath = (await ask('  Enter the file path here: '))
            .trim()
            .replace(/^["']|["']$/g, ''); // Regex: remove leading/trailing ' or "

        // Check if the user entered nothing at all
        if (!inputPath) {
            console.log('');
            console.log('  Oops! You did not enter anything. Please try again.');
            console.log('');
            continue; // Skip the rest of the loop and ask again
        }

        // ── fs.existsSync() validation ────────────────────────────
        // fs.existsSync() returns true if something exists at that path.
        // fs.statSync().isFile() confirms it's a file (not a folder).
        // Both must be true for the path to be valid.
        if (fs.existsSync(inputPath) && fs.statSync(inputPath).isFile()) {
            console.log('');
            console.log('  Great! File found: ' + path.resolve(inputPath));
            return inputPath; // Valid file found — exit the loop and return the path
        } else {
            // File wasn't found — show a helpful message and loop back to ask again
            console.log('');
            console.log('  We could not find a file at that location. Please check:');
            console.log('    - The file path is correct');
            console.log('    - The file name includes ".csv" at the end');
            console.log('    - The file has not been moved or deleted');
            console.log('');
            // Loop continues — asks again from the top of 'while (true)'
        }
    }
}


// ════════════════════════════════════════════════════════════════
//  STEP 2 — loadCSV(filePath)
//  Reads the CSV file from disk and loads it into memory as an array.
//  Each element of the returned array is one game row (a string array).
//  The header row (row 0) is skipped — only data rows are included.
// ════════════════════════════════════════════════════════════════
function loadCSV(filePath) {
    console.log('');
    console.log('  STEP 2 — Loading and analyzing your data...');
    console.log('  --------------------------------------------');

    // fs.readFileSync() reads the entire file as a single string.
    // 'utf8' tells Node.js to decode the bytes as text (not binary).
    const raw = fs.readFileSync(filePath, 'utf8');

    // Split the text into individual lines.
    // /\r?\n/ handles both Windows line endings (\r\n) and Unix (\n).
    const lines = raw.split(/\r?\n/);

    const records = []; // This array will hold all parsed game rows

    // Start at index 1 to skip the first line (the CSV header row).
    for (let i = 1; i < lines.length; i++) {
        const line = lines[i].trim();

        // Skip empty lines (e.g., a blank line at the end of the file)
        if (!line) continue;

        // Parse the CSV line into an array of field values
        const cols = parseCsvLine(line);

        // Only include rows that have all required columns (at least 12)
        // This guards against malformed or incomplete rows
        if (cols.length >= 12) {
            records.push(cols);
        }
    }

    console.log('  Loaded ' + records.length.toLocaleString() + ' game records successfully!');
    return records; // Return the array of all game rows
}


// ════════════════════════════════════════════════════════════════
//  HELPER: parseCsvLine(line)
//  Splits one CSV line into an array of field values.
//  Handles fields that contain commas inside quotes.
//
//  Example: `"Grand Theft Auto, V",PS4,Action`
//  Result:  ["Grand Theft Auto, V", "PS4", "Action"]
//
//  Without this, a simple line.split(',') would incorrectly split
//  "Grand Theft Auto, V" into two separate fields.
// ════════════════════════════════════════════════════════════════
function parseCsvLine(line) {
    const fields = [];    // Array to collect each parsed field
    let current  = '';    // Accumulates characters for the current field
    let inQuotes = false; // Tracks whether we're inside a quoted field

    for (let i = 0; i < line.length; i++) {
        const ch = line[i];

        if (ch === '"') {
            // Toggle the inQuotes flag — entering or exiting a quoted section
            inQuotes = !inQuotes;

        } else if (ch === ',' && !inQuotes) {
            // A comma outside of quotes = end of this field
            fields.push(current.trim()); // Save the current field
            current = '';                // Reset for the next field

        } else {
            // Any other character — add it to the current field
            current += ch;
        }
    }

    // Don't forget the last field — there's no trailing comma after it
    fields.push(current.trim());

    return fields;
}


// ════════════════════════════════════════════════════════════════
//  HELPER: toNum(row, col)
//  Safely converts a CSV cell value to a number (float).
//  Returns 0 if the cell is missing, empty, or not a valid number.
//  This prevents NaN (Not a Number) errors from crashing calculations.
// ════════════════════════════════════════════════════════════════
function toNum(row, col) {
    const v = parseFloat(row[col]); // Try to parse as a decimal number
    return isNaN(v) ? 0 : v;        // If it failed (NaN), return 0 instead
}


// ════════════════════════════════════════════════════════════════
//  HELPER: getCol(row, col, fallback)
//  Safely reads a string value from a CSV row at the given column.
//  If the column doesn't exist or is blank, returns 'fallback'.
//  Example: getCol(row, COL_GENRE, 'Unknown')
// ════════════════════════════════════════════════════════════════
function getCol(row, col, fallback = 'Unknown') {
    const v = (row[col] || '').trim(); // Get cell value or empty string, then trim whitespace
    return v || fallback;              // Return fallback if the cell was empty
}


// ════════════════════════════════════════════════════════════════
//  HELPER: topN(map, n)
//  Takes an object (used as a dictionary: { name → totalSales })
//  and returns the top N entries sorted by value, highest first.
//
//  Object.entries() converts { "Sports": 1187.5 } into
//  an array of pairs: [["Sports", 1187.5], ...]
//  Then we sort descending by the value (index 1) and slice to N.
// ════════════════════════════════════════════════════════════════
function topN(map, n) {
    return Object.entries(map)
        .sort((a, b) => b[1] - a[1]) // Sort by value descending (b - a = descending)
        .slice(0, n);                 // Take only the first N results
}


// ════════════════════════════════════════════════════════════════
//  HELPER: fmt(n)
//  Formats a number to always show exactly 2 decimal places,
//  with commas separating thousands for readability.
//  Example: fmt(1234.5) → "1,234.50"
// ════════════════════════════════════════════════════════════════
function fmt(n) {
    return n.toFixed(2)                        // Always 2 decimal places
             .replace(/\B(?=(\d{3})+(?!\d))/g, ','); // Add comma every 3 digits
}


// ════════════════════════════════════════════════════════════════
//  HELPER: fixedWidth(str, width)
//  Ensures a string fits within a fixed character width.
//  If longer: truncates and appends "..." to show it was cut.
//  If shorter: pads with spaces on the right to align columns.
//  Used to create neat, aligned tables in the terminal output.
// ════════════════════════════════════════════════════════════════
function fixedWidth(str, width) {
    const s = String(str || '');
    if (s.length > width) return s.slice(0, width - 3) + '...'; // Too long — truncate
    return s.padEnd(width);                                       // Too short — pad with spaces
}


// ════════════════════════════════════════════════════════════════
//  analyzeData(records)
//  The core analytics function. All Math functions are used here.
//  Loops through all records TWICE:
//    Pass 1 — accumulate totals, find max/min, group by category
//    Pass 2 — calculate standard deviation (needs the mean first)
//  Returns a data object with all computed analytics results.
// ════════════════════════════════════════════════════════════════
function analyzeData(records) {

    // ── Initialize running totals ─────────────────────────────────
    let totalSales = 0;   // Sum of all games' total sales
    let naSales    = 0;   // Sum of North America sales
    let jpSales    = 0;   // Sum of Japan sales
    let palSales   = 0;   // Sum of PAL region sales
    let otherSales = 0;   // Sum of other regions' sales
    let totalCritic = 0;  // Sum of all critic scores (for computing average)
    let criticCount = 0;  // Number of games that have a critic score

    // ── Initialize Math.max / Math.min trackers ───────────────────
    // We use -Infinity as the starting max so any real sale value will be larger.
    // We use +Infinity as the starting min so any real sale value will be smaller.
    let highestSale = -Infinity;
    let lowestSale  =  Infinity;
    let highestGame = ''; // Title of the highest-selling game
    let lowestGame  = ''; // Title of the lowest-selling game

    // ── Category grouping objects ─────────────────────────────────
    // These act as dictionaries: { "Sports": 1187.51, "Action": 1125.89, ... }
    // We use plain objects here (not Map) for simplicity with Object.entries().
    const byGenre     = {};
    const byConsole   = {};
    const byPublisher = {};


    // ════════════════════════════════════════════════════════════
    //  PASS 1 — Loop through every record
    //  Accumulate totals, track max/min, group by category
    // ════════════════════════════════════════════════════════════
    for (const r of records) {

        // Read values from this row's columns (0 if missing/invalid)
        const sale = toNum(r, COL_TOTAL_SALES);
        const na   = toNum(r, COL_NA_SALES);
        const jp   = toNum(r, COL_JP_SALES);
        const pal  = toNum(r, COL_PAL_SALES);
        const ot   = toNum(r, COL_OTHER_SALES);
        const cs   = toNum(r, COL_CRITIC);

        // Add this game's figures to the running totals
        totalSales += sale;
        naSales    += na;
        jpSales    += jp;
        palSales   += pal;
        otherSales += ot;

        // Only count games that actually have a critic score
        if (cs > 0) {
            totalCritic += cs;
            criticCount++;
        }

        // ── Math.max() ────────────────────────────────────────────
        // Returns the larger of two values.
        // If this game's sales beat the current max, it becomes the new max.
        if (Math.max(highestSale, sale) === sale && sale > 0) {
            highestSale = sale;
            highestGame = r[COL_TITLE] + ' (' + r[COL_CONSOLE] + ')';
        }

        // ── Math.min() ────────────────────────────────────────────
        // Returns the smaller of two values.
        // We skip 0-sale games to avoid counting missing/unknown data.
        if (sale > 0 && Math.min(lowestSale, sale) === sale) {
            lowestSale = sale;
            lowestGame = r[COL_TITLE] + ' (' + r[COL_CONSOLE] + ')';
        }

        // ── Group sales by category ───────────────────────────────
        // Get the category label, defaulting to 'Unknown' if the cell is blank
        const genre   = getCol(r, COL_GENRE);
        const cons    = getCol(r, COL_CONSOLE);
        const pub     = getCol(r, COL_PUBLISHER);

        // Add this game's sales to the running total for each category.
        // '|| 0' initializes to 0 if the key doesn't exist yet.
        byGenre[genre]     = (byGenre[genre]     || 0) + sale;
        byConsole[cons]    = (byConsole[cons]     || 0) + sale;
        byPublisher[pub]   = (byPublisher[pub]    || 0) + sale;

    } // ── End of Pass 1 ─────────────────────────────────────────────


    // ── Calculate mean (average) before Pass 2 ───────────────────
    // The mean is needed in Pass 2 to compute each game's deviation from it.
    const rawAvg = (records.length > 0) ? totalSales / records.length : 0;


    // ════════════════════════════════════════════════════════════
    //  PASS 2 — Standard Deviation
    //  Formula: stdDev = sqrt( sum((x - mean)^2) / N )
    //  A higher standard deviation means sales figures are more spread out.
    // ════════════════════════════════════════════════════════════
    let sumSquaredDiffs = 0;

    for (const r of records) {
        const sale = toNum(r, COL_TOTAL_SALES);

        // ── Math.pow(x, 2) ────────────────────────────────────────
        // Computes (sale - mean)^2 — the squared difference from the mean.
        // Squaring makes all values positive (negatives become positives).
        sumSquaredDiffs += Math.pow(sale - rawAvg, 2);
    }

    // ── Math.sqrt() ───────────────────────────────────────────────
    // Divide total squared differences by N to get the variance,
    // then take the square root to get the standard deviation.
    const salesStdDev = (records.length > 0)
        ? Math.sqrt(sumSquaredDiffs / records.length)
        : 0;


    // ── Math.round() ─────────────────────────────────────────────
    // Round the mean to 2 decimal places.
    // Multiply by 100 → round to integer → divide by 100 = 2dp rounding.
    // Example: rawAvg = 0.1034 → * 100 = 10.34 → round = 10 → / 100 = 0.10
    const avgSalesPerGame = Math.round(rawAvg * 100) / 100;


    // ── Average critic score ──────────────────────────────────────
    const avgCritic = (criticCount > 0) ? totalCritic / criticCount : 0;


    // ── Sort all records descending by sales for the Top 10 list ──
    // Spread operator [...records] creates a copy so we don't modify
    // the original array. We then sort this copy.
    const top10Games = [...records]
        .sort((a, b) => toNum(b, COL_TOTAL_SALES) - toNum(a, COL_TOTAL_SALES))
        .slice(0, 10); // Take only the top 10


    // ── Return all results as a single object ─────────────────────
    // This object is passed to displayResults() and exportCSV().
    return {
        totalGames   : records.length,
        totalSales,
        naSales,
        jpSales,
        palSales,
        otherSales,
        avgCritic,
        criticCount,
        // Guard: if no valid sales were found, return 0
        highestSale  : (highestSale === -Infinity) ? 0 : highestSale,
        lowestSale   : (lowestSale  ===  Infinity) ? 0 : lowestSale,
        highestGame,
        lowestGame,
        avgSalesPerGame,
        salesStdDev,
        top5Genre    : topN(byGenre, 5),
        top5Console  : topN(byConsole, 5),
        top5Publisher: topN(byPublisher, 5),
        top10Games
    };
}


// ════════════════════════════════════════════════════════════════
//  displayResults(data)
//  Prints the analytics results to the terminal in a formatted,
//  human-readable layout. Uses process.stdout.write() for lines
//  where console.log would cause naming conflicts.
// ════════════════════════════════════════════════════════════════
function displayResults(data) {
    // Separator lines for visual structure in the terminal output
    const LINE  = '='.repeat(68); // Thick separator for major sections
    const line2 = '-'.repeat(68); // Thin separator for sub-sections

    // ── Title Block ───────────────────────────────────────────────
    console.log('');
    console.log(LINE);
    console.log('   VGChartz 2024  -  CSV Export Analytics Report');
    console.log('   Prepared by: Villaflores');
    console.log(LINE);

    // ── Dataset Overview ──────────────────────────────────────────
    console.log('');
    console.log('  DATASET OVERVIEW');
    console.log(line2);
    console.log('  Total game titles in dataset : ' + data.totalGames.toLocaleString());
    console.log('  Total global sales           : ' + fmt(data.totalSales) + ' million units');
    console.log('  Average critic score         : ' + data.avgCritic.toFixed(2) + ' out of 10');
    console.log('  Titles that have a score     : ' + data.criticCount.toLocaleString());

    // ── Sales Statistics (Math function results) ──────────────────
    console.log('');
    console.log('  SALES STATISTICS  (Math functions)');
    console.log(line2);
    console.log('  Highest single-game sales    : ' + fmt(data.highestSale) + ' M  [Math.max()]');
    console.log('    -> ' + data.highestGame);
    console.log('  Lowest non-zero sale         : ' + fmt(data.lowestSale)  + ' M  [Math.min()]');
    console.log('    -> ' + data.lowestGame);
    console.log('  Average sales per game       : ' + fmt(data.avgSalesPerGame) + ' M  [Math.round()]');
    console.log('  Sales std. deviation         : ' + fmt(data.salesStdDev)     + ' M  [Math.pow() + Math.sqrt()]');

    // ── Regional Breakdown ────────────────────────────────────────
    console.log('');
    console.log('  REGIONAL SALES BREAKDOWN');
    console.log(line2);
    console.log('  North America (NA)   : ' + fmt(data.naSales)    + ' M');
    console.log('  Japan (JP)           : ' + fmt(data.jpSales)    + ' M');
    console.log('  PAL - Europe / AUS   : ' + fmt(data.palSales)   + ' M');
    console.log('  Other Regions        : ' + fmt(data.otherSales) + ' M');

    // ── Top 5 Rankings ────────────────────────────────────────────
    // Reusable arrow function to print any top-N list
    const printTop = (label, arr) => {
        console.log('');
        console.log('  ' + label);
        console.log(line2);
        arr.forEach(([name, sales], i) =>
            // process.stdout.write avoids the automatic newline console.log adds,
            // giving us full control over the line format
            process.stdout.write(
                '  ' + String(i + 1).padStart(2) + '. ' +
                fixedWidth(name, 34) + '  ' + fmt(sales) + ' M\n'
            )
        );
    };

    printTop('TOP 5 GENRES BY TOTAL SALES',     data.top5Genre);
    printTop('TOP 5 CONSOLES BY TOTAL SALES',   data.top5Console);
    printTop('TOP 5 PUBLISHERS BY TOTAL SALES', data.top5Publisher);

    // ── Top 10 Games ──────────────────────────────────────────────
    console.log('');
    console.log('  TOP 10 BEST-SELLING GAMES');
    console.log(line2);
    data.top10Games.forEach((r, i) => {
        process.stdout.write(
            '  ' + String(i + 1).padStart(2) + '. ' +  // Rank number (right-aligned)
            fixedWidth(r[COL_TITLE], 44) + '  ' +        // Game title (truncated if long)
            r[COL_CONSOLE].padEnd(6) + '  ' +            // Console (padded to 6 chars)
            fmt(toNum(r, COL_TOTAL_SALES)) + ' M\n'      // Sales figure
        );
    });

    console.log(LINE);
    console.log('');
}


// ════════════════════════════════════════════════════════════════
//  STEP 3 — exportCSV(data)
//  Writes the analytics summary to a new CSV file called
//  'summary_report.csv' in the current working directory.
//
//  Uses fs.writeFileSync() to write the file synchronously,
//  meaning the program waits for the file to finish saving
//  before continuing to the next line.
//
//  The CSV is structured with three columns:
//    Section  — the category (e.g. "Dataset Overview")
//    Metric   — the specific data point (e.g. "Total Game Titles")
//    Value    — the numeric or text value
// ════════════════════════════════════════════════════════════════
function exportCSV(data) {
    const outputFile = 'summary_report.csv'; // Output file name (saved in current folder)

    console.log('  STEP 3 — Saving the report to: ' + path.resolve(outputFile));
    console.log('  --------------------------------------------');

    // ── Build the rows array ──────────────────────────────────────
    // Each element is an array of [Section, Metric, Value].
    // The first row is the required header row.
    const rows = [
        // Header row — required by the assignment
        ['Section', 'Metric', 'Value'],

        // Dataset overview rows
        ['Dataset Overview', 'Total Game Titles',                  data.totalGames],
        ['Dataset Overview', 'Total Global Sales (Million Units)', data.totalSales.toFixed(2)],
        ['Dataset Overview', 'Average Critic Score (out of 10)',   data.avgCritic.toFixed(2)],
        ['Dataset Overview', 'Titles With a Critic Score',         data.criticCount],

        // Sales statistics rows — these come from Math functions
        ['Sales Statistics', 'Highest Single-Game Sales (M) [Math.max()]',   data.highestSale.toFixed(2)],
        ['Sales Statistics', 'Game with Highest Sales',                        data.highestGame],
        ['Sales Statistics', 'Lowest Non-Zero Sale (M) [Math.min()]',         data.lowestSale.toFixed(2)],
        ['Sales Statistics', 'Game with Lowest Sales',                         data.lowestGame],
        ['Sales Statistics', 'Average Sales per Game (M) [Math.round()]',     data.avgSalesPerGame.toFixed(2)],
        ['Sales Statistics', 'Sales Std Deviation (M) [Math.pow+Math.sqrt]',  data.salesStdDev.toFixed(2)],

        // Regional sales rows
        ['Regional Sales', 'North America (M)',     data.naSales.toFixed(2)],
        ['Regional Sales', 'Japan (M)',             data.jpSales.toFixed(2)],
        ['Regional Sales', 'PAL - Europe/AUS (M)', data.palSales.toFixed(2)],
        ['Regional Sales', 'Other Regions (M)',     data.otherSales.toFixed(2)],

        // Top 5 genres — spread operator flattens the array returned by map()
        ...data.top5Genre.map(([name, val], i) =>
            ['Top 5 Genres', '#' + (i + 1) + ' - ' + name, val.toFixed(2) + ' M']),

        // Top 5 consoles
        ...data.top5Console.map(([name, val], i) =>
            ['Top 5 Consoles', '#' + (i + 1) + ' - ' + name, val.toFixed(2) + ' M']),

        // Top 5 publishers
        ...data.top5Publisher.map(([name, val], i) =>
            ['Top 5 Publishers', '#' + (i + 1) + ' - ' + name, val.toFixed(2) + ' M']),

        // Top 10 best-selling games
        ...data.top10Games.map((r, i) =>
            ['Top 10 Best-Selling Games',
             '#' + (i + 1) + ' - ' + r[COL_TITLE] + ' (' + r[COL_CONSOLE] + ')',
             toNum(r, COL_TOTAL_SALES).toFixed(2) + ' M'])
    ];

    // ── Convert rows array to CSV text ────────────────────────────
    // For each row: convert each cell to a string, escape if needed, join with commas.
    // For each row array: join all cells with '\n' to make lines.
    const csvText = rows.map(row =>
        row.map(cell => {
            const s = String(cell);
            // csvEscape: if a cell contains a comma, quote, or newline,
            // wrap it in double quotes and escape any internal quotes.
            // This follows the official CSV format specification (RFC 4180).
            return (s.includes(',') || s.includes('"') || s.includes('\n'))
                ? '"' + s.replace(/"/g, '""') + '"'
                : s;
        }).join(',')   // Join cells in a row with commas
    ).join('\n');      // Join rows with newlines

    // ── Write the CSV file to disk ────────────────────────────────
    // fs.writeFileSync(path, data, encoding)
    // - Writes 'csvText' to 'outputFile'
    // - 'utf8' ensures proper text encoding
    // - Synchronous: program waits here until the file is fully written
    fs.writeFileSync(outputFile, csvText, 'utf8');

    console.log('  Report saved successfully!');
    console.log('');
    console.log('  You can open "' + outputFile + '" in:');
    console.log('    - Microsoft Excel');
    console.log('    - Google Sheets (File > Import)');
    console.log('    - Any text editor');
}


// ════════════════════════════════════════════════════════════════
//  MAIN FUNCTION — main()
//  The program's entry point. Runs all steps in order:
//    1. Ask the user for a valid file path
//    2. Load the CSV into memory
//    3. Analyze the data (all math happens here)
//    4. Display the results in the terminal
//    5. Export the summary report to a CSV file
//
//  'async' allows us to use 'await' inside this function,
//  which pauses execution until async operations complete
//  (like waiting for the user to type their file path).
// ════════════════════════════════════════════════════════════════
async function main() {
    // ── Welcome Banner ────────────────────────────────────────────
    console.log('');
    console.log('  +===================================================+');
    console.log('  |  VGChartz CSV Analytics Report  -  Villaflores   |');
    console.log('  +===================================================+');
    console.log('');
    console.log('  This program reads a VGChartz CSV file and generates');
    console.log('  a summary analytics report, then exports it as a new');
    console.log('  CSV file called "summary_report.csv".');

    // ── Step 1: Get a valid file path ─────────────────────────────
    // 'await' pauses here until getValidFilePath() returns a valid path
    const filePath = await getValidFilePath();

    // Close the readline interface — we're done reading from the terminal
    rl.close();

    // ── Step 2: Load the CSV file into memory ─────────────────────
    const records = loadCSV(filePath);

    // ── Step 3 (internal): Run all analytics and math ─────────────
    console.log('  Performing analytics...');
    const data = analyzeData(records);

    // ── Step 3 (display): Print formatted results to terminal ─────
    displayResults(data);

    // ── Step 3 (export): Save the report as summary_report.csv ───
    exportCSV(data);

    // ── Done ──────────────────────────────────────────────────────
    console.log('  All done! Thank you for using the Analytics Report tool.');
    console.log('');
}


// ── START THE PROGRAM ─────────────────────────────────────────────
// Call main() to begin execution.
// .catch() handles any unexpected errors that bubble up from main().
// If something crashes, a friendly error message is shown instead of
// a raw stack trace that would confuse non-technical users.
main().catch(err => {
    console.error('');
    console.error('  Something went wrong: ' + err.message);
    console.error('  Please restart the program and try again.');
    process.exit(1); // Exit with error code 1 to signal failure to the OS
});