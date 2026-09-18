package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;

/// # AggregateStage — V2 (Step 2)
///
/// - Record accessors used: `emp.departmentId()`, `emp.departmentName()`, `emp.salary()`
/// - `DepartmentSummary` is constructed **atomically**: all stats are computed first,
///   then the record is created in a single call. No partially-initialised object
///   ever exists in heap (contrast with V1's six-setter build-up).
/// - `var` (Java 10) used for local variables where the type is obvious from context.
///
/// The three-loop structure and the intermediate `Map<Integer, List<Employee>>` remain
/// as V1-style eager materialisation — replaced by `Stream.collect(groupingBy +
/// summarizingDouble)` in a single lazy pass in Step 4.
///
/// **Why `AggregateStage` does not implement `PipelineStage<T>`:**
/// `PipelineStage<T>` requires the same element type `T` for both input and output.
/// `AggregateStage.process()` takes `List<Employee>` and returns `List<DepartmentSummary>` —
/// different types in and out — so a single `T` cannot satisfy both ends.
/// Step 4/5 will introduce a separate `Transformer<I, O>` abstraction (or use a
/// `Collector`/`Gatherer`) to handle type-changing pipeline stages cleanly.
public class AggregateStage {

    /// Groups employees by department and computes per-department salary statistics.
    ///
    /// @param employees validated employee list
    /// @return one `DepartmentSummary` record per department, atomically constructed
    public List<DepartmentSummary> process(List<Employee> employees) {

        /// Loop 1: group by department — intermediate Map still allocated in heap (Step 4 target)
        Map<Integer, List<Employee>> byDepartment = new HashMap<>();
        for (Employee emp : employees) {
            int deptId = emp.departmentId();
            if (byDepartment.get(deptId) == null) {
                byDepartment.put(deptId, new ArrayList<>());
            }
            byDepartment.get(deptId).add(emp);
        }

        List<DepartmentSummary> summaries = new ArrayList<>();

        for (Map.Entry<Integer, List<Employee>> entry : byDepartment.entrySet()) {
            var deptId        = entry.getKey();
            var deptEmployees = entry.getValue();

            /// Compute all stats BEFORE constructing the record
            double totalSalary = 0;
            double minSalary   = Double.MAX_VALUE;
            double maxSalary   = Double.MIN_VALUE;
            String deptName    = deptEmployees.get(0).departmentName();

            for (Employee emp : deptEmployees) {
                totalSalary += emp.salary();
                if (emp.salary() < minSalary) minSalary = emp.salary();
                if (emp.salary() > maxSalary) maxSalary = emp.salary();
            }

            /// Atomic construction: DepartmentSummary record is valid the instant it exists
            summaries.add(new DepartmentSummary(
                    deptId,
                    deptName,
                    deptEmployees.size(),
                    totalSalary / deptEmployees.size(),
                    minSalary,
                    maxSalary,
                    totalSalary
            ));
        }

        return summaries;
    }
}
