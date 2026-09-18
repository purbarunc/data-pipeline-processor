package com.purbarun.pipeline.pipeline;

import java.util.Map;

import com.purbarun.pipeline.model.Department;

/// # PipelineContext — V1 Naive (ThreadLocal-based context sharing)
///
/// ## The V1 bug
///
/// `ThreadLocal` values survive task completion in a thread pool.
/// If this pipeline runs inside an `ExecutorService` and `clear()` is not called,
/// the next task that runs on the same thread **inherits the previous tenant's
/// department map and salary threshold** — a silent data leak across tenants.
///
/// ## V2 fix (Step 5)
///
/// Replace `ThreadLocal` with **Scoped Values** (JEP 506, finalized Java 25).
/// Scoped Values are automatically cleaned up when the scope exits — no leak possible,
/// no explicit `clear()` required, and the value is immutable within its scope.
public class PipelineContext {

    private static final ThreadLocal<Map<Integer, Department>> departmentMapHolder    = new ThreadLocal<>();
    private static final ThreadLocal<String>                   tenantIdHolder         = new ThreadLocal<>();
    private static final ThreadLocal<Double>                   salaryThresholdHolder  = new ThreadLocal<>();

    /// Stores pipeline configuration in thread-local slots.
    ///
    /// **V1 flaw:** caller must remember to call `clear()` — not enforced by the compiler.
    /// Scoped Values (Step 5) make this impossible to forget.
    public static void set(Map<Integer, Department> deptMap, String tenantId, double salaryThreshold) {
        departmentMapHolder.set(deptMap);
        tenantIdHolder.set(tenantId);
        salaryThresholdHolder.set(salaryThreshold);
    }

    /// Returns the department map stored for the current thread.
    public static Map<Integer, Department> getDepartmentMap() {
        return departmentMapHolder.get();
    }

    /// Returns the tenant id stored for the current thread.
    public static String getTenantId() {
        return tenantIdHolder.get();
    }

    /// Returns the salary threshold stored for the current thread.
    public static double getSalaryThreshold() {
        return salaryThresholdHolder.get();
    }

    /// Removes all thread-local values for the current thread.
    ///
    /// **Must be called after every pipeline run** — but in V1's `DataPipeline` it is NOT called,
    /// which is the exact bug this class documents. Scoped Values in Step 5 eliminate the need
    /// for this method entirely.
    public static void clear() {
        departmentMapHolder.remove();
        tenantIdHolder.remove();
        salaryThresholdHolder.remove();
    }
}
