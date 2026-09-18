package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.List;

import com.purbarun.pipeline.model.Employee;

/// # ValidateStage — V2 (Step 2)
///
/// - Implements `PipelineStage<Employee>` — the generic contract is now compile-time enforced.
///   It is impossible to wire this stage to a `List<Department>` without a compiler error.
/// - Record accessors used: `emp.name()`, `emp.salary()`, `emp.departmentId()`
/// - `emp.name().isBlank()` replaces `emp.getName().trim().isEmpty()`:
///   - `isBlank()` is the correct Java 11+ idiom for whitespace-only strings
///   - `NullPointerException` on `name` is now **impossible** — the `Employee` record compact
///     constructor guarantees `name != null` for every instance that was successfully built.
///
/// The `catch(Exception e)` swallower and the unreported `invalid` list remain as V1 flaws.
/// They are replaced by typed custom exceptions and exception chaining in Step 3.
public class ValidateStage implements PipelineStage<Employee> {

    @Override
    public List<Employee> process(List<Employee> employees) {
        List<Employee> valid   = new ArrayList<>();
        /// collected but never returned or reported — fixed in Step 3
        List<Employee> invalid = new ArrayList<>();

        for (Employee emp : employees) {
            try {
                /// no NPE possible — record compact constructor guarantees name != null
                if (emp.name().isBlank()) {
                    invalid.add(emp);
                    continue;
                }
                if (emp.salary() <= 0) {
                    invalid.add(emp);
                    continue;
                }
                if (emp.departmentId() <= 0) {
                    invalid.add(emp);
                    continue;
                }
                valid.add(emp);
            } catch (Exception e) {
                /// still V1-style — replaced by typed custom exceptions in Step 3
                e.printStackTrace();
            }
        }

        System.out.println("Validation: " + valid.size() + " valid, "
                + invalid.size() + " invalid (errors silently swallowed)");
        return valid;
    }
}
