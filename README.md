# Employee Shift Scheduler (Python + Java) — Priority-Ranked Preferences

This project contains **two implementations of the same console application**:

- **Python**: `final_scheduler.py`
- **Java**: `EmployeeScheduler.java` (public class name: `EmployeeScheduler`)

The program builds a **7‑day schedule** with **3 shifts per day** (Morning, Afternoon, Evening), using **priority-ranked employee preferences** and simple rules to keep the schedule fair and covered.

---

## What the application does: 

### Scheduling rules (constraints)
- **7 days**: Monday → Sunday  
- **3 shifts/day**: Morning, Afternoon, Evening  
- **Max 5 working days per employee per week**
- **Minimum 2 employees per shift** (the program tries to meet this using available employees)

### Priority-ranked preferences (bonus feature)
For each day an employee wants to work, they can enter:
- **1st choice shift**
- **2nd choice shift**
- **3rd choice shift**

The scheduler will use these ranked choices when assigning shifts.

---

## How scheduling works (high-level)

The scheduling logic runs in **three passes**:

1. **Preferred assignment pass**
   - For each employee, and for each day they requested, the scheduler assigns them to a shift based on their priority list.

2. **Minimum coverage pass**
   - The scheduler checks every day/shift.
   - If a shift has fewer than the minimum required employees, it assigns additional available employees (who are not already working that day and have not hit the max days).

3. **Conflict resolution pass**
   - If an employee requested certain days but wasn’t scheduled on those days, the scheduler tries to place them using their ranked shift list (only if they still have capacity for more work days).



---

## Project structure

```
.
├── final_scheduler.py              # Python implementation
├── EmployeeScheduler.java    # Java implementation (class: EmployeeScheduler)
├── Final_Python_Code_output.png     # Sample Python console output 
└── Final_Java_Code_output.png       # Sample Java console output 
```

---

# Run the Python version

## 1) Prerequisites
- Python **3.8+** recommended  
- No external libraries needed (only standard library modules)

## 2) Run
From the folder containing the file:

```bash
python3 final_scheduler.py
```

## 3) Follow the prompts (input flow)
1. Enter number of employees
2. For each employee:
   - Enter employee name
   - Enter number of days they want to work (**1–5**)
   - For each day:
     - Enter a valid day name (Monday–Sunday)
     - Enter ranked shift preferences (Morning/Afternoon/Evening)

## 4) Output
The program prints:
- Weekly schedule (day → shift → assigned employees)
- Work summary (days worked per employee)

---

# Run the Java version

## 1) Prerequisites
- JDK **11+** recommended (8+ should also work)
- `javac` and `java` available in your terminal

Check:

```bash
javac -version
java -version
```

## 2) IMPORTANT: file name vs class name
The Java code declares:

- `public class EmployeeScheduler`

In Java, a `public class` **must** be in a file with the same name.

### Option A (recommended): rename the file
Rename:

- `final_EmployeeScheduler.java` → `EmployeeScheduler.java`

Then compile/run:

```bash
javac EmployeeScheduler.java
java EmployeeScheduler
```

### Option B: keep the filename (not recommended)
If you keep `final_EmployeeScheduler.java`, compilation may fail with:
> “class EmployeeScheduler is public, should be declared in a file named EmployeeScheduler.java”

So **renaming is the cleanest fix**.

## 3) Follow the prompts
The Java program asks for the same inputs as the Python version:
- number of employees
- employee name
- days (1–5)
- ranked shift preferences per day

---

## Error handling & input validation (Python + Java)

Both implementations are designed to **keep prompting until valid input is received**, instead of crashing.

### Examples of handled cases
- **Non-numeric input** when a number is expected (e.g., “three” instead of `3`)
- **Out-of-range values**
  - employee count less than 1
  - days-to-work outside **1–5**
- **Empty employee name**
- **Invalid day names**
  - must be one of: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
- **Invalid shift names**
  - must be one of: Morning, Afternoon, Evening
- **Duplicate day entries** for the same employee

### Python-specific handling
- Uses `try/except ValueError` for numeric parsing.
- Uses loops to re-prompt until valid values are entered.
- In the “minimum coverage” stage, it uses a safety check so it **breaks out** if no more employees are available (prevents infinite loops).

### Java-specific handling
- Uses `try/catch (NumberFormatException)` when parsing integers from the console.
- Uses loops + condition checks to validate day/shift choices and avoid duplicates.
- Also protects coverage loops by stopping when no eligible employees remain.

---

## Troubleshooting

### Python
- **`python3: command not found`**
  - Install Python 3 or use `python` if that is mapped to Python 3 on your system.

### Java
- **`javac` not found**
  - Install a JDK and make sure it’s on your PATH.
- **Public class name error**
  - Rename the file to `EmployeeScheduler.java` (see above).

---

## Tips for demo/testing
- Try **3–10 employees** for a smooth demo.
- If you provide too few employees, the scheduler may not be able to meet “2 employees per shift” for every shift (it will fill as much as it can without breaking other rules).

---

## Screenshots
Sample console outputs are included:
- `Final_Python_Code_output.png`
- `Final_Java_Code_output.png`
