# Programming 2 – Machine Problem Submissions

Student: Villaflores, Joellen T.
Course: Programming 2 Laboratory
School: University of Perpetual Help System DALTA Molino Campus
Assigned Problems: MP06, MP07, MP08


## MP06 – Display Unique Values in a Column
### Java
This program asks the user for a CSV file and reads it line by line. It stores all the rows in a list and shows the user what columns are available. The user picks a column and the program goes through every row collecting values, but skips any value it has already seen before. At the end it prints all the unique values it found and how many there were.

### JavaScript
This program asks the user for a CSV file path and reads the file using Node.js. It splits the file into rows and columns and shows the user the available column names to choose from. Once the user picks a column, the program collects all the values in that column but removes any repeats. It then displays the final list of unique values with a total count.


## MP07 – Sort Records Alphabetically by a Column
### Java
This program reads a CSV file and stores all the rows in a list. It shows the user the available columns and asks which one to sort by. The program then rearranges all the rows from A to Z based on the values in that column. The sorted records are displayed as a table with the headers on top and a total count at the bottom.

### JavaScript
This program reads a CSV file using Node.js and separates it into headers and data rows. The user picks a column and the program sorts all the rows alphabetically based on the values in that column. It then prints the sorted rows in a neat table format with aligned columns. A total record count is shown at the end.


## MP08 – Filter Records Using a Keyword
### Java
This program reads a CSV file and asks the user to pick a column and enter a keyword. It then goes through every row and checks if the value in the chosen column contains that keyword. Any row that matches is saved and printed as a table at the end. The output also shows how many rows matched out of the total number of records.

### JavaScript
This program reads a CSV file using Node.js and asks the user to choose a column and type a keyword. It checks every row to see if the selected column contains that keyword and collects all the matching rows. The matching records are then displayed in a formatted table. At the bottom it shows how many rows matched compared to the total.