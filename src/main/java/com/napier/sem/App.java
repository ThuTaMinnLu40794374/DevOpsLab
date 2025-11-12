package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

public class App {
    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect(String location, int delay) {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 15;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(delay);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://" + location
                                + "/employees?allowPublicKeyRetrieval=true&useSSL=false",
                        "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " +                                  Integer.toString(i));
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Gets a single department by name, including its manager.
     * @param dept_name Department name (e.g. "Sales")
     * @return Department object or null if not found
     */
    public Department getDepartment(String dept_name) {
        try {
            Statement stmt = con.createStatement();
            String query =
                    "SELECT d.dept_no, d.dept_name, e.emp_no, e.first_name, e.last_name " +
                            "FROM departments d " +
                            "JOIN dept_manager dm ON d.dept_no = dm.dept_no " +
                            "JOIN employees e ON dm.emp_no = e.emp_no " +
                            "WHERE d.dept_name = '" + dept_name + "' " +
                            "AND dm.to_date = '9999-01-01'";

            ResultSet rset = stmt.executeQuery(query);

            if (rset.next()) {
                Employee manager = new Employee();
                manager.emp_no = rset.getInt("emp_no");
                manager.first_name = rset.getString("first_name");
                manager.last_name = rset.getString("last_name");

                Department dept = new Department(
                        rset.getString("dept_no"),
                        rset.getString("dept_name"),
                        manager
                );
                return dept;
            } else {
                return null;
            }

        } catch (Exception e) {
            System.out.println("Error retrieving department: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets employee details including department and manager.
     * @param ID Employee number
     * @return Employee object with department and manager info
     */
    public Employee getEmployee(int ID) {
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT e.emp_no, e.first_name, e.last_name, d.dept_name, dm.emp_no AS mgr_no, em.first_name AS mgr_first, em.last_name AS mgr_last " +
                            "FROM employees e " +
                            "JOIN dept_emp de ON e.emp_no = de.emp_no " +
                            "JOIN departments d ON de.dept_no = d.dept_no " +
                            "JOIN dept_manager dm ON d.dept_no = dm.dept_no " +
                            "JOIN employees em ON dm.emp_no = em.emp_no " +
                            "WHERE e.emp_no = " + ID + " AND dm.to_date = '9999-01-01'";

            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.dept_name = rset.getString("dept_name");

                // Add manager as an Employee object
                Employee manager = new Employee();
                manager.emp_no = rset.getInt("mgr_no");
                manager.first_name = rset.getString("mgr_first");
                manager.last_name = rset.getString("mgr_last");
                emp.manager = manager;

                return emp;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Failed to get employee details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get employee based on first name and last name.
     * @param first_name Employee first name
     * @param last_name Employee last name
     * @return Employee object or null if not found
     */
    public Employee getEmployee(String first_name, String last_name) {
        try {
            Statement stmt = con.createStatement();
            String query =
                    "SELECT emp_no, first_name, last_name " +
                            "FROM employees " +
                            "WHERE first_name = '" + first_name + "' AND last_name = '" + last_name + "'";

            ResultSet rset = stmt.executeQuery(query);

            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                return emp;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error retrieving employee by name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets all current employees and salaries for a given department.
     * @param dept Department object returned by getDepartment()
     * @return ArrayList of Employee objects, or null if error
     */
    public ArrayList<Employee> getSalariesByDepartment(Department dept) {
        if (dept == null) {
            System.out.println("Department is null");
            return null;
        }

        try {
            Statement stmt = con.createStatement();
            String query =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary " +
                            "FROM employees, salaries, dept_emp, departments " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND employees.emp_no = dept_emp.emp_no " +
                            "AND dept_emp.dept_no = departments.dept_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "AND departments.dept_no = '" + dept.dept_no + "' " +
                            "ORDER BY employees.emp_no ASC";

            ResultSet rset = stmt.executeQuery(query);

            ArrayList<Employee> employees = new ArrayList<>();

            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }

            return employees;
        } catch (Exception e) {
            System.out.println("Error retrieving salaries by department: " + e.getMessage());
            return null;
        }
    }

    /**
     * Prints a list of employees.
     * @param employees The list of employees to print.
     */
    public void printSalaries(ArrayList<Employee> employees)
    {
        // Check employees is not null
        if (employees == null)
        {
            System.out.println("No employees");
            return;
        }
        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list
        for (Employee emp : employees)
        {
            if (emp == null)
                continue;
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    /**
     * Displays the details of a single employee.
     * @param emp The employee to display.
     */
    public void displayEmployee(Employee emp)
    {
        if (emp == null)
        {
            System.out.println("Employee is null");
            return;
        }

        // Print header
        System.out.println(String.format(
                "%-10s %-15s %-20s %-20s %-8s",
                "Emp No", "First Name", "Last Name", "Title", "Salary"
        ));

        // Safely handle null or empty values
        String firstName = (emp.first_name != null && !emp.first_name.isEmpty()) ? emp.first_name : "N/A";
        String lastName = (emp.last_name != null && !emp.last_name.isEmpty()) ? emp.last_name : "N/A";
        String title = (emp.title != null && !emp.title.isEmpty()) ? emp.title : "N/A";

        System.out.println(String.format(
                "%-10s %-15s %-20s %-20s %-8s",
                emp.emp_no, firstName, lastName, title, emp.salary
        ));
    }



    public static void main(String[] args) {
        App a = new App();

        if(args.length < 1){
            a.connect("localhost:33060", 30000);
        }else{
            a.connect(args[0], Integer.parseInt(args[1]));
        }

        Department dept = a.getDepartment("Development");
        ArrayList<Employee> employees = a.getSalariesByDepartment(dept);


        // Print salary report
        a.printSalaries(employees);

//        // 1️⃣ Get Department and Manager
//        Department dept = a.getDepartment("Sales");
//        if (dept != null) {
//            System.out.println("\nDepartment: " + dept.dept_name +
//                    " | Manager: " + dept.manager.first_name + " " + dept.manager.last_name);
//        }
//
//        // 2️⃣ Get Employees and Salaries for Department
//        ArrayList<Employee> employees = a.getSalariesByDepartment(dept);
//        a.printSalaries(employees);
//
//        // 3️⃣ Get Employee by ID and by Name
//        Employee emp = a.getEmployee(10001);
//        if (emp != null && emp.manager != null) {
//            System.out.println("\nEmployee: " + emp.first_name + " " + emp.last_name +
//                    " | Dept: " + emp.dept_name +
//                    " | Manager: " + emp.manager.first_name + " " + emp.manager.last_name);
//        }
//
//        Employee empByName = a.getEmployee("Georgi", "Facello");
//        if (empByName != null) {
//            System.out.println("Found by name: " + empByName.emp_no + " " + empByName.first_name + " " + empByName.last_name);
//        }

        a.disconnect();
    }
}
