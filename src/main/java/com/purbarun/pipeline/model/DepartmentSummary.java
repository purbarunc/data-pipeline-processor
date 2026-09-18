package com.purbarun.pipeline.model;

/// # DepartmentSummary — V2 Improved (Step 2, Java 25)
///
/// **`record`** (finalized Java 16) replaces the mutable POJO.
///
/// V1's `AggregateStage` built `DepartmentSummary` incrementally via six setter calls —
/// a partially-initialised object existed in heap throughout the computation loop.
///
/// V2: `AggregateStage` computes ALL stats first, then constructs the record in a single
/// atomic call. The moment this object exists it is valid and complete.
///
/// Generated accessors: `departmentId()`, `departmentName()`, `employeeCount()`,
/// `averageSalary()`, `minSalary()`, `maxSalary()`, `totalSalary()`
public record DepartmentSummary(
        int    departmentId,
        String departmentName,
        int    employeeCount,
        double averageSalary,
        double minSalary,
        double maxSalary,
        double totalSalary) {

    /// Compact canonical constructor — rejects a negative employee count.
    public DepartmentSummary {
        if (employeeCount < 0) {
            throw new IllegalArgumentException("employeeCount must be >= 0, got: " + employeeCount);
        }
    }
}
