package com.purbarun.pipeline;

import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.pipeline.DataPipeline;
import com.purbarun.pipeline.util.DataLoader;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Data Pipeline Processor — V1 (Naive) ===\n");

        List<Employee>           employees   = DataLoader.generateEmployees(10_000);
        Map<Integer, Department> departments = DataLoader.getDepartments();

        System.out.println("Loaded " + employees.size() + " employees");
        System.out.println("Departments: " + departments.size() + "\n");

        DataPipeline pipeline = new DataPipeline();

        List<DepartmentSummary> summaries = pipeline.execute(
                employees,
                true,       // onlyActive
                50_000.0,   // salaryThreshold
                departments,
                "tenant-001"
        );

        String report = pipeline.generateReport(summaries);
        System.out.println("\n" + report);
    }
}
