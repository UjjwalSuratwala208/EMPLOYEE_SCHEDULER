/**
 * Employee Shift Scheduling System
 * 
 * This program manages employee schedules for a company operating 7 days a week
 * with three shifts per day (Morning, Afternoon, Evening). It collects employee
 * preferences, enforces scheduling constraints, and resolves conflicts to create
 * an optimal weekly schedule.
 * 
 * Key Features:
 * - Collects employee shift preferences through user input
 * - Enforces scheduling constraints (max 5 days/week, min 2 employees/shift)
 * - Resolves conflicts when preferences cannot be met
 * - Displays formatted weekly schedule
 * - BONUS: Supports priority ranking for shift preferences (1st, 2nd, 3rd choice)
 * 
 * Control Structures Demonstrated:
 * - If/else conditionals for validation and constraint checking
 * - For loops for iteration through collections
 * - While loops for ensuring minimum coverage
 * - Break/continue statements for loop control
 * - Try-catch blocks for exception handling
 * - Nested loops for complex scheduling logic
 * 
 * @author Assignment 4 - Implementing Control Structures
 * @version 2.0 (with bonus feature)
 */

import java.util.*;

/**
 * Main class that implements the employee scheduling system.
 * Uses maps, lists, and sets to manage scheduling data efficiently.
 */
public class EmployeeScheduler {
    // ==================== CONSTANTS ====================
    // Define days of the week
    private static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    
    // Define available shift times
    private static final String[] SHIFTS = {"Morning", "Afternoon", "Evening"};
    
    // Maximum days an employee can work per week
    private static final int MAX_DAYS_PER_WEEK = 5;
    
    // Minimum employees required per shift for adequate coverage
    private static final int MIN_EMPLOYEES_PER_SHIFT = 2;
    
    // ==================== DATA STRUCTURES ====================
    // List to store all employee names
    private List<String> employees;
    
    // Map: employee name -> (day -> list of preferred shifts in priority order)
    // BONUS: Stores shift preferences as priority-ranked lists
    // Example: {"Alice" -> {"Monday" -> ["Morning", "Afternoon", "Evening"]}}
    private Map<String, Map<String, List<String>>> preferences;
    
    // Nested map: day -> shift -> list of assigned employees
    // This is the main schedule data structure
    private Map<String, Map<String, List<String>>> schedule;
    
    // Map: employee name -> number of days worked
    // Tracks workload to enforce the 5-day maximum
    private Map<String, Integer> employeeDaysWorked;
    
    /**
     * Constructor: Initializes the scheduler with empty data structures.
     * 
     * Creates and initializes all necessary data structures:
     * - Empty employee list
     * - Empty preferences map
     * - Pre-structured schedule map (nested: day -> shift -> employee list)
     * - Empty days-worked counter map
     */
    public EmployeeScheduler() {
        // Initialize collections
        employees = new ArrayList<>();
        preferences = new HashMap<>();
        schedule = new HashMap<>();
        employeeDaysWorked = new HashMap<>();
        
        // Initialize schedule structure: nested map with all days and shifts
        // Outer loop: For each day of the week
        for (String day : DAYS) {
            // Create a map for this day's shifts
            Map<String, List<String>> daySchedule = new HashMap<>();
            
            // Inner loop: For each shift in the day
            for (String shift : SHIFTS) {
                // Initialize empty employee list for this shift
                daySchedule.put(shift, new ArrayList<>());
            }
            
            // Add this day's schedule to the main schedule map
            schedule.put(day, daySchedule);
        }
    }
    
