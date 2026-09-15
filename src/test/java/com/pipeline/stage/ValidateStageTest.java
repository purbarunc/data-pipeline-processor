package com.pipeline.stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.stage.ValidateStage;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidateStageTest {

    private ValidateStage validateStage;

    @BeforeEach
    void setUp() {
        validateStage = new ValidateStage();
    }

    @Test
    void shouldRetainFullyValidEmployees() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", 80_000, 1, true),
                new Employee(2, "Bob",   60_000, 2, true)
        );
        List<Employee> result = validateStage.process(employees);
        assertEquals(2, result.size());
    }

    @Test
    void shouldRemoveEmployeeWithZeroSalary() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", 0, 1, true),
                new Employee(2, "Bob", 60_000, 2, true)
        );
        List<Employee> result = validateStage.process(employees);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void shouldRemoveEmployeeWithNegativeSalary() {
        List<Employee> employees = List.of(new Employee(1, "Alice", -500, 1, true));
        List<Employee> result = validateStage.process(employees);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRemoveEmployeeWithInvalidDepartmentId() {
        List<Employee> employees = List.of(new Employee(1, "Alice", 80_000, 0, true));
        List<Employee> result = validateStage.process(employees);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSilentlyDropEmployeeWithNullName() {
        // V1 FLAW DOCUMENTED: null name causes NullPointerException inside the catch block.
        // The exception is swallowed by catch(Exception e) { e.printStackTrace(); }.
        // The employee is silently dropped — no way for the caller to know this happened.
        // In V2 this will throw a typed ValidationException with the employee ID and root cause.
        Employee nullNameEmp = new Employee(1, null, 80_000, 1, true);
        List<Employee> employees = Arrays.asList(
                nullNameEmp,
                new Employee(2, "Bob", 60_000, 2, true)
        );
        List<Employee> result = validateStage.process(employees);
        assertEquals(1, result.size(), "Null-name employee should be silently dropped (V1 flaw)");
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void shouldHandleEmptyInputGracefully() {
        List<Employee> result = validateStage.process(List.of());
        assertTrue(result.isEmpty());
    }
}
