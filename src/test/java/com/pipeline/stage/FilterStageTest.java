package com.pipeline.stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.stage.FilterStage;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterStageTest {

    private FilterStage filterStage;
    private List<Employee> employees;

    @BeforeEach
    void setUp() {
        filterStage = new FilterStage();
        employees = Arrays.asList(
                new Employee(1, "Alice",   80_000, 1, true),
                new Employee(2, "Bob",     40_000, 1, true),  // below threshold
                new Employee(3, "Charlie", 90_000, 2, false), // inactive
                new Employee(4, "Diana",   60_000, 2, true),
                new Employee(5, "Eve",     55_000, 3, false)  // inactive
        );
    }

    @Test
    void shouldFilterOutInactiveEmployeesWhenOnlyActiveIsTrue() {
        List<Employee> result = filterStage.process(employees, true, 0);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(Employee::isActive));
    }

    @Test
    void shouldFilterBySalaryThreshold() {
        List<Employee> result = filterStage.process(employees, false, 50_000);
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(e -> e.getSalary() >= 50_000));
    }

    @Test
    void shouldApplyBothActiveAndSalaryFilter() {
        List<Employee> result = filterStage.process(employees, true, 50_000);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.isActive() && e.getSalary() >= 50_000));
    }

    @Test
    void shouldReturnAllRecordsWhenNoFiltersApply() {
        List<Employee> result = filterStage.process(employees, false, 0);
        assertEquals(5, result.size());
    }

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        List<Employee> result = filterStage.process(List.of(), true, 50_000);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIncludeEmployeeAtExactThreshold() {
        // V1 uses strict less-than: salary < threshold means employees AT the threshold pass through.
        // e.g. salary=50_000, threshold=50_000 → 50_000 < 50_000 is false → employee is INCLUDED.
        List<Employee> result = filterStage.process(
                List.of(new Employee(1, "Alice", 50_000, 1, true)),
                false, 50_000);
        assertEquals(1, result.size(), "Employee at exact threshold should be included (V1 strict <)");
    }
}