    /**
     * Add an employee with their shift preferences to the system.
     * 
     * This method stores employee information for later scheduling.
     * Uses conditional logic to prevent duplicate entries.
     * 
     * BONUS FEATURE: Supports priority-ranked shift preferences
     * 
     * @param name Employee's name
     * @param shiftPreferences Map of day names to priority-ordered list of preferred shifts
     *                        Example: {"Monday" -> ["Morning", "Afternoon", "Evening"]}
     *                        First item is 1st choice, second is 2nd choice, third is 3rd choice
     */
    public void addEmployee(String name, Map<String, List<String>> shiftPreferences) {
        // Conditional: Only add if employee doesn't already exist
        if (!employees.contains(name)) {
            employees.add(name);  // Add to employee list
            preferences.put(name, shiftPreferences);  // Store their preferences
            employeeDaysWorked.put(name, 0);  // Initialize days worked counter to 0
        }
    }
    
    /**
     * Main scheduling logic with conflict resolution and priority preferences.
     * 
     * This method implements a three-pass algorithm:
     * Pass 1: Assign employees to their preferred shifts (trying priorities in order)
     * Pass 2: Fill understaffed shifts with available employees
     * Pass 3: Resolve remaining conflicts using priority preferences
     * 
     * Uses nested loops to iterate through employees and days, with
     * conditionals to enforce all scheduling constraints.
     * 
     * BONUS FEATURE: Tries to assign shifts based on priority ranking
     */
    public void assignShifts() {
        // ========== FIRST PASS: Assign preferred shifts using priority ranking ==========
        // Outer loop: Iterate through each employee
        for (String employee : employees) {
            // Get this employee's preferences
            Map<String, List<String>> empPrefs = preferences.get(employee);
            
            // Inner loop: Check each day of the week
            for (String day : DAYS) {
                // Conditional: Check if employee has reached maximum work days
                if (employeeDaysWorked.get(employee) >= MAX_DAYS_PER_WEEK) {
                    break;  // Break out of day loop if max days reached
                }
                
                // Conditional: Check if employee has a preference for this day
                if (empPrefs.containsKey(day)) {
                    // BONUS: Get priority-ranked list of shifts for this day
                    List<String> preferredShifts = empPrefs.get(day);
                    
                    // Check if employee is already scheduled for this day (any shift)
                    boolean alreadyScheduled = isEmployeeScheduledOnDay(employee, day);
                    
                    // Conditional: Only schedule if not already working this day
                    if (!alreadyScheduled) {
                        // BONUS: Try shifts in priority order (1st choice, then 2nd, then 3rd)
                        boolean assigned = false;
                        for (String preferredShift : preferredShifts) {
                            // Try to assign to this priority shift
                            schedule.get(day).get(preferredShift).add(employee);
                            // Increment the counter for days worked
                            employeeDaysWorked.put(employee, employeeDaysWorked.get(employee) + 1);
                            assigned = true;
                            break;  // Successfully assigned, move to next day
                        }
                    }
                }
            }
        }
        
        // ========== SECOND PASS: Ensure minimum coverage ==========
        ensureMinimumCoverage();
        
        // ========== THIRD PASS: Resolve conflicts using priority preferences ==========
        resolveConflicts();
    }
    
    /**
     * Check if an employee is already scheduled on a specific day.
     * 
     * Helper method that checks all shifts for a given day to see if
     * the employee is assigned to any of them. Uses a loop with early
     * return (branching) for efficiency.
     * 
     * @param employee The employee's name
     * @param day The day to check
     * @return true if employee is scheduled on this day, false otherwise
     */
    private boolean isEmployeeScheduledOnDay(String employee, String day) {
        // Loop through all shifts for this day
        for (String shift : SHIFTS) {
            // Conditional: Check if employee is in this shift
            if (schedule.get(day).get(shift).contains(employee)) {
                return true;  // Early return - employee found, no need to check more
            }
        }
        return false;  // Employee not found in any shift for this day
    }
    
