package com.pipeline.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.pipeline.DataPipeline;
import com.purbarun.pipeline.util.DataLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataPipelineTest {

    private DataPipeline pipeline;
    private Map<Integer, Department> departments;

    @BeforeEach
    void setUp() {
        pipeline    = new DataPipeline();
        departments = DataLoader.getDepartments();
    }

    @Test
    void shouldProduceOneSummaryPerDepartment() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice",   80_000, 1, true),
                new Employee(2, "Bob",     70_000, 2, true),
                new Employee(3, "Charlie", 60_000, 1, true)
        );
        List<DepartmentSummary> summaries = pipeline.execute(
                employees, false, 0, departments, "test-tenant");
        assertEquals(2, summaries.size());
    }

    @Test
    void shouldFilterOutInactiveEmployees() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", 80_000, 1, true),
                new Employee(2, "Bob",   70_000, 1, false) // inactive
        );
        List<DepartmentSummary> summaries = pipeline.execute(
                employees, true, 0, departments, "test-tenant");
        assertEquals(1, summaries.size());
        assertEquals(1, summaries.get(0).getEmployeeCount());
    }

    @Test
    void shouldReturnEmptyListWhenAllRecordsFilteredOut() {
        List<Employee> employees = List.of(
                new Employee(1, "Alice", 40_000, 1, true) // below threshold
        );
        List<DepartmentSummary> summaries = pipeline.execute(
                employees, true, 50_000.0, departments, "test-tenant");
        assertTrue(summaries.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        List<DepartmentSummary> summaries = pipeline.execute(
                List.of(), true, 50_000.0, departments, "test-tenant");
        assertTrue(summaries.isEmpty());
    }

    @Test
    void shouldCalculateCorrectAverageSalary() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", 80_000, 1, true),
                new Employee(2, "Bob",   60_000, 1, true)
        );
        List<DepartmentSummary> summaries = pipeline.execute(
                employees, false, 0, departments, "test-tenant");
        assertEquals(1, summaries.size());
        DepartmentSummary dept = summaries.get(0);
        assertEquals(70_000, dept.getAverageSalary(), 0.01);
        assertEquals(60_000, dept.getMinSalary(),     0.01);
        assertEquals(80_000, dept.getMaxSalary(),     0.01);
        assertEquals(140_000, dept.getTotalSalary(),  0.01);
    }

    @Test
    void shouldEnrichDepartmentNameFromDepartmentMap() {
        List<Employee> employees = List.of(
                new Employee(1, "Alice", 80_000, 1, true) // deptId=1 => "Engineering"
        );
        List<DepartmentSummary> summaries = pipeline.execute(
                employees, false, 0, departments, "test-tenant");
        assertEquals("Engineering", summaries.get(0).getDepartmentName());
    }
}
