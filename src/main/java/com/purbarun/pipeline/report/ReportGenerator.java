package com.purbarun.pipeline.report;

import java.util.Date;
import java.util.List;

import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;

// V1 NAIVE: Uses String += in a loop — O(n²) object creation.
// Each += creates a brand-new String object on the heap and copies all previous content.
// For n lines: 1 + 2 + 3 + ... + n = n*(n+1)/2 copy operations.
// On 10,000 records this produces ~4 seconds of GC pressure vs ~80ms with StringBuilder.
// V2 will replace with StringBuilder (generateSummaryReport) and String.formatted() (Java 15+).
public class ReportGenerator {

    public String generateSummaryReport(List<DepartmentSummary> summaries) {
        String report = "";
        report += "=== DEPARTMENT SALARY REPORT ===\n";
        report += "Generated : " + new Date() + "\n";
        report += "Departments: " + summaries.size() + "\n";
        report += "--------------------------------\n";

        for (DepartmentSummary summary : summaries) {
            report += "\nDepartment : " + summary.getDepartmentName() + "\n";
            report += "  Employees : " + summary.getEmployeeCount() + "\n";
            report += "  Avg Salary: $" + String.format("%.2f", summary.getAverageSalary()) + "\n";
            report += "  Min Salary: $" + String.format("%.2f", summary.getMinSalary()) + "\n";
            report += "  Max Salary: $" + String.format("%.2f", summary.getMaxSalary()) + "\n";
            report += "  Total Pay : $" + String.format("%.2f", summary.getTotalSalary()) + "\n";
        }

        report += "--------------------------------\n";
        report += "=== END OF REPORT ===\n";
        return report;
    }

    // Used by Benchmark to demonstrate the String += heap-churn problem at scale.
    // Each call to this method with 10,000 employees creates ~50MB of short-lived String objects.
    public String generateDetailedReport(List<Employee> employees) {
        String report = "";
        for (Employee emp : employees) {
            // Every iteration: Java allocates a new String, copies everything before it, then discards the old one
            report += emp.getId() + "," + emp.getName() + ","
                    + String.format("%.2f", emp.getSalary()) + ","
                    + emp.getDepartmentName() + "\n";
        }
        return report;
    }
}
