Attendance Tracker — Quick README

What this is
------------
Simple Java Swing sign-in app. Shows fields for Name, Course, Year Level, Time In (auto) and E-Signature (auto).


Files
-----
- Attendance.java — Java source (package: Attendance).
- README.md — this file.


Requirements
------------
- Java JDK (for javac) to compile. A JRE can run compiled classes.
- If javac is not available, compile on a machine with a JDK and distribute the Attendance folder containing Attendance.class.


Where the app saves data
------------------------
- By default it saves to ${user.home}/.attendance_records.csv (created automatically on first save).
- You do NOT need to include that CSV when sharing the project unless you want to ship pre-filled records. To ship pre-filled data, include a file named .attendance_records.csv and tell recipients to place it in their home directory.


CSV format (short)
------------------
- Each row: "Name","Course","Year Level","Time In","E-Signature"


Attendance.java File
-------------------------
- contains the Attendance class and the whole app: UI layout, sign-in logic, Time In (uses LocalDateTime.now()), E-Signature (UUID.randomUUID()), CSV read/write, and the admin viewer (default password admin123).


Class files (.class) — brief explanations
---------------------------------------
- `Attendance.class`: Main compiled class. Contains the `Attendance` JFrame, `main()` method, UI setup, sign-in logic, and CSV persistence.
- `Attendance$1.class`: Runnable used with `SwingUtilities.invokeLater` to start the UI on the Event Dispatch Thread.
- `Attendance$2.class`: Anonymous `ActionListener` for the "Sign In" button (validates inputs, sets Time In, generates E-Signature, saves record).
- `Attendance$3.class`: Anonymous `ActionListener` for the "Clear" button (resets form fields).
- `Attendance$4.class`: Anonymous `ActionListener` that handles the "View Records" admin flow; inner classes `Attendance$4$1.class`, `Attendance$4$2.class`, and `Attendance$4$3.class` are small listeners and handlers used inside the admin dialog (window/listener and Delete/Close handlers).
- `Attendance$5.class`: Anonymous `ActionListener` for the "Exit" button (closes the window).
- `Attendance$6.class`: Window listener that saves records when the main window closes.
- `Attendance$7.class`: Additional compiler-generated helper/anonymous listener class used in the source.
