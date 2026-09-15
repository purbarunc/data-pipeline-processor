package com.purbarun.pipeline.pipeline;

import java.util.Map;

import com.purbarun.pipeline.model.Department;

// V1 NAIVE: ThreadLocal-based context sharing.
//
// THE BUG: ThreadLocal values survive task completion in a thread pool.
// If this pipeline runs inside an ExecutorService and clear() is not called,
// the next task that runs on the same thread inherits the previous tenant's
// department map and salary threshold — a silent data leak across tenants.
//
// V2 will replace this with Scoped Values (JEP 506, finalized Java 25).
// Scoped Values are automatically cleaned up when the scope exits — no leak possible.
public class PipelineContext {

    private static final ThreadLocal<Map<Integer, Department>> departmentMapHolder = new ThreadLocal<>();
    private static final ThreadLocal<String>                   tenantIdHolder       = new ThreadLocal<>();
    private static final ThreadLocal<Double>                   salaryThresholdHolder = new ThreadLocal<>();

    public static void set(Map<Integer, Department> deptMap, String tenantId, double salaryThreshold) {
        departmentMapHolder.set(deptMap);
        tenantIdHolder.set(tenantId);
        salaryThresholdHolder.set(salaryThreshold);
    }

    public static Map<Integer, Department> getDepartmentMap() {
        return departmentMapHolder.get();
    }

    public static String getTenantId() {
        return tenantIdHolder.get();
    }

    public static double getSalaryThreshold() {
        return salaryThresholdHolder.get();
    }

    // Must be called after every pipeline run — but in V1's DataPipeline it is NOT called!
    public static void clear() {
        departmentMapHolder.remove();
        tenantIdHolder.remove();
        salaryThresholdHolder.remove();
    }
}
