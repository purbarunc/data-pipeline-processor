package com.purbarun.pipeline.pipeline;

import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.report.ReportGenerator;
import com.purbarun.pipeline.stage.AggregateStage;
import com.purbarun.pipeline.stage.EnrichStage;
import com.purbarun.pipeline.stage.FilterStage;
import com.purbarun.pipeline.stage.ValidateStage;

/// # DataPipeline — V1 Naive
///
/// Wires all stages together and executes them in sequence.
///
/// ## V1 flaws (planned for later steps)
///
/// - `execute()` has 5 parameters — config is threaded through every method call.
///   Adding one new config value means changing this signature AND every caller.
/// - Stages are directly instantiated (`new FilterStage()`) — no dependency injection,
///   no way to swap or mock a stage without changing this class.
/// - `PipelineContext.set()` is called but `clear()` is never called — **ThreadLocal leak!**
/// - Stages are wired by direct field references, not through `PipelineStage<T>` —
///   `DataPipeline` cannot swap or compose stages polymorphically at runtime.
///
/// ## Step 2 done vs. still pending
///
/// - ✓ **`PipelineStage<T>` interface created** (Step 2): `ValidateStage` already implements it.
///   `FilterStage` and `EnrichStage` cannot conform yet — they still carry extra parameters
///   (`onlyActive`, `salaryThreshold`, `departmentMap`) that are removed in Step 5.
///   `AggregateStage` takes `List<Employee>` and returns `List<DepartmentSummary>` — different
///   input and output types — so it requires a different abstraction (see Step 4/5).
/// - ✗ **Builder pattern** — Step 5 (replaces the 5-parameter `execute()` signature).
/// - ✗ **Scoped Values** — Step 5 (replaces `ThreadLocal`-based `PipelineContext`).
public class DataPipeline {

    private final FilterStage     filterStage;
    private final EnrichStage     enrichStage;
    private final ValidateStage   validateStage;
    private final AggregateStage  aggregateStage;
    private final ReportGenerator reportGenerator;

    /// Constructs the pipeline by directly instantiating all stages.
    ///
    /// V1 flaw: no dependency injection — impossible to substitute a mock stage in tests
    /// without changing this constructor. Addressed in Step 5 via injected stage list.
    public DataPipeline() {
        this.filterStage    = new FilterStage();
        this.enrichStage    = new EnrichStage();
        this.validateStage  = new ValidateStage();
        this.aggregateStage = new AggregateStage();
        this.reportGenerator = new ReportGenerator();
    }

    /// Executes the full pipeline: filter → enrich → validate → aggregate.
    ///
    /// **V1 flaw:** long parameter chain — every piece of config is threaded through explicitly.
    /// Adding one new setting requires changing this signature AND every call site.
    /// Replaced by a Builder + Scoped Values in Step 5.
    ///
    /// @param employees        source records
    /// @param onlyActive       passed directly to `FilterStage`
    /// @param salaryThreshold  passed directly to `FilterStage`
    /// @param departments      lookup map passed directly to `EnrichStage`
    /// @param tenantId         used for log output and stored in `PipelineContext`
    /// @return list of per-department summaries
    public List<DepartmentSummary> execute(
            List<Employee>           employees,
            boolean                  onlyActive,
            double                   salaryThreshold,
            Map<Integer, Department> departments,
            String                   tenantId) {

        /// V1 flaw: set context in ThreadLocal but never call clear() — leaks in thread pools
        PipelineContext.set(departments, tenantId, salaryThreshold);

        System.out.println("[" + tenantId + "] Pipeline starting with " + employees.size() + " records");

        /// Stage 1: Filter — config passed as params (not from context)
        List<Employee> filtered = filterStage.process(employees, onlyActive, salaryThreshold);
        System.out.println("[" + tenantId + "] After filter:    " + filtered.size() + " records");

        /// Stage 2: Enrich — departmentMap passed as param (not from context)
        List<Employee> enriched = enrichStage.process(filtered, departments);
        System.out.println("[" + tenantId + "] After enrich:    " + enriched.size() + " records");

        /// Stage 3: Validate
        List<Employee> validated = validateStage.process(enriched);
        System.out.println("[" + tenantId + "] After validate:  " + validated.size() + " records");

        /// Stage 4: Aggregate
        List<DepartmentSummary> summaries = aggregateStage.process(validated);
        System.out.println("[" + tenantId + "] After aggregate: " + summaries.size() + " department summaries");

        /// V1 flaw: PipelineContext.clear() is never called here — ThreadLocal value leaks
        return summaries;
    }

    /// Generates a human-readable summary report from the aggregated department data.
    ///
    /// @param summaries output of `execute()`
    /// @return formatted report string
    public String generateReport(List<DepartmentSummary> summaries) {
        return reportGenerator.generateSummaryReport(summaries);
    }
}
