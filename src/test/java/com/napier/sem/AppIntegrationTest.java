package com.napier.sem;

import com.napier.sem.App;
import com.napier.sem.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppIntegrationTest {

    App app;

    @BeforeEach
    void setUp() {
        app = new App();
        app.connect("localhost:33060", 10000);
    }

    @Test
    void testGetEmployee() {
        Employee emp = app.getEmployee(String.valueOf(255530));
        assertEquals(255530, emp.emp_no);
        assertEquals("Ronghao", emp.first_name);
        assertEquals("Garigliano", emp.last_name);
    }

    @Test
    void testAddEmployee() {

        // Always clean old record so INSERT won’t fail
        app.deleteEmployee(500002);

        Employee emp = new Employee();
        emp.emp_no = 500002;
        emp.first_name = "Wana";
        emp.last_name = "Fat";

        app.addEmployee(emp);

        emp = app.getEmployee(String.valueOf(500002));
        assertNotNull(emp, "Employee not found — insert failed");

        assertEquals(500002, emp.emp_no);
        assertEquals("Wana", emp.first_name);
        assertEquals("Fat", emp.last_name);
    }





}