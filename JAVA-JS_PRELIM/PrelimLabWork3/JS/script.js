function validateInput(value, min, max) {
            const num = parseFloat(value);
            return !isNaN(num) && num >= min && num <= max;
        }

        function calculateGrade() {
            // Get input values
            const absences = document.getElementById('absences').value;
            const lab1 = document.getElementById('lab1').value;
            const lab2 = document.getElementById('lab2').value;
            const lab3 = document.getElementById('lab3').value;

            // Reset error messages
            document.querySelectorAll('.error').forEach(e => e.style.display = 'none');

            // Validate inputs
            let isValid = true;

            if (!validateInput(absences, 0, 4)) {
                document.getElementById('absences-error').style.display = 'block';
                isValid = false;
            }
            if (!validateInput(lab1, 0, 100)) {
                document.getElementById('lab1-error').style.display = 'block';
                isValid = false;
            }
            if (!validateInput(lab2, 0, 100)) {
                document.getElementById('lab2-error').style.display = 'block';
                isValid = false;
            }
            if (!validateInput(lab3, 0, 100)) {
                document.getElementById('lab3-error').style.display = 'block';
                isValid = false;
            }

            if (!isValid) return;

            // Convert to numbers
            const absencesNum = parseFloat(absences);
            const attendanceScore = 100 - absencesNum;
            const lab1Score = parseFloat(lab1);
            const lab2Score = parseFloat(lab2);
            const lab3Score = parseFloat(lab3);

            // Calculate Lab Work Average
            const labWorkAverage = (lab1Score + lab2Score + lab3Score) / 3;

            // Calculate Class Standing (30% of Prelim Grade)
            const classStanding = (attendanceScore * 0.40) + (labWorkAverage * 0.60);

            // Calculate required Prelim Exam scores
            // Formula: Prelim Grade = (Prelim Exam × 0.70) + (Class Standing × 0.30)
            // Rearranged: Prelim Exam = (Prelim Grade - (Class Standing × 0.30)) / 0.70
    
            const passingGrade = 75;
            const excellentGrade = 100;

            const requiredForPass = (passingGrade - (classStanding * 0.30)) / 0.70;
            const requiredForExcellent = (excellentGrade - (classStanding * 0.30)) / 0.70;

            // Display results
            document.getElementById('display-attendance').textContent = Math.round(attendanceScore) + '%';
            document.getElementById('display-absences').textContent = absencesNum + ' absences';
            document.getElementById('display-lab1').textContent = lab1Score.toFixed(2);
            document.getElementById('display-lab2').textContent = lab2Score.toFixed(2);
            document.getElementById('display-lab3').textContent = lab3Score.toFixed(2);
            document.getElementById('display-lab-avg').textContent = Math.round(labWorkAverage);
            document.getElementById('display-lab-avg-formula').textContent = `(${lab1Score.toFixed(0)} + ${lab2Score.toFixed(0)} + ${lab3Score.toFixed(0)}) / 3 = ${Math.round(labWorkAverage)}`;
            document.getElementById('display-class-standing').textContent = Math.round(classStanding);
            document.getElementById('display-class-standing-formula').textContent = '40% Attendance + 60% Lab Avg';

            // Display required scores
            // Show or hide pass score
            if (requiredForPass > 100) {
                document.getElementById('pass-score').textContent = '';
            } else {
                document.getElementById('pass-score').textContent = Math.round(requiredForPass);
            }
            // Show or hide excellent score
            if (requiredForExcellent > 100) {
                document.getElementById('excellent-score').textContent = '';
            } else {
                document.getElementById('excellent-score').textContent = Math.round(requiredForExcellent);
            }

            // Generate remarks for passing grade
            const passRemark = document.getElementById('pass-remark');
            if (requiredForPass <= 0) {
                passRemark.textContent = '🎉 You already passed! Your Class Standing is excellent!';
                passRemark.className = 'remark success';
            } else if (requiredForPass <= 100) {
                passRemark.textContent = `You need to score ${Math.round(requiredForPass)} on the Prelim Exam to pass.`;
                passRemark.className = 'remark warning';
            } else {
                passRemark.textContent = '⚠️ Unfortunately, it is not possible to pass even with a perfect Prelim Exam score.';
                passRemark.className = 'remark danger';
            }

            // Generate remarks for excellent grade
            const excellentRemark = document.getElementById('excellent-remark');
            if (requiredForExcellent <= 0) {
                excellentRemark.textContent = '🌟 Perfect! You already have an excellent standing!';
                excellentRemark.className = 'remark success';
            } else if (requiredForExcellent <= 100) {
                excellentRemark.textContent = `You need to score ${Math.round(requiredForExcellent)} on the Prelim Exam for excellence.`;
                excellentRemark.className = 'remark warning';
            } else {
                excellentRemark.textContent = '⚠️ It is not possible to achieve 100% even with a perfect Prelim Exam score.';
                excellentRemark.className = 'remark danger';
            }

            // Hide form container and show combined results/return button
            document.querySelector('.form-container').style.display = 'none';
            document.getElementById('results-combined').style.display = 'block';
            document.getElementById('return-btn-container').style.display = 'block';
        }

        // Allow Enter key to submit
        document.querySelectorAll('input').forEach(input => {
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    calculateGrade();
                }
            });
        });

        // Return button functionality
        function returnToForm() {
            document.querySelector('.form-container').style.display = 'block';
            document.getElementById('results-combined').style.display = 'none';
            document.getElementById('return-btn-container').style.display = 'none';
            // Optionally clear inputs and results
            document.getElementById('absences').value = '';
            document.getElementById('lab1').value = '';
            document.getElementById('lab2').value = '';
            document.getElementById('lab3').value = '';
            document.getElementById('display-attendance').textContent = '-';
            document.getElementById('display-absences').textContent = '';
            document.getElementById('display-lab1').textContent = '-';
            document.getElementById('display-lab2').textContent = '-';
            document.getElementById('display-lab3').textContent = '-';
            document.getElementById('display-class-standing').textContent = '-';
            document.getElementById('display-class-standing-formula').textContent = '';
            document.getElementById('display-lab-avg').textContent = '-';
            document.getElementById('display-lab-avg-formula').textContent = '';
            document.getElementById('pass-score').textContent = '-';
            document.getElementById('excellent-score').textContent = '-';
            document.getElementById('pass-remark').textContent = '';
            document.getElementById('excellent-remark').textContent = '';
        }