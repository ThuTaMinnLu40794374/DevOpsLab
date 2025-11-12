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
        Employee emp = app.getEmployee(255530);
        assertEquals(255530, emp.emp_no);
        assertEquals("Ronghao", emp.first_name);
        assertEquals("Garigliano", emp.last_name);
    }


}