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

// V1 NAIVE:
// - execute() has 5 parameters — config is threaded through every method call.
//   Adding one new config value means changing this signature AND every caller.
// - Stages are directly instantiated (new FilterStage()) — no dependency injection,
//   no way to swap or mock a stage without changing this class.
// - PipelineContext.set() is called but clear() is never called — ThreadLocal leak!
// - No generics: this pipeline only works with Employee, not reusable for other types.
// V2 will use a Builder pattern + Scoped Values + generic PipelineStage<T> interface.
public class DataPipeline {

    private final FilterStage    filterStage;
    private final EnrichStage    enrichStage;
    private final ValidateStage  validateStage;
    private final AggregateStage aggregateStage;
    private final ReportGenerator reportGenerator;

    public DataPipeline() {
        this.filterStage    = new FilterStage();
        this.enrichStage    = new EnrichStage();
        this.validateStage  = new ValidateStage();
        this.aggregateStage = new AggregateStage();
        this.reportGenerator = new ReportGenerator();
    }

    // V1: Long parameter chain — every piece of config is threaded through explicitly
    public List<DepartmentSummary> execute(
            List<Employee>          employees,
            boolean                 onlyActive,
            double                  salaryThreshold,
            Map<Integer, Department> departments,
            String                  tenantId) {

        // V1 flaw: set context in ThreadLocal but never call clear() — leaks in thread pools
        PipelineContext.set(departments, tenantId, salaryThreshold);

        System.out.println("[" + tenantId + "] Pipeline starting with " + employees.size() + " records");

        // Stage 1: Filter — config passed as params (not from context)
        List<Employee> filtered = filterStage.process(employees, onlyActive, salaryThreshold);
        System.out.println("[" + tenantId + "] After filter:    " + filtered.size() + " records");

        // Stage 2: Enrich — departmentMap passed as param (not from context)
        List<Employee> enriched = enrichStage.process(filtered, departments);
        System.out.println("[" + tenantId + "] After enrich:    " + enriched.size() + " records");

        // Stage 3: Validate
        List<Employee> validated = validateStage.process(enriched);
        System.out.println("[" + tenantId + "] After validate:  " + validated.size() + " records");

        // Stage 4: Aggregate
        List<DepartmentSummary> summaries = aggregateStage.process(validated);
        System.out.println("[" + tenantId + "] After aggregate: " + summaries.size() + " department summaries");

        // V1 flaw: PipelineContext.clear() is never called here!
        return summaries;
    }

    public String generateReport(List<DepartmentSummary> summaries) {
        return reportGenerator.generateSummaryReport(summaries);
    }
}
