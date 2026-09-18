package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.Employee;

/// # EnrichStage — V2 (Step 2)
///
/// - `emp.withDepartmentName(deptName)` replaces `emp.setDepartmentName(deptName)`.
///   The original `Employee` record is untouched; a new immutable copy is added to the result.
///   Multiple stages can now safely hold references to the same `Employee` — no shared mutation.
/// - Record accessors used: `emp.departmentId()`, `dept.name()`
///
/// The null-check nesting ("Pyramid of Doom") remains here — eliminated in Step 6 via `Optional<T>`.
/// `departmentMap` is still passed as a method parameter — replaced by Scoped Values in Step 5.
/// `EnrichStage` will fully implement `PipelineStage<Employee>` after those two refactors.
public class EnrichStage {

    /// Enriches each employee with the department name looked up from `departmentMap`.
    ///
    /// @param employees      source list
    /// @param departmentMap  lookup table keyed by department id
    /// @return enriched list (new immutable record copies; originals are untouched)
    public List<Employee> process(List<Employee> employees, Map<Integer, Department> departmentMap) {
        List<Employee> result = new ArrayList<>();

        for (Employee emp : employees) {
            if (emp != null) {
                String deptName = "Unknown";
                if (departmentMap != null) {
                    Department dept = departmentMap.get(emp.departmentId());
                    if (dept != null && dept.name() != null) {
                        deptName = dept.name();
                    }
                }
                /// Immutable update: returns a new Employee record with departmentName set
                result.add(emp.withDepartmentName(deptName));
            }
        }

        return result;
    }
}
