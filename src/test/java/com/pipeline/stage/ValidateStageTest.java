package com.pipeline.stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.stage.ValidateStage;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// # ValidateStageTest — unit tests for ValidateStage
///
/// Verifies that business-rule violations (zero/negative salary, invalid department id,
/// blank name) are caught by the stage, and that structural violations (null name, non-positive id)
/// are rejected at construction time by the V2 `Employee` record.
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
        /// V2: record accessor `name()` replaces `getName()`
        assertEquals("Bob", result.get(0).name());
    }

    @Test
    void shouldRemoveEmployeeWithNegativeSalary() {
        List<Employee> employees = List.of(new Employee(1, "Alice", -500, 1, true));
        List<Employee> result = validateStage.process(employees);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRemoveEmployeeWithInvalidDepartmentId() {
        /// `departmentId=0` is allowed at construction (business rule, not structural invariant).
        /// `ValidateStage` enforces the `departmentId > 0` business rule.
        List<Employee> employees = List.of(new Employee(1, "Alice", 80_000, 0, true));
        List<Employee> result = validateStage.process(employees);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenConstructedWithNullName() {
        /// V2: `Employee` record compact constructor enforces `name != null`.
        /// Null is rejected at construction time — fail-fast, no silent drop buried in a catch block.
        ///
        /// V1 flaw: null name caused NPE swallowed by `catch(Exception e) { e.printStackTrace(); }`
        /// giving zero traceability. V2 makes it impossible to construct such an object.
        assertThrows(NullPointerException.class,
                () -> new Employee(1, null, 80_000, 1, true));
    }

    @Test
    void shouldRemoveEmployeeWithBlankName() {
        /// Blank name is a **business-rule** violation — `ValidateStage` catches it.
        /// (The record allows blank strings; only null is rejected at construction.)
        List<Employee> employees = Arrays.asList(
                new Employee(1, "  ", 80_000, 1, true),  // blank name
                new Employee(2, "Bob", 60_000, 2, true)
        );
        List<Employee> result = validateStage.process(employees);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).name());
    }

    @Test
    void shouldHandleEmptyInputGracefully() {
        List<Employee> result = validateStage.process(List.of());
        assertTrue(result.isEmpty());
    }
}
