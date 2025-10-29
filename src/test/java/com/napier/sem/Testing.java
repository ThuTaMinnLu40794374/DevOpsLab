package com.napier.sem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class Testing
{
    static App app;

    @BeforeAll
    static void init()
    {
        app = new App();
    }

    @Test
    void printSalariesTestNull()
    {
        app.printSalaries(null);
    }

    @Test
    void printSalariesTestEmpty()
    {
        ArrayList<Employee> employess = new ArrayList<Employee>();
        app.printSalaries(employess);
    }

    @Test
    void printSalariesTestContainsNull()
    {
        ArrayList<Employee> employess = new ArrayList<Employee>();
        employess.add(null);
        app.printSalaries(employess);
    }

    @Test
    void printSalaries()
    {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        Employee emp = new Employee();
        emp.emp_no = 1;
        emp.first_name = "Kevin";
        emp.last_name = "Chalmers";
        emp.title = "Engineer";
        emp.salary = 55000;
        employees.add(emp);
        app.printSalaries(employees);
    }

    // ---------------------------
    // New displayEmployee tests
    // ---------------------------

    @Test
    void displayEmployeeTestNull()
    {
        app.displayEmployee(null);
    }

    @Test
    void displayEmployeeTestEmpty()
    {
        Employee emp = new Employee();
        emp.emp_no = 0;
        emp.first_name = "";
        emp.last_name = "";
        emp.title = "";
        emp.salary = 0;
        app.displayEmployee(emp);
    }

    @Test
    void displayEmployeeTestValid()
    {
        Employee emp = new Employee();
        emp.emp_no = 1;
        emp.first_name = "Alice";
        emp.last_name = "Smith";
        emp.title = "Developer";
        emp.salary = 60000;
        app.displayEmployee(emp);
    }

    @Test
    void displayEmployeeTestMissingFields()
    {
        Employee emp = new Employee();
        emp.emp_no = 2;
        emp.first_name = "Bob";
        // last_name intentionally missing
        emp.title = null;
        emp.salary = 0;
        app.displayEmployee(emp);
    }

}

