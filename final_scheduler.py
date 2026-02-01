import random
from collections import defaultdict

class EmployeeScheduler:
    """
    Employee shift scheduling system with conflict resolution and priority preferences.
    
    This class manages employee schedules for a company operating 7 days a week
    with three shifts per day (Morning, Afternoon, Evening). It handles employee
    preferences with priority ranking, enforces scheduling constraints, and resolves conflicts.
    
    BONUS FEATURE: Supports priority ranking for shift preferences (1st, 2nd, 3rd choice per day)
    """
    
    # Class constants defining scheduling parameters
    DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    SHIFTS = ["Morning", "Afternoon", "Evening"]
    MAX_DAYS_PER_WEEK = 5  # Maximum days an employee can work per week
    MIN_EMPLOYEES_PER_SHIFT = 2  # Minimum employees required per shift
    
    def __init__(self):
        """
        Initialize the scheduler with empty data structures.
        
        Data structures:
        - employees: List of employee names
        - preferences: Dictionary mapping employee names to their shift preferences with priorities
        - schedule: Nested dictionary organizing shifts by day and shift type
        - employee_days_worked: Counter tracking days worked per employee
        """
        self.employees = []  # List to store all employee names
        # Dictionary: {employee_name: {day: [shift_priority_list]}}
        # Example: {"Alice": {"Monday": ["Morning", "Afternoon", "Evening"]}}
        self.preferences = {}
        # Nested dictionary structure: {day: {shift: [employees]}}
        self.schedule = {day: {shift: [] for shift in self.SHIFTS} for day in self.DAYS}
        self.employee_days_worked = defaultdict(int)  # Tracks days worked per employee
    
    def add_employee(self, name, shift_preferences):
        """
        Add an employee with their shift preferences to the system.
        
        This method stores employee information for later scheduling.
        Uses conditional logic to prevent duplicate entries.
        
        BONUS FEATURE: Supports priority-ranked preferences per day
        
        Args:
            name: Employee name (string)
            shift_preferences: Dictionary mapping day names to list of preferred shifts in priority order
                              Example: {"Monday": ["Morning", "Afternoon", "Evening"]}
                              First item is 1st choice, second is 2nd choice, third is 3rd choice
        """
        # Conditional: Only add if employee doesn't already exist
        if name not in self.employees:
            self.employees.append(name)  # Add to employee list
            self.preferences[name] = shift_preferences  # Store their preferences
    
    def assign_shifts(self):
        """
        Main scheduling logic with conflict resolution and priority preferences.
        
        This method implements a three-pass algorithm:
        1. First pass: Assign employees to their preferred shifts (trying priorities in order)
        2. Second pass: Fill understaffed shifts with available employees
        3. Third pass: Resolve remaining conflicts using priority preferences
        
        Uses nested loops to iterate through employees and days, with
        conditionals to enforce scheduling constraints.
        
        BONUS FEATURE: Tries to assign shifts based on priority ranking (1st, 2nd, 3rd choice)
        """
        # FIRST PASS: Assign preferred shifts using priority ranking
        # Outer loop: Iterate through each employee
        for employee in self.employees:
            # Inner loop: Check each day of the week
            for day in self.DAYS:
                # Conditional: Check if employee has reached maximum work days
                if self.employee_days_worked[employee] >= self.MAX_DAYS_PER_WEEK:
                    break  # Break out of day loop if max days reached
                
                # Conditional: Check if employee has a preference for this day
                if day in self.preferences[employee]:
                    # Get priority-ranked list of shifts for this day
                    preferred_shifts = self.preferences[employee][day]
                    
                    # Check if employee is already scheduled this day (any shift)
                    # Uses list comprehension to check all shifts for the day
                    already_scheduled = any(employee in self.schedule[day][shift] 
                                          for shift in self.SHIFTS)
                    
                    # Conditional: Only schedule if not already working this day
                    if not already_scheduled:
                        # BONUS: Try shifts in priority order (1st choice, then 2nd, then 3rd)
                        assigned = False
                        for preferred_shift in preferred_shifts:
                            # Try to assign to this priority shift
                            self.schedule[day][preferred_shift].append(employee)
                            # Increment the counter for days worked
                            self.employee_days_worked[employee] += 1
                            assigned = True
                            break  # Successfully assigned, move to next day
                        
                        # If no priority worked, this shouldn't happen with 3 shifts,
                        # but we break to be safe
                        if not assigned:
                            break
        
        # SECOND PASS: Ensure minimum coverage for all shifts
        self._ensure_minimum_coverage()
        
        # THIRD PASS: Resolve any remaining conflicts using priority preferences
        self._resolve_conflicts()
    
    def _ensure_minimum_coverage(self):
        """
        Ensure each shift has minimum required employees.
        
        This method uses nested loops to check all shifts and automatically
        assigns available employees to understaffed shifts. Uses while loop
        to continue assigning until minimum coverage is met or no employees
        are available.
        """
        # Outer loop: Iterate through each day
        for day in self.DAYS:
            # Inner loop: Check each shift within the day
            for shift in self.SHIFTS:
                # Get current number of employees assigned to this shift
                current_count = len(self.schedule[day][shift])
                
                # While loop: Continue until minimum coverage is met
                while current_count < self.MIN_EMPLOYEES_PER_SHIFT:
                    assigned = False  # Flag to track if we assigned someone
                    
                    # Loop through all employees to find someone available
                    for employee in self.employees:
                        # Conditional: Check if employee can work more days
                        if self.employee_days_worked[employee] >= self.MAX_DAYS_PER_WEEK:
                            continue  # Skip this employee, move to next
                        
                        # Check if employee is already scheduled for this day
                        already_scheduled = any(employee in self.schedule[day][s] 
                                              for s in self.SHIFTS)
                        if already_scheduled:
                            continue  # Skip if already working this day
                        
                        # Conditional: Check if employee is not in this shift yet
                        if employee not in self.schedule[day][shift]:
                            # Assign employee to this shift
                            self.schedule[day][shift].append(employee)
                            self.employee_days_worked[employee] += 1
                            current_count += 1
                            assigned = True
                            break  # Exit employee loop, got someone
                    
                    # Conditional: If no one was assigned, exit while loop
                    # This prevents infinite loop when no employees available
                    if not assigned:
                        break
    
    def _resolve_conflicts(self):
        """
        Resolve scheduling conflicts by reassigning employees using priority preferences.
        
        This method handles cases where employees couldn't get their preferred
        shifts by attempting to schedule them using their ranked preferences.
        Uses set operations to efficiently find scheduling gaps.
        
        BONUS FEATURE: Uses priority ranking to find best alternate shifts
        """
        # Loop through each employee to check for conflicts
        for employee in self.employees:
            # Get all days this employee wanted to work
            preferred_days = set(self.preferences[employee].keys())
            
            # Find which days they're actually scheduled
            scheduled_days = set()
            # Nested loop: Check all days and shifts
            for day in self.DAYS:
                for shift in self.SHIFTS:
                    # Conditional: If employee is in this shift, mark day as scheduled
                    if employee in self.schedule[day][shift]:
                        scheduled_days.add(day)
            
            # Set operation: Find days they wanted but didn't get
            missing_days = preferred_days - scheduled_days
            
            # Loop through days that need resolution
            for day in missing_days:
                # Conditional: Check if employee has capacity for more days
                if self.employee_days_worked[employee] >= self.MAX_DAYS_PER_WEEK:
                    break  # Employee at max capacity, stop trying
                
                # Check if they're already scheduled this day
                already_scheduled = day in scheduled_days
                # Conditional: Only try to schedule if not already working
                if not already_scheduled:
                    # BONUS: Try shifts in priority order for this day
                    priority_shifts = self.preferences[employee].get(day, self.SHIFTS)
                    for shift in priority_shifts:
                        # Conditional: Check if employee not already in this shift
                        if employee not in self.schedule[day][shift]:
                            # Assign to this shift (respecting priority if available)
                            self.schedule[day][shift].append(employee)
                            self.employee_days_worked[employee] += 1
                            scheduled_days.add(day)
                            break  # Found a shift, move to next day
    
    def display_schedule(self):
        """
        Display the final schedule in a readable format.
        
        Uses formatted string output to create a professional-looking
        schedule display with clear sections and alignment.
        """
        # Print header with decorative borders
        print("\n" + "="*80)
        print("EMPLOYEE SCHEDULE FOR THE WEEK".center(80))
        print("="*80 + "\n")
        
        # Loop through each day to display schedule
        for day in self.DAYS:
            print(f"\n{day.upper()}")  # Day header in uppercase
            print("-" * 60)  # Separator line
            
            # Loop through each shift for this day
            for shift in self.SHIFTS:
                employees = self.schedule[day][shift]
                # Conditional: Display employee list or "No employees assigned"
                employee_list = ", ".join(employees) if employees else "No employees assigned"
                # Formatted output with alignment
                print(f"  {shift:12} : {employee_list}")
        
        # Print employee summary section
        print("\n" + "="*80)
        print("EMPLOYEE WORK SUMMARY".center(80))
        print("="*80 + "\n")
        
        # Loop through employees in sorted order
        for employee in sorted(self.employees):
            days_worked = self.employee_days_worked[employee]
            # Formatted output showing days worked per employee
            print(f"  {employee:20} : {days_worked} days")
        
        print("\n" + "="*80 + "\n")


