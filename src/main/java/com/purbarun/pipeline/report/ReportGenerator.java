package com.purbarun.pipeline.report;

import java.util.Date;
import java.util.List;

import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;

/// # ReportGenerator — V2 (Step 2)
///
/// Uses record accessors throughout (no `get` prefix):
/// - `summary.departmentName()` replaces `summary.getDepartmentName()`
/// - `summary.employeeCount()`  replaces `summary.getEmployeeCount()`
/// - `summary.averageSalary()`  replaces `summary.getAverageSalary()` … etc.
/// - `emp.id()`, `emp.name()`, `emp.salary()`, `emp.departmentName()`
///
/// `String +=` and `String.format()` are intentionally unchanged here —
/// they are the subject of the Step 7 benchmark (V1: O(n²) → V2: O(n) `StringBuilder`).
public class ReportGenerator {

    /// Generates a concise per-department salary summary report.
    ///
    /// @param summaries aggregated department data
    /// @return formatted multi-line report string
    public String generateSummaryReport(List<DepartmentSummary> summaries) {
        String report = "";
        report += "=== DEPARTMENT SALARY REPORT ===\n";
        report += "Generated : " + new Date() + "\n";
        report += "Departments: " + summaries.size() + "\n";
        report += "--------------------------------\n";

        for (DepartmentSummary summary : summaries) {
            report += "\nDepartment : " + summary.departmentName() + "\n";
            report += "  Employees : " + summary.employeeCount() + "\n";
            report += "  Avg Salary: $" + String.format("%.2f", summary.averageSalary()) + "\n";
            report += "  Min Salary: $" + String.format("%.2f", summary.minSalary()) + "\n";
            report += "  Max Salary: $" + String.format("%.2f", summary.maxSalary()) + "\n";
            report += "  Total Pay : $" + String.format("%.2f", summary.totalSalary()) + "\n";
        }

        report += "--------------------------------\n";
        report += "=== END OF REPORT ===\n";
        return report;
    }

    /// Generates a detailed per-employee CSV-style report.
    ///
    /// **V1 Naive:** `String +=` in a loop — O(n²) object creation.
    /// Each iteration allocates a new `String`, copies all previous characters, then discards the old one.
    /// On 10K records this produces ~1.65 billion character copies (~3.3 GB byte traffic).
    ///
    /// **Step 7 target:** replace with `StringBuilder` → expected **~10–15× speedup**.
    ///
    /// @param employees enriched and validated employee list
    /// @return CSV-formatted report string
    public String generateDetailedReport(List<Employee> employees) {
        String report = "";
        for (Employee emp : employees) {
            /// Every iteration: Java allocates a new String, copies everything before it, then discards the old one
            report += emp.id() + "," + emp.name() + ","
                    + String.format("%.2f", emp.salary()) + ","
                    + emp.departmentName() + "\n";
        }
        return report;
    }
}
