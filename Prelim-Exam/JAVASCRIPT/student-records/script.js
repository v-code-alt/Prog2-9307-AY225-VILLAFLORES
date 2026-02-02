// Joellen T. Villaflores, 25-0775-853

// Hardcoded CSV data
const csvData = `ID,Name,Grade\n101,John Doe,90\n102,Jane Smith,85\n103,Sam Lee,88`;

// Parse CSV to array of objects
function parseCSV(csv) {
    const [header, ...rows] = csv.trim().split(/\r?\n/);
    const keys = header.split(",");
    return rows.map(row => {
        const values = row.split(",");
        return Object.fromEntries(keys.map((k, i) => [k, values[i]]));
    });
}

let students = parseCSV(csvData);

function render() {
    const tbody = document.getElementById("student-tbody");
    tbody.innerHTML = "";
    students.forEach((student, idx) => {
        tbody.innerHTML += `
            <tr>
                <td>${student.ID}</td>
                <td>${student.Name}</td>
                <td>${student.Grade}</td>
                <td><button onclick="deleteStudent(${idx})">Delete</button></td>
            </tr>
        `;
    });
}

function addStudent() {
    const id = document.getElementById("id").value.trim();
    const name = document.getElementById("name").value.trim();
    const grade = document.getElementById("grade").value.trim();
    if (id && name && grade) {
        students.push({ ID: id, Name: name, Grade: grade });
        render();
        document.getElementById("id").value = "";
        document.getElementById("name").value = "";
        document.getElementById("grade").value = "";
    }
}

function deleteStudent(idx) {
    students.splice(idx, 1);
    render();
}

document.addEventListener("DOMContentLoaded", render);