def main():
    """
    Main function to run the scheduler with user input.
    
    This function handles all user interaction, input validation,
    and orchestrates the scheduling process. Demonstrates extensive
    use of loops and conditionals for input handling.
    """
    # Create scheduler instance
    scheduler = EmployeeScheduler()
    
    print("Employee Shift Scheduling System")
    print("=" * 60)
    
    # INPUT VALIDATION: Get number of employees
    # While loop continues until valid input received
    while True:
        try:
            num_employees = int(input("\nHow many employees to schedule? (3-10 recommended): "))
            # Conditional: Validate minimum number
            if num_employees < 1:
                print("Please enter at least 1 employee.")
                continue  # Return to start of while loop
            break  # Exit loop if valid input
        except ValueError:
            # Exception handling for non-integer input
            print("Invalid input. Please enter a number.")
    
    # LOOP: Collect information for each employee
    for i in range(num_employees):
        print(f"\n--- Employee {i+1} ---")
        name = input("Enter employee name: ").strip()
        
        # While loop: Ensure name is not empty
        while not name:
            print("Name cannot be empty.")
            name = input("Enter employee name: ").strip()
        
        # Display instructions
        print(f"\nEnter shift preferences for {name}")
        print("Available days: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday")
        print("Available shifts: Morning, Afternoon, Evening")
        print("*** BONUS FEATURE: You can rank your shift preferences (1st, 2nd, 3rd choice) ***")
        
        # Dictionary to store this employee's preferences
        preferences = {}
        
        # INPUT VALIDATION: Get number of days employee wants to work
        while True:
            num_prefs = input(f"How many days does {name} want to work? (1-5): ").strip()
            try:
                num_prefs = int(num_prefs)
                # Conditional: Validate range (1-5 days)
                if 1 <= num_prefs <= 5:
                    break  # Valid input, exit loop
                print("Please enter a number between 1 and 5.")
            except ValueError:
                # Exception handling for non-integer input
                print("Invalid input. Please enter a number.")
        
        # LOOP: Collect preferences for each day
        for j in range(num_prefs):
            # INPUT VALIDATION: Get valid day
            while True:
                day = input(f"  Day {j+1} (e.g., Monday): ").strip().capitalize()
                # Conditional: Check if day is valid
                if day in scheduler.DAYS:
                    # Conditional: Check for duplicate days
                    if day not in preferences:
                        break  # Valid and not duplicate, exit loop
                    print(f"{day} already entered. Choose a different day.")
                else:
                    print(f"Invalid day. Choose from: {', '.join(scheduler.DAYS)}")
            
            # BONUS FEATURE: Collect ranked preferences for this day
            print(f"    Enter your shift preferences for {day} in priority order:")
            shift_priorities = []
            available_shifts = list(scheduler.SHIFTS)
            
            # Collect 1st, 2nd, and 3rd choice
            for priority in ["1st", "2nd", "3rd"]:
                if not available_shifts:
                    break
                    
                while True:
                    shift = input(f"      {priority} choice (Morning/Afternoon/Evening): ").strip().capitalize()
                    # Conditional: Check if shift is valid and not already chosen
                    if shift in scheduler.SHIFTS:
                        if shift in available_shifts:
                            shift_priorities.append(shift)
                            available_shifts.remove(shift)  # Remove from available choices
                            break
                        else:
                            print(f"      {shift} already chosen. Pick a different shift.")
                    else:
                        print(f"      Invalid shift. Choose from: {', '.join(scheduler.SHIFTS)}")
            
            # Store this day's priority-ranked preferences
            preferences[day] = shift_priorities
        
        # Add employee and their preferences to scheduler
        scheduler.add_employee(name, preferences)
    
    # Process scheduling with all collected data
    print("\n" + "="*60)
    print("Processing schedule...")
    scheduler.assign_shifts()
    
    # Display final results
    scheduler.display_schedule()


# Entry point: Run main function if script is executed directly
if __name__ == "__main__":
    main()