    /**
     * Ensure each shift has minimum required employees.
     * 
     * This method uses nested loops to check all shifts and automatically
     * assigns available employees to understaffed shifts. Uses while loop
     * to continue assigning until minimum coverage is met or no employees
     * are available.
     */
    private void ensureMinimumCoverage() {
        // Outer loop: Iterate through each day
        for (String day : DAYS) {
            // Inner loop: Check each shift within the day
            for (String shift : SHIFTS) {
                // Get list of currently assigned employees for this shift
                List<String> currentShift = schedule.get(day).get(shift);
                int currentCount = currentShift.size();
                
                // While loop: Continue until minimum coverage is met
                while (currentCount < MIN_EMPLOYEES_PER_SHIFT) {
                    boolean assigned = false;  // Flag to track if we assigned someone
                    
                    // Loop through all employees to find someone available
                    for (String employee : employees) {
                        // Conditional: Check if employee can work more days
                        if (employeeDaysWorked.get(employee) >= MAX_DAYS_PER_WEEK) {
                            continue;  // Skip this employee, move to next
                        }
                        
                        // Conditional: Check if already scheduled for this day
                        if (isEmployeeScheduledOnDay(employee, day)) {
                            continue;  // Skip if already working this day
                        }
                        
                        // Conditional: Check if not already in this shift
                        if (!currentShift.contains(employee)) {
                            // Assign employee to this shift
                            currentShift.add(employee);
                            employeeDaysWorked.put(employee, employeeDaysWorked.get(employee) + 1);
                            currentCount++;
                            assigned = true;
                            break;  // Exit employee loop, got someone
                        }
                    }
                    
                    // Conditional: If no one was assigned, exit while loop
                    // This prevents infinite loop when no employees available
                    if (!assigned) {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Resolve scheduling conflicts by reassigning employees using priority preferences.
     * 
     * This method handles cases where employees couldn't get their preferred
     * shifts by attempting to schedule them using their ranked preferences.
     * Uses Set operations to efficiently find scheduling gaps.
     * 
     * BONUS FEATURE: Uses priority ranking to find best alternate shifts
     */
    private void resolveConflicts() {
        // Loop through each employee to check for conflicts
        for (String employee : employees) {
            // Get this employee's preferences
            Map<String, List<String>> empPrefs = preferences.get(employee);
            Set<String> preferredDays = empPrefs.keySet();
            
            // Find which days they're actually scheduled using a Set
            Set<String> scheduledDays = new HashSet<>();
            // Nested loop: Check all days and shifts
            for (String day : DAYS) {
                // Conditional: If employee is scheduled this day, add to set
                if (isEmployeeScheduledOnDay(employee, day)) {
                    scheduledDays.add(day);
                }
            }
            
            // Set operation: Find days they wanted but didn't get
            // Create a new set with preferred days
            Set<String> missingDays = new HashSet<>(preferredDays);
            // Remove all days they are scheduled (leaves only missing days)
            missingDays.removeAll(scheduledDays);
            
            // Loop through days that need resolution
            for (String day : missingDays) {
                // Conditional: Check if employee has capacity for more days
                if (employeeDaysWorked.get(employee) >= MAX_DAYS_PER_WEEK) {
                    break;  // Employee at max capacity, stop trying
                }
                
                // Conditional: Only try to schedule if not already working this day
                if (!scheduledDays.contains(day)) {
                    // BONUS: Try shifts in priority order for this day
                    List<String> priorityShifts = empPrefs.getOrDefault(day, Arrays.asList(SHIFTS));
                    for (String shift : priorityShifts) {
                        // Conditional: Check if employee not already in this shift
                        if (!schedule.get(day).get(shift).contains(employee)) {
                            // Assign to this shift (respecting priority if available)
                            schedule.get(day).get(shift).add(employee);
                            employeeDaysWorked.put(employee, employeeDaysWorked.get(employee) + 1);
                            scheduledDays.add(day);
                            break;  // Found a shift, move to next day
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Display the final schedule in a readable format.
     * 
     * Uses formatted string output (printf) to create a professional-looking
     * schedule display with clear sections and alignment. Demonstrates use
     * of loops for iterating through schedule data.
     */
    public void displaySchedule() {
        // Print header with decorative borders
        System.out.println("\n" + "=".repeat(80));
        System.out.println(centerString("EMPLOYEE SCHEDULE FOR THE WEEK", 80));
        System.out.println("=".repeat(80) + "\n");
        
        // Loop through each day to display schedule
        for (String day : DAYS) {
            System.out.println("\n" + day.toUpperCase());  // Day header in uppercase
            System.out.println("-".repeat(60));  // Separator line
            
            // Loop through each shift for this day
            for (String shift : SHIFTS) {
                List<String> employeeList = schedule.get(day).get(shift);
                // Conditional: Display employee list or "No employees assigned"
                String employees = employeeList.isEmpty() ? 
                    "No employees assigned" : String.join(", ", employeeList);
                // Formatted output with alignment using printf
                System.out.printf("  %-12s : %s%n", shift, employees);
            }
        }
        
        // Print employee summary section
        System.out.println("\n" + "=".repeat(80));
        System.out.println(centerString("EMPLOYEE WORK SUMMARY", 80));
        System.out.println("=".repeat(80) + "\n");
        
        // Sort employees alphabetically for consistent display
        List<String> sortedEmployees = new ArrayList<>(employees);
        Collections.sort(sortedEmployees);
        
        // Loop through employees to display their work summary
        for (String employee : sortedEmployees) {
            int daysWorked = employeeDaysWorked.get(employee);
            // Formatted output showing days worked per employee
            System.out.printf("  %-20s : %d days%n", employee, daysWorked);
        }
        
        System.out.println("\n" + "=".repeat(80) + "\n");
    }
    
    /**
     * Helper method to center a string within a given width.
     * 
     * Calculates padding needed and returns centered string.
     * Uses Math.max to prevent negative padding values.
     * 
     * @param text The text to center
     * @param width The total width to center within
     * @return The centered string with appropriate padding
     */
    private String centerString(String text, int width) {
        // Calculate padding needed on left side
        int padding = (width - text.length()) / 2;
        // Use Math.max to ensure non-negative padding
        return " ".repeat(Math.max(0, padding)) + text;
    }
    
    /**
     * Main method to run the scheduler with user input.
     * 
     * This method handles all user interaction, input validation using
     * exception handling, and orchestrates the scheduling process.
     * Demonstrates extensive use of loops, conditionals, and try-catch blocks.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create Scanner for reading user input
        Scanner scanner = new Scanner(System.in);
        // Create scheduler instance
        EmployeeScheduler scheduler = new EmployeeScheduler();
        
        // Display welcome message
        System.out.println("Employee Shift Scheduling System");
        System.out.println("=".repeat(60));
        
        // ========== INPUT VALIDATION: Get number of employees ==========
        int numEmployees = 0;
        // While loop continues until valid input received
        while (numEmployees < 1) {
            try {
                // Try to parse user input as integer
                System.out.print("\nHow many employees to schedule? (3-10 recommended): ");
                numEmployees = Integer.parseInt(scanner.nextLine());
                
                // Conditional: Validate minimum number
                if (numEmployees < 1) {
                    System.out.println("Please enter at least 1 employee.");
                }
            } catch (NumberFormatException e) {
                // Exception handling: Catch non-integer input
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // ========== LOOP: Collect information for each employee ==========
        for (int i = 0; i < numEmployees; i++) {
            System.out.println("\n--- Employee " + (i + 1) + " ---");
            
            // INPUT VALIDATION: Get employee name
            String name = "";
            // While loop: Ensure name is not empty
            while (name.trim().isEmpty()) {
                System.out.print("Enter employee name: ");
                name = scanner.nextLine().trim();
                // Conditional: Check if name is empty after trimming whitespace
                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty.");
                }
            }
            
            // Display instructions for shift preferences
            System.out.println("\nEnter shift preferences for " + name);
            System.out.println("Available days: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday");
            System.out.println("Available shifts: Morning, Afternoon, Evening");
            System.out.println("*** BONUS FEATURE: You can rank your shift preferences (1st, 2nd, 3rd choice) ***");
            
            // Map to store this employee's preferences
            Map<String, List<String>> preferences = new HashMap<>();
            
            // INPUT VALIDATION: Get number of days employee wants to work
            int numPrefs = 0;
            // While loop: Ensure valid number between 1-5
            while (numPrefs < 1 || numPrefs > 5) {
                try {
                    // Try to parse input as integer
                    System.out.print("How many days does " + name + " want to work? (1-5): ");
                    numPrefs = Integer.parseInt(scanner.nextLine());
                    
                    // Conditional: Validate range (1-5 days)
                    if (numPrefs < 1 || numPrefs > 5) {
                        System.out.println("Please enter a number between 1 and 5.");
                    }
                } catch (NumberFormatException e) {
                    // Exception handling: Catch non-integer input
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
            
            // ========== LOOP: Collect preferences for each day ==========
            for (int j = 0; j < numPrefs; j++) {
                // INPUT VALIDATION: Get valid day
                String day = "";
                // While loop: Continue until valid day entered
                while (!Arrays.asList(DAYS).contains(day)) {
                    System.out.print("  Day " + (j + 1) + " (e.g., Monday): ");
                    day = scanner.nextLine().trim();
                    
                    // Capitalize first letter for consistency
                    if (!day.isEmpty()) {
                        day = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
                    }
                    
                    // Conditional: Check if day is valid
                    if (Arrays.asList(DAYS).contains(day)) {
                        // Nested conditional: Check for duplicate days
                        if (preferences.containsKey(day)) {
                            System.out.println(day + " already entered. Choose a different day.");
                            day = "";  // Reset to trigger loop continuation
                        }
                    } else {
                        System.out.println("Invalid day. Choose from: " + String.join(", ", DAYS));
                    }
                }
                
                // BONUS FEATURE: Collect ranked preferences for this day
                System.out.println("    Enter your shift preferences for " + day + " in priority order:");
                List<String> shiftPriorities = new ArrayList<>();
                List<String> availableShifts = new ArrayList<>(Arrays.asList(SHIFTS));
                
                // Collect 1st, 2nd, and 3rd choice
                String[] priorityLabels = {"1st", "2nd", "3rd"};
                for (String priority : priorityLabels) {
                    if (availableShifts.isEmpty()) {
                        break;
                    }
                    
                    String shift = "";
                    while (true) {
                        System.out.print("      " + priority + " choice (Morning/Afternoon/Evening): ");
                        shift = scanner.nextLine().trim();
                        
                        // Capitalize first letter for consistency
                        if (!shift.isEmpty()) {
                            shift = shift.substring(0, 1).toUpperCase() + shift.substring(1).toLowerCase();
                        }
                        
                        // Conditional: Check if shift is valid
                        if (Arrays.asList(SHIFTS).contains(shift)) {
                            // Check if not already chosen for this day
                            if (availableShifts.contains(shift)) {
                                shiftPriorities.add(shift);
                                availableShifts.remove(shift);  // Remove from available
                                break;
                            } else {
                                System.out.println("      " + shift + " already chosen. Pick a different shift.");
                            }
                        } else {
                            System.out.println("      Invalid shift. Choose from: " + String.join(", ", SHIFTS));
                        }
                    }
                }
                
                // Store this day's priority-ranked preferences
                preferences.put(day, shiftPriorities);
            }
            
            // Add employee and their preferences to scheduler
            scheduler.addEmployee(name, preferences);
        }
        
        // ========== PROCESS SCHEDULING ==========
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Processing schedule...");
        // Call main scheduling algorithm
        scheduler.assignShifts();
        
        // ========== DISPLAY RESULTS ==========
        scheduler.displaySchedule();
        
        // Close scanner to prevent resource leak
        scanner.close();
    }
}
