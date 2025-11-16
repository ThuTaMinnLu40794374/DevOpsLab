package com.napier.sem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
     * Get employee details including department and manager.
     * @param empNo Employee number
     * @return Employee object with department and manager info
     */
    public Employee getEmployeeWithDeptAndManager(int empNo) {
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT e.emp_no, e.first_name, e.last_name, d.dept_name, dm.emp_no AS mgr_no, em.first_name AS mgr_first, em.last_name AS mgr_last " +
                            "FROM employees e " +
                            "JOIN dept_emp de ON e.emp_no = de.emp_no " +
                            "JOIN departments d ON de.dept_no = d.dept_no " +
                            "JOIN dept_manager dm ON d.dept_no = dm.dept_no " +
                            "JOIN employees em ON dm.emp_no = em.emp_no " +
                            "WHERE e.emp_no = " + empNo + " AND dm.to_date = '9999-01-01'";

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


    public Employee getEmployee(int empNo) {
        try {
            String sql = "SELECT emp_no, first_name, last_name FROM employees WHERE emp_no = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, empNo);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) return null;

            Employee emp = new Employee();
            emp.emp_no = rs.getInt("emp_no");
            emp.first_name = rs.getString("first_name");
            emp.last_name = rs.getString("last_name");

            rs.close();
            pstmt.close();
            return emp;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Get employee based on first name and last name.
     * @param first_name Employee first name
     * @param last_name Employee last name
     * @return Employee object or null if not found
     */
    public Employee getEmployeeByName(String first_name, String last_name) {
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


    public void deleteEmployee(int empNo) {
        try {
            String sql = "DELETE FROM employees WHERE emp_no = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, empNo);
            pstmt.executeUpdate();
            pstmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addEmployee(Employee emp) {
        try {
            String sql = "INSERT INTO employees (emp_no, first_name, last_name, birth_date, gender, hire_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, emp.emp_no);
            pstmt.setString(2, emp.first_name);
            pstmt.setString(3, emp.last_name);
            pstmt.setString(4, "1990-01-01");
            pstmt.setString(5, "M");
            pstmt.setString(6, "2020-01-01");

            pstmt.executeUpdate();
            pstmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to add employee");
        }
    }

    public ArrayList<Employee> getSalariesByRole(String role) {
        ArrayList<Employee> employees = new ArrayList<>();

        try {
            String sql =
                    "SELECT e.emp_no, e.first_name, e.last_name, " +
                            "t.title, s.salary, d.dept_name, " +
                            "m.emp_no AS mgr_no, m.first_name AS mgr_first, m.last_name AS mgr_last " +
                            "FROM employees e " +
                            "JOIN salaries s ON e.emp_no = s.emp_no AND s.to_date = '9999-01-01' " +
                            "JOIN titles t ON e.emp_no = t.emp_no AND t.to_date = '9999-01-01' " +
                            "JOIN dept_emp de ON e.emp_no = de.emp_no AND de.to_date = '9999-01-01' " +
                            "JOIN departments d ON de.dept_no = d.dept_no " +
                            "JOIN dept_manager dm ON d.dept_no = dm.dept_no AND dm.to_date = '9999-01-01' " +
                            "JOIN employees m ON dm.emp_no = m.emp_no " +
                            "WHERE t.title = ?";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Employee emp = new Employee();

                emp.emp_no = rs.getInt("emp_no");
                emp.first_name = rs.getString("first_name");
                emp.last_name = rs.getString("last_name");
                emp.title = rs.getString("title");
                emp.salary = rs.getInt("salary");
                emp.dept_name = rs.getString("dept_name");

                Employee manager = new Employee();
                manager.emp_no = rs.getInt("mgr_no");
                manager.first_name = rs.getString("mgr_first");
                manager.last_name = rs.getString("mgr_last");

                emp.manager = manager;

                employees.add(emp);
            }

            rs.close();
            pstmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return employees;
    }

    public void outputEmployees(ArrayList<Employee> employees, String filename) {
        if (employees == null) {
            System.out.println("No employees");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("| Emp No | First Name | Last Name | Title | Salary | Department | Manager |\n");
        sb.append("| --- | --- | --- | --- | --- | --- | --- |\n");

        for (Employee emp : employees) {
            if (emp == null) continue;

            String managerName = "";
            if (emp.manager != null) {
                managerName = emp.manager.first_name + " " + emp.manager.last_name;
            }

            sb.append("| " + emp.emp_no + " | " +
                    emp.first_name + " | " +
                    emp.last_name + " | " +
                    emp.title + " | " +
                    emp.salary + " | " +
                    emp.dept_name + " | " +
                    managerName + " |\n");
        }

        try {
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(
                    new File("./reports/" + filename)));
            writer.write(sb.toString());
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void main(String[] args) throws SQLException {
        App a = new App();

        if(args.length < 1){
            a.connect("localhost:33060", 30000);
        }else{
            a.connect(args[0], Integer.parseInt(args[1]));
        }

        ArrayList<Employee> managers = a.getSalariesByRole("Manager");

        a.outputEmployees(managers, "ManagerSalaries.md");



//        Employee emp = a.getEmployee(500002);
//        if(emp != null) {
//            System.out.println("Employee inserted: " + emp.first_name + " " + emp.last_name);
//        } else {
//            System.out.println("Employee not found in DB!");
//        }
//
//
//        // 1️⃣ Get basic employee info by ID
//        Employee empBasic = a.getEmployee(255530);
//        if (empBasic != null) {
//            System.out.println("\n=== Basic Employee Info ===");
//            System.out.println(empBasic.emp_no + " | " + empBasic.first_name + " " + empBasic.last_name);
//        } else {
//            System.out.println("Employee not found");
//        }
//
//        // 2️⃣ Get employee info with department and manager
//        Employee empWithDept = a.getEmployeeWithDeptAndManager(255530);
//        if (empWithDept != null) {
//            System.out.println("\n=== Employee Info with Department and Manager ===");
//            System.out.println("Employee: " + empWithDept.emp_no + " | " +
//                    empWithDept.first_name + " " + empWithDept.last_name);
//            System.out.println("Department: " + empWithDept.dept_name);
//            if (empWithDept.manager != null) {
//                System.out.println("Manager: " + empWithDept.manager.first_name + " " + empWithDept.manager.last_name);
//            }
//        } else {
//            System.out.println("Employee not found");
//        }
//
//        // 3️⃣ Get employee by first and last name
//        Employee empByName = a.getEmployeeByName("Ronghao", "Garigliano");
//        if (empByName != null) {
//            System.out.println("\n=== Employee Info by Name ===");
//            System.out.println(empByName.emp_no + " | " + empByName.first_name + " " + empByName.last_name);
//        } else {
//            System.out.println("Employee not found by name");
//        }
//
//        try {
//            // 2️⃣ Print all salaries for all employees
//            Statement stmt = a.con.createStatement();
//            String allEmpQuery = "SELECT emp_no, first_name, last_name, salary FROM employees " +
//                    "JOIN salaries USING(emp_no) " +
//                    "WHERE salaries.to_date = '9999-01-01' " +
//                    "ORDER BY emp_no ASC";
//            ResultSet rs = stmt.executeQuery(allEmpQuery);
//
//            ArrayList<Employee> allEmployees = new ArrayList<>();
//            while(rs.next()) {
//                Employee emp = new Employee();
//                emp.emp_no = rs.getInt("emp_no");
//                emp.first_name = rs.getString("first_name");
//                emp.last_name = rs.getString("last_name");
//                emp.salary = rs.getInt("salary");
//                allEmployees.add(emp);
//            }
//            System.out.println("=== All Employees and Salaries ===");
//            a.printSalaries(allEmployees);
//
//            rs.close();
//            stmt.close();
//
//            // 3️⃣ Get Development Department info
//            Department devDept = a.getDepartment("Development");
//            if(devDept != null) {
//                System.out.println("\n=== Development Department Manager ===");
//                System.out.println("Department: " + devDept.dept_name +
//                        " | Manager: " + devDept.manager.first_name + " " + devDept.manager.last_name);
//
//                // 4️⃣ Get all employees and salaries for Development Department
//                ArrayList<Employee> devEmployees = a.getSalariesByDepartment(devDept);
//                System.out.println("\n=== Employees and Salaries for Development Department ===");
//                a.printSalaries(devEmployees);
//            } else {
//                System.out.println("Development Department not found!");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Disconnect
        a.disconnect();
    }
}
