package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.List;

import com.purbarun.pipeline.model.Employee;

/// # FilterStage — V2 (Step 2)
///
/// Uses immutable `Employee` record accessors:
/// - `emp.active()` replaces `emp.isActive()` — record components use the field name directly
/// - `emp.salary()` replaces `emp.getSalary()` — no `get` prefix; idiomatic Java record style
///
/// `FilterStage` will fully implement `PipelineStage<Employee>` in Step 5 when the hardcoded
/// parameters (`onlyActive`, `salaryThreshold`) are replaced by an injected `Predicate<Employee>`.
/// That refactor removes the need for callers to know which parameters to supply.
public class FilterStage {

    /// Filters the employee list by active status and salary threshold.
    ///
    /// @param employees        source list
    /// @param onlyActive       when `true`, inactive employees are excluded
    /// @param salaryThreshold  employees with salary strictly below this value are excluded
    /// @return filtered list (new instance; input is not modified)
    public List<Employee> process(List<Employee> employees, boolean onlyActive, double salaryThreshold) {
        /// eager intermediate list — replaced by lazy Stream in Step 4
        List<Employee> result = new ArrayList<>();

        for (Employee emp : employees) {
            if (onlyActive && !emp.active()) {
                continue;
            }
            if (emp.salary() < salaryThreshold) {
                continue;
            }
            result.add(emp);
        }

        return result;
    }
}
